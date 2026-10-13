package com.blackholecodes.macroscript.parser;

import com.blackholecodes.macroscript.ast.Annotation;
import com.blackholecodes.macroscript.ast.AssignOp;
import com.blackholecodes.macroscript.ast.Block;
import com.blackholecodes.macroscript.ast.ConstraintStatement;
import com.blackholecodes.macroscript.ast.Declaration;
import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.ast.ItemMeta;
import com.blackholecodes.macroscript.ast.MacroFile;
import com.blackholecodes.macroscript.ast.Modifier;
import com.blackholecodes.macroscript.ast.Operator;
import com.blackholecodes.macroscript.ast.Stmt;
import com.blackholecodes.macroscript.ast.TriggerStatement;
import com.blackholecodes.macroscript.ast.VarType;
import com.blackholecodes.macroscript.diagnostics.Diagnostics;
import com.blackholecodes.macroscript.diagnostics.SourceRange;
import com.blackholecodes.macroscript.parser.MacroScriptParser.*;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts the ANTLR parse tree into the syntax tree in the {@code ast} package.
 *
 * <p>Parts of the tree that failed to parse are null in ANTLR. This class skips them, so a
 * file with a syntax error still gives a tree for the rest of the file.
 */
final class AstBuilder {

    /** The ANTLR channel that the lexer puts doc comments on. */
    private static final int DOC_COMMENT_CHANNEL = 2;
    private static final Pattern DURATION_PART = Pattern.compile("(\\d+)(ms|h|m|s)");

    private final BufferedTokenStream tokens;
    private final Diagnostics diagnostics;
    private final SourceOrigin origin;

    AstBuilder(BufferedTokenStream tokens, Diagnostics diagnostics, SourceOrigin origin) {
        this.tokens = tokens;
        this.diagnostics = diagnostics;
        this.origin = origin;
    }

    // ----- file and blocks ---------------------------------------------------------------

    MacroFile buildFile(FileContext ctx) {
        List<Annotation> annotations = new ArrayList<>();
        for (AnnotationContext annotation : ctx.annotation()) {
            annotations.add(buildAnnotation(annotation));
        }

        Block<Declaration> variables = null;
        Block<TriggerStatement> triggers = null;
        Block<ConstraintStatement> constraints = null;
        Block<Stmt> actions = null;
        for (TopLevelBlockContext block : ctx.topLevelBlock()) {
            SourceRange keyword = range(block.getStart());
            switch (block) {
                case VariablesBlockContext b -> {
                    checkNotRepeated(variables, keyword, "variables");
                    variables = new Block<>(buildDeclarations(b.declaration()), keyword);
                }
                case TriggersBlockContext b -> {
                    checkNotRepeated(triggers, keyword, "triggers");
                    triggers = new Block<>(mapParsed(b.triggerStatement(), this::buildTrigger), keyword);
                }
                case ConstraintsBlockContext b -> {
                    checkNotRepeated(constraints, keyword, "constraints");
                    constraints = new Block<>(mapParsed(b.constraintStatement(), this::buildConstraint), keyword);
                }
                case ActionsBlockContext b -> {
                    checkNotRepeated(actions, keyword, "actions");
                    actions = new Block<>(buildStatements(b.statement()), keyword);
                }
                default -> throw new IllegalStateException("Unknown block " + block.getClass());
            }
        }
        return new MacroFile(List.copyOf(annotations), variables, triggers, constraints, actions);
    }

    private void checkNotRepeated(Block<?> existing, SourceRange keyword, String blockName) {
        if (existing != null) {
            diagnostics.error(keyword, "A file can have only one " + blockName + " block.");
        }
    }

    private Annotation buildAnnotation(AnnotationContext ctx) {
        String name = ctx.ANNOTATION().getText().substring(1);
        Expr argument = ctx.expression() == null ? null : buildExpression(ctx.expression());
        return new Annotation(name, argument, range(ctx));
    }

    private List<Declaration> buildDeclarations(List<DeclarationContext> contexts) {
        List<Declaration> declarations = new ArrayList<>();
        for (DeclarationContext ctx : contexts) {
            if (failed(ctx) || ctx.typeName() == null) {
                continue;
            }
            Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
            for (ModifierContext modifier : ctx.modifier()) {
                Modifier value = Modifier.valueOf(modifier.getText().toUpperCase(Locale.ROOT));
                if (!modifiers.add(value)) {
                    diagnostics.error(range(modifier), "The modifier '" + modifier.getText() + "' is repeated.");
                }
            }
            VarType type = typeOf(ctx.typeName());
            for (DeclaratorContext declarator : ctx.declarator()) {
                if (declarator.IDENT() == null) {
                    continue;
                }
                Expr initializer = declarator.expression() == null ? null : buildExpression(declarator.expression());
                declarations.add(new Declaration(Set.copyOf(modifiers), type, declarator.IDENT().getText(),
                        initializer, range(declarator.IDENT().getSymbol()), range(declarator)));
            }
        }
        return List.copyOf(declarations);
    }

    private TriggerStatement buildTrigger(TriggerStatementContext ctx) {
        if (failed(ctx.call())) {
            return null;
        }
        Expr.Call call = buildCall(ctx.call());
        String label = ctx.IDENT() == null ? null : ctx.IDENT().getText();
        SourceRange labelRange = ctx.IDENT() == null ? null : range(ctx.IDENT().getSymbol());
        Expr when = ctx.whenClause() == null ? null : buildExpression(ctx.whenClause().expression());
        return new TriggerStatement(call, label, labelRange, when, buildMeta(ctx.annotation(), ctx), range(ctx));
    }

    private ConstraintStatement buildConstraint(ConstraintStatementContext ctx) {
        return new ConstraintStatement(buildExpression(ctx.expression()), buildMeta(ctx.annotation(), ctx), range(ctx));
    }

    // ----- statements --------------------------------------------------------------------

    private List<Stmt> buildStatements(List<StatementContext> contexts) {
        List<Stmt> statements = new ArrayList<>();
        for (StatementContext ctx : contexts) {
            Stmt statement = buildStatement(ctx);
            if (statement != null) {
                statements.add(statement);
            }
        }
        return List.copyOf(statements);
    }

    private Stmt buildStatement(StatementContext ctx) {
        if (failed(ctx) || failed(ctx.statementBody())) {
            return null;
        }
        ItemMeta meta = buildMeta(ctx.annotation(), ctx);
        SourceRange range = range(ctx);
        return switch (ctx.statementBody()) {
            case CallStatementContext c -> new Stmt.CallStatement(buildCall(c.call()),
                    c.whenClause() == null ? null : buildExpression(c.whenClause().expression()), meta, range);
            case AssignmentStatementContext c -> buildAssignment(c.assignment(), meta, range);
            case IfStatementContext c -> buildIf(c, meta, range);
            case WhileStatementContext c -> new Stmt.While(buildExpression(c.expression()), buildBody(c.body()), meta, range);
            case DoWhileStatementContext c -> new Stmt.DoWhile(buildBody(c.body()), buildExpression(c.expression()), meta, range);
            case RepeatStatementContext c -> new Stmt.Repeat(buildExpression(c.expression()), buildBody(c.body()), meta, range);
            case ForStatementContext c -> new Stmt.For(c.IDENT(0).getText(), c.IDENT(1).getText(),
                    buildExpression(c.expression()), buildBody(c.body()), meta, range);
            case BreakStatementContext c -> new Stmt.Break(meta, range);
            case ContinueStatementContext c -> new Stmt.Continue(meta, range);
            case ReturnStatementContext c -> new Stmt.Return(meta, range);
            default -> throw new IllegalStateException("Unknown statement " + ctx.statementBody().getClass());
        };
    }

    private Stmt buildIf(IfStatementContext ctx, ItemMeta meta, SourceRange range) {
        List<Stmt.Branch> branches = new ArrayList<>();
        branches.add(new Stmt.Branch(buildExpression(ctx.expression()), buildBody(ctx.body()), range(ctx.expression())));
        for (ElseIfClauseContext elseIf : ctx.elseIfClause()) {
            branches.add(new Stmt.Branch(buildExpression(elseIf.expression()), buildBody(elseIf.body()), range(elseIf)));
        }
        List<Stmt> elseBody = ctx.elseClause() == null ? null : buildBody(ctx.elseClause().body());
        return new Stmt.If(List.copyOf(branches), elseBody, meta, range);
    }

    private List<Stmt> buildBody(BodyContext ctx) {
        return ctx == null ? List.of() : buildStatements(ctx.statement());
    }

    private Stmt buildAssignment(AssignmentContext ctx, ItemMeta meta, SourceRange range) {
        AssignTargetContext targetCtx = ctx.assignTarget();
        List<Expr> keys = map(targetCtx.expression(), this::buildExpression);
        Stmt.Target target = new Stmt.Target(targetCtx.IDENT().getText(), keys,
                range(targetCtx.IDENT().getSymbol()), range(targetCtx));
        AssignOp op = AssignOp.fromSymbol(ctx.op.getText()).orElseThrow();
        Expr value = ctx.expression() == null ? null : buildExpression(ctx.expression());
        return new Stmt.Assignment(target, op, value, meta, range);
    }

    /**
     * Reads the statement annotations and the doc comment in front of a statement.
     * Only {@code @disabled} and {@code @comment} are permitted on statements.
     */
    private ItemMeta buildMeta(List<AnnotationContext> annotations, ParserRuleContext statement) {
        boolean disabled = false;
        String comment = docCommentBefore(statement.getStart());
        for (AnnotationContext ctx : annotations) {
            Annotation annotation = buildAnnotation(ctx);
            switch (annotation.name()) {
                case "disabled" -> {
                    if (annotation.argument() != null) {
                        diagnostics.error(annotation.range(), "@disabled takes no value.");
                    }
                    disabled = true;
                }
                case "comment" -> {
                    if (annotation.argument() instanceof Expr.StringLiteral text && text.plainText().isPresent()) {
                        comment = text.plainText().get();
                    } else {
                        diagnostics.error(annotation.range(), "@comment needs a string, for example @comment(\"Turns on WiFi\").");
                    }
                }
                default -> diagnostics.error(annotation.range(), "@" + annotation.name()
                        + " is not permitted on a statement. Use @disabled or @comment(\"...\").");
            }
        }
        return disabled || comment != null ? new ItemMeta(disabled, comment) : ItemMeta.NONE;
    }

    private String docCommentBefore(Token start) {
        List<Token> hidden = tokens.getHiddenTokensToLeft(start.getTokenIndex(), DOC_COMMENT_CHANNEL);
        if (hidden == null || hidden.isEmpty()) {
            return null;
        }
        String text = hidden.get(hidden.size() - 1).getText();
        String body = text.substring(3, text.length() - 2);
        StringBuilder cleaned = new StringBuilder();
        for (String line : body.split("\\R")) {
            String trimmed = line.strip();
            if (trimmed.startsWith("*")) {
                trimmed = trimmed.substring(1).strip();
            }
            if (!trimmed.isEmpty()) {
                if (!cleaned.isEmpty()) {
                    cleaned.append('\n');
                }
                cleaned.append(trimmed);
            }
        }
        return cleaned.isEmpty() ? null : cleaned.toString();
    }

    // ----- expressions -------------------------------------------------------------------

    Expr buildExpression(ExpressionContext ctx) {
        if (failed(ctx)) {
            return new Expr.NullLiteral(SourceRange.NONE);
        }
        SourceRange range = range(ctx);
        return switch (ctx) {
            case PrimaryExpressionContext c -> buildPrimary(c.primary());
            case IndexExpressionContext c -> new Expr.Index(buildExpression(c.expression(0)), buildExpression(c.expression(1)), range);
            case MethodCallExpressionContext c -> new Expr.MethodCall(buildExpression(c.expression()), c.IDENT().getText(),
                    buildArguments(c.arguments()), range);
            case CastExpressionContext c -> new Expr.Cast(typeOf(c.typeName()), buildExpression(c.expression()), range);
            case UnaryExpressionContext c -> buildUnary(c, range);
            case BinaryExpressionContext c -> new Expr.Binary(Operator.fromSymbol(c.op.getText()).orElseThrow(),
                    buildExpression(c.expression(0)), buildExpression(c.expression(1)), range);
            default -> throw new IllegalStateException("Unknown expression " + ctx.getClass());
        };
    }

    private Expr buildUnary(UnaryExpressionContext ctx, SourceRange range) {
        Expr operand = buildExpression(ctx.expression());
        if (ctx.op.getType() == MacroScriptLexer.NOT) {
            return new Expr.Not(operand, range);
        }
        // Fold a minus sign into a number literal, so -7 is one int literal.
        return switch (operand) {
            case Expr.IntLiteral i -> new Expr.IntLiteral(-i.value(), range);
            case Expr.DecimalLiteral d -> new Expr.DecimalLiteral(-d.value(), range);
            default -> new Expr.Negate(operand, range);
        };
    }

    private Expr buildPrimary(PrimaryContext ctx) {
        SourceRange range = range(ctx);
        return switch (ctx) {
            case LiteralPrimaryContext c -> buildLiteral(c.literal());
            case CallPrimaryContext c -> buildCall(c.call());
            case SysPrimaryContext c -> new Expr.SysValue(c.IDENT() == null ? "" : c.IDENT().getText(), range);
            case NamePrimaryContext c -> new Expr.Name(c.IDENT().getText(), range);
            case ParenPrimaryContext c -> buildExpression(c.expression());
            case ListPrimaryContext c -> new Expr.ListLiteral(map(c.expression(), this::buildExpression), range);
            case ObjectPrimaryContext c -> new Expr.ObjectLiteral(map(c.objectEntry(), this::buildObjectEntry), range);
            default -> throw new IllegalStateException("Unknown primary " + ctx.getClass());
        };
    }

    private Expr.ObjectEntry buildObjectEntry(ObjectEntryContext ctx) {
        String key;
        SourceRange keyRange;
        if (ctx.name() != null) {
            key = ctx.name().getText();
            keyRange = range(ctx.name());
        } else {
            Token token = ctx.STRING_LITERAL().getSymbol();
            keyRange = range(token);
            key = buildString(token) instanceof Expr.StringLiteral s ? s.plainText().orElse(token.getText()) : token.getText();
        }
        return new Expr.ObjectEntry(key, buildExpression(ctx.expression()), keyRange);
    }

    Expr.Call buildCall(CallContext ctx) {
        return new Expr.Call(ctx.IDENT().getText(), buildArguments(ctx.arguments()),
                range(ctx.IDENT().getSymbol()), range(ctx));
    }

    private List<Expr.Argument> buildArguments(ArgumentsContext ctx) {
        if (ctx == null) {
            return List.of();
        }
        List<Expr.Argument> arguments = new ArrayList<>();
        for (ArgumentContext argument : ctx.argument()) {
            String name = argument.name() == null ? null : argument.name().getText();
            arguments.add(new Expr.Argument(name, buildExpression(argument.expression()), range(argument)));
        }
        return List.copyOf(arguments);
    }

    private Expr buildLiteral(LiteralContext ctx) {
        Token token = ctx.getStart();
        SourceRange range = range(token);
        String text = token.getText();
        return switch (token.getType()) {
            case MacroScriptLexer.INT_LITERAL -> buildInt(text, range);
            case MacroScriptLexer.DECIMAL_LITERAL -> new Expr.DecimalLiteral(Double.parseDouble(text), range);
            case MacroScriptLexer.STRING_LITERAL -> buildString(token);
            case MacroScriptLexer.DURATION_LITERAL -> buildDuration(text, range);
            case MacroScriptLexer.COLOR_LITERAL -> buildColor(text, range);
            case MacroScriptLexer.TRUE -> new Expr.BoolLiteral(true, range);
            case MacroScriptLexer.FALSE -> new Expr.BoolLiteral(false, range);
            case MacroScriptLexer.NULL -> new Expr.NullLiteral(range);
            default -> throw new IllegalStateException("Unknown literal " + text);
        };
    }

    private Expr buildInt(String text, SourceRange range) {
        try {
            return new Expr.IntLiteral(Long.parseLong(text), range);
        } catch (NumberFormatException e) {
            diagnostics.error(range, "The number " + text + " is too large.");
            return new Expr.IntLiteral(0, range);
        }
    }

    private Expr buildDuration(String text, SourceRange range) {
        long millis = 0;
        Matcher matcher = DURATION_PART.matcher(text);
        while (matcher.find()) {
            long amount = Long.parseLong(matcher.group(1));
            millis += switch (matcher.group(2)) {
                case "ms" -> amount;
                case "s" -> amount * 1_000L;
                case "m" -> amount * 60_000L;
                default -> amount * 3_600_000L;
            };
        }
        return new Expr.DurationLiteral(millis, range);
    }

    private Expr buildColor(String text, SourceRange range) {
        String hex = text.substring(1);
        if (hex.length() == 6) {
            hex = "FF" + hex;
        }
        int argb = (int) Long.parseLong(hex, 16);
        return new Expr.ColorLiteral(argb, text.toUpperCase(Locale.ROOT), range);
    }

    private Expr buildString(Token token) {
        return new StringLiteralReader(token, origin, diagnostics).read();
    }

    // ----- helpers -----------------------------------------------------------------------

    private static VarType typeOf(TypeNameContext ctx) {
        return VarType.fromKeyword(ctx.getText()).orElseThrow();
    }

    /** True when ANTLR could not parse the context. A syntax error has then been reported. */
    private static boolean failed(ParserRuleContext ctx) {
        return ctx == null || ctx.exception != null;
    }

    /** Builds each context that parsed and leaves out the ones that did not. */
    private <A extends ParserRuleContext, B> List<B> mapParsed(List<A> items, java.util.function.Function<A, B> mapper) {
        List<B> result = new ArrayList<>(items.size());
        for (A item : items) {
            B built = failed(item) ? null : mapper.apply(item);
            if (built != null) {
                result.add(built);
            }
        }
        return List.copyOf(result);
    }

    private SourceRange range(ParserRuleContext ctx) {
        return origin.range(ctx.getStart(), ctx.getStop());
    }

    private SourceRange range(Token token) {
        return origin.range(token);
    }

    private static <A, B> List<B> map(List<A> items, java.util.function.Function<A, B> mapper) {
        List<B> result = new ArrayList<>(items.size());
        for (A item : items) {
            result.add(mapper.apply(item));
        }
        return List.copyOf(result);
    }
}
