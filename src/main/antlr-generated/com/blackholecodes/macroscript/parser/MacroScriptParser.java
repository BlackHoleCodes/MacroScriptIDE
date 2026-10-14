// Generated from com/blackholecodes/macroscript/parser/MacroScript.g4 by ANTLR 4.13.2
package com.blackholecodes.macroscript.parser;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class MacroScriptParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		VARIABLES=1, TRIGGERS=2, CONSTRAINTS=3, ACTIONS=4, IF=5, ELSE=6, WHILE=7, 
		DO=8, REPEAT=9, FOR=10, BREAK=11, CONTINUE=12, RETURN=13, TRUE=14, FALSE=15, 
		NULL=16, AS=17, WHEN=18, GLOBAL=19, SECURE=20, IN=21, OUT=22, BOOL=23, 
		INT=24, STRING=25, DECIMAL=26, DICT=27, ARRAY=28, COLOR=29, SYS=30, ANNOTATION=31, 
		DURATION_LITERAL=32, DECIMAL_LITERAL=33, INT_LITERAL=34, COLOR_LITERAL=35, 
		STRING_LITERAL=36, IDENT=37, LPAREN=38, RPAREN=39, LBRACE=40, RBRACE=41, 
		LBRACKET=42, RBRACKET=43, SEMI=44, COMMA=45, DOT=46, COLON=47, ASSIGN=48, 
		ADD_ASSIGN=49, SUB_ASSIGN=50, MUL_ASSIGN=51, DIV_ASSIGN=52, MOD_ASSIGN=53, 
		INCREMENT=54, DECREMENT=55, EQ=56, NEQ=57, LE=58, GE=59, LT=60, GT=61, 
		AND=62, OR=63, NOT=64, PLUS=65, MINUS=66, STAR=67, SLASH=68, PERCENT=69, 
		DOC_COMMENT=70, BLOCK_COMMENT=71, LINE_COMMENT=72, WHITESPACE=73, UNTERMINATED_COMMENT=74, 
		UNTERMINATED_STRING=75, ERROR_CHARACTER=76;
	public static final int
		RULE_file = 0, RULE_annotation = 1, RULE_topLevelBlock = 2, RULE_declaration = 3, 
		RULE_modifier = 4, RULE_typeName = 5, RULE_declarator = 6, RULE_triggerStatement = 7, 
		RULE_constraintStatement = 8, RULE_whenClause = 9, RULE_statement = 10, 
		RULE_statementBody = 11, RULE_elseIfClause = 12, RULE_elseClause = 13, 
		RULE_body = 14, RULE_assignment = 15, RULE_assignTarget = 16, RULE_call = 17, 
		RULE_arguments = 18, RULE_argument = 19, RULE_expression = 20, RULE_primary = 21, 
		RULE_objectEntry = 22, RULE_literal = 23, RULE_name = 24, RULE_interpolation = 25;
	private static String[] makeRuleNames() {
		return new String[] {
			"file", "annotation", "topLevelBlock", "declaration", "modifier", "typeName", 
			"declarator", "triggerStatement", "constraintStatement", "whenClause", 
			"statement", "statementBody", "elseIfClause", "elseClause", "body", "assignment", 
			"assignTarget", "call", "arguments", "argument", "expression", "primary", 
			"objectEntry", "literal", "name", "interpolation"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'variables'", "'triggers'", "'constraints'", "'actions'", "'if'", 
			"'else'", "'while'", "'do'", "'repeat'", "'for'", "'break'", "'continue'", 
			"'return'", "'true'", "'false'", "'null'", "'as'", "'when'", "'global'", 
			"'secure'", "'in'", "'out'", "'bool'", "'int'", "'string'", "'decimal'", 
			"'dict'", "'array'", "'color'", "'sys'", null, null, null, null, null, 
			null, null, "'('", "')'", "'{'", "'}'", "'['", "']'", "';'", "','", "'.'", 
			"':'", "'='", "'+='", "'-='", "'*='", "'/='", "'%='", "'++'", "'--'", 
			"'=='", "'!='", "'<='", "'>='", "'<'", "'>'", "'&&'", "'||'", "'!'", 
			"'+'", "'-'", "'*'", "'/'", "'%'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "VARIABLES", "TRIGGERS", "CONSTRAINTS", "ACTIONS", "IF", "ELSE", 
			"WHILE", "DO", "REPEAT", "FOR", "BREAK", "CONTINUE", "RETURN", "TRUE", 
			"FALSE", "NULL", "AS", "WHEN", "GLOBAL", "SECURE", "IN", "OUT", "BOOL", 
			"INT", "STRING", "DECIMAL", "DICT", "ARRAY", "COLOR", "SYS", "ANNOTATION", 
			"DURATION_LITERAL", "DECIMAL_LITERAL", "INT_LITERAL", "COLOR_LITERAL", 
			"STRING_LITERAL", "IDENT", "LPAREN", "RPAREN", "LBRACE", "RBRACE", "LBRACKET", 
			"RBRACKET", "SEMI", "COMMA", "DOT", "COLON", "ASSIGN", "ADD_ASSIGN", 
			"SUB_ASSIGN", "MUL_ASSIGN", "DIV_ASSIGN", "MOD_ASSIGN", "INCREMENT", 
			"DECREMENT", "EQ", "NEQ", "LE", "GE", "LT", "GT", "AND", "OR", "NOT", 
			"PLUS", "MINUS", "STAR", "SLASH", "PERCENT", "DOC_COMMENT", "BLOCK_COMMENT", 
			"LINE_COMMENT", "WHITESPACE", "UNTERMINATED_COMMENT", "UNTERMINATED_STRING", 
			"ERROR_CHARACTER"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "MacroScript.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public MacroScriptParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FileContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(MacroScriptParser.EOF, 0); }
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public List<TopLevelBlockContext> topLevelBlock() {
			return getRuleContexts(TopLevelBlockContext.class);
		}
		public TopLevelBlockContext topLevelBlock(int i) {
			return getRuleContext(TopLevelBlockContext.class,i);
		}
		public FileContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_file; }
	}

	public final FileContext file() throws RecognitionException {
		FileContext _localctx = new FileContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_file);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(55);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==ANNOTATION) {
				{
				{
				setState(52);
				annotation();
				}
				}
				setState(57);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(61);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 30L) != 0)) {
				{
				{
				setState(58);
				topLevelBlock();
				}
				}
				setState(63);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(64);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AnnotationContext extends ParserRuleContext {
		public TerminalNode ANNOTATION() { return getToken(MacroScriptParser.ANNOTATION, 0); }
		public TerminalNode LPAREN() { return getToken(MacroScriptParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(MacroScriptParser.RPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public AnnotationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotation; }
	}

	public final AnnotationContext annotation() throws RecognitionException {
		AnnotationContext _localctx = new AnnotationContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_annotation);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(66);
			match(ANNOTATION);
			setState(72);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				{
				setState(67);
				match(LPAREN);
				setState(69);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((((_la - 14)) & ~0x3f) == 0 && ((1L << (_la - 14)) & 5629499903115271L) != 0)) {
					{
					setState(68);
					expression(0);
					}
				}

				setState(71);
				match(RPAREN);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TopLevelBlockContext extends ParserRuleContext {
		public TopLevelBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_topLevelBlock; }
	 
		public TopLevelBlockContext() { }
		public void copyFrom(TopLevelBlockContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class VariablesBlockContext extends TopLevelBlockContext {
		public TerminalNode VARIABLES() { return getToken(MacroScriptParser.VARIABLES, 0); }
		public TerminalNode LBRACE() { return getToken(MacroScriptParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(MacroScriptParser.RBRACE, 0); }
		public List<DeclarationContext> declaration() {
			return getRuleContexts(DeclarationContext.class);
		}
		public DeclarationContext declaration(int i) {
			return getRuleContext(DeclarationContext.class,i);
		}
		public VariablesBlockContext(TopLevelBlockContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ConstraintsBlockContext extends TopLevelBlockContext {
		public TerminalNode CONSTRAINTS() { return getToken(MacroScriptParser.CONSTRAINTS, 0); }
		public TerminalNode LBRACE() { return getToken(MacroScriptParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(MacroScriptParser.RBRACE, 0); }
		public List<ConstraintStatementContext> constraintStatement() {
			return getRuleContexts(ConstraintStatementContext.class);
		}
		public ConstraintStatementContext constraintStatement(int i) {
			return getRuleContext(ConstraintStatementContext.class,i);
		}
		public ConstraintsBlockContext(TopLevelBlockContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ActionsBlockContext extends TopLevelBlockContext {
		public TerminalNode ACTIONS() { return getToken(MacroScriptParser.ACTIONS, 0); }
		public TerminalNode LBRACE() { return getToken(MacroScriptParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(MacroScriptParser.RBRACE, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public ActionsBlockContext(TopLevelBlockContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TriggersBlockContext extends TopLevelBlockContext {
		public TerminalNode TRIGGERS() { return getToken(MacroScriptParser.TRIGGERS, 0); }
		public TerminalNode LBRACE() { return getToken(MacroScriptParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(MacroScriptParser.RBRACE, 0); }
		public List<TriggerStatementContext> triggerStatement() {
			return getRuleContexts(TriggerStatementContext.class);
		}
		public TriggerStatementContext triggerStatement(int i) {
			return getRuleContext(TriggerStatementContext.class,i);
		}
		public TriggersBlockContext(TopLevelBlockContext ctx) { copyFrom(ctx); }
	}

	public final TopLevelBlockContext topLevelBlock() throws RecognitionException {
		TopLevelBlockContext _localctx = new TopLevelBlockContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_topLevelBlock);
		int _la;
		try {
			setState(110);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case VARIABLES:
				_localctx = new VariablesBlockContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(74);
				match(VARIABLES);
				setState(75);
				match(LBRACE);
				setState(79);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1073217536L) != 0)) {
					{
					{
					setState(76);
					declaration();
					}
					}
					setState(81);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(82);
				match(RBRACE);
				}
				break;
			case TRIGGERS:
				_localctx = new TriggersBlockContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(83);
				match(TRIGGERS);
				setState(84);
				match(LBRACE);
				setState(88);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ANNOTATION || _la==IDENT) {
					{
					{
					setState(85);
					triggerStatement();
					}
					}
					setState(90);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(91);
				match(RBRACE);
				}
				break;
			case CONSTRAINTS:
				_localctx = new ConstraintsBlockContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(92);
				match(CONSTRAINTS);
				setState(93);
				match(LBRACE);
				setState(97);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (((((_la - 14)) & ~0x3f) == 0 && ((1L << (_la - 14)) & 5629499903246343L) != 0)) {
					{
					{
					setState(94);
					constraintStatement();
					}
					}
					setState(99);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(100);
				match(RBRACE);
				}
				break;
			case ACTIONS:
				_localctx = new ActionsBlockContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(101);
				match(ACTIONS);
				setState(102);
				match(LBRACE);
				setState(106);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 139586453408L) != 0)) {
					{
					{
					setState(103);
					statement();
					}
					}
					setState(108);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(109);
				match(RBRACE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DeclarationContext extends ParserRuleContext {
		public TypeNameContext typeName() {
			return getRuleContext(TypeNameContext.class,0);
		}
		public List<DeclaratorContext> declarator() {
			return getRuleContexts(DeclaratorContext.class);
		}
		public DeclaratorContext declarator(int i) {
			return getRuleContext(DeclaratorContext.class,i);
		}
		public TerminalNode SEMI() { return getToken(MacroScriptParser.SEMI, 0); }
		public List<ModifierContext> modifier() {
			return getRuleContexts(ModifierContext.class);
		}
		public ModifierContext modifier(int i) {
			return getRuleContext(ModifierContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MacroScriptParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MacroScriptParser.COMMA, i);
		}
		public DeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_declaration; }
	}

	public final DeclarationContext declaration() throws RecognitionException {
		DeclarationContext _localctx = new DeclarationContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_declaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(115);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 7864320L) != 0)) {
				{
				{
				setState(112);
				modifier();
				}
				}
				setState(117);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(118);
			typeName();
			setState(119);
			declarator();
			setState(124);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(120);
				match(COMMA);
				setState(121);
				declarator();
				}
				}
				setState(126);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(127);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ModifierContext extends ParserRuleContext {
		public TerminalNode GLOBAL() { return getToken(MacroScriptParser.GLOBAL, 0); }
		public TerminalNode SECURE() { return getToken(MacroScriptParser.SECURE, 0); }
		public TerminalNode IN() { return getToken(MacroScriptParser.IN, 0); }
		public TerminalNode OUT() { return getToken(MacroScriptParser.OUT, 0); }
		public ModifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_modifier; }
	}

	public final ModifierContext modifier() throws RecognitionException {
		ModifierContext _localctx = new ModifierContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_modifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(129);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 7864320L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeNameContext extends ParserRuleContext {
		public TerminalNode BOOL() { return getToken(MacroScriptParser.BOOL, 0); }
		public TerminalNode INT() { return getToken(MacroScriptParser.INT, 0); }
		public TerminalNode STRING() { return getToken(MacroScriptParser.STRING, 0); }
		public TerminalNode DECIMAL() { return getToken(MacroScriptParser.DECIMAL, 0); }
		public TerminalNode DICT() { return getToken(MacroScriptParser.DICT, 0); }
		public TerminalNode ARRAY() { return getToken(MacroScriptParser.ARRAY, 0); }
		public TerminalNode COLOR() { return getToken(MacroScriptParser.COLOR, 0); }
		public TypeNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeName; }
	}

	public final TypeNameContext typeName() throws RecognitionException {
		TypeNameContext _localctx = new TypeNameContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_typeName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(131);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 1065353216L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DeclaratorContext extends ParserRuleContext {
		public TerminalNode IDENT() { return getToken(MacroScriptParser.IDENT, 0); }
		public TerminalNode ASSIGN() { return getToken(MacroScriptParser.ASSIGN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public DeclaratorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_declarator; }
	}

	public final DeclaratorContext declarator() throws RecognitionException {
		DeclaratorContext _localctx = new DeclaratorContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_declarator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(133);
			match(IDENT);
			setState(136);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASSIGN) {
				{
				setState(134);
				match(ASSIGN);
				setState(135);
				expression(0);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TriggerStatementContext extends ParserRuleContext {
		public CallContext call() {
			return getRuleContext(CallContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(MacroScriptParser.SEMI, 0); }
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public TerminalNode AS() { return getToken(MacroScriptParser.AS, 0); }
		public TerminalNode IDENT() { return getToken(MacroScriptParser.IDENT, 0); }
		public WhenClauseContext whenClause() {
			return getRuleContext(WhenClauseContext.class,0);
		}
		public TriggerStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_triggerStatement; }
	}

	public final TriggerStatementContext triggerStatement() throws RecognitionException {
		TriggerStatementContext _localctx = new TriggerStatementContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_triggerStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(141);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==ANNOTATION) {
				{
				{
				setState(138);
				annotation();
				}
				}
				setState(143);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(144);
			call();
			setState(147);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AS) {
				{
				setState(145);
				match(AS);
				setState(146);
				match(IDENT);
				}
			}

			setState(150);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WHEN) {
				{
				setState(149);
				whenClause();
				}
			}

			setState(152);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ConstraintStatementContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(MacroScriptParser.SEMI, 0); }
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public ConstraintStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constraintStatement; }
	}

	public final ConstraintStatementContext constraintStatement() throws RecognitionException {
		ConstraintStatementContext _localctx = new ConstraintStatementContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_constraintStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(157);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==ANNOTATION) {
				{
				{
				setState(154);
				annotation();
				}
				}
				setState(159);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(160);
			expression(0);
			setState(161);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WhenClauseContext extends ParserRuleContext {
		public TerminalNode WHEN() { return getToken(MacroScriptParser.WHEN, 0); }
		public TerminalNode LPAREN() { return getToken(MacroScriptParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(MacroScriptParser.RPAREN, 0); }
		public WhenClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whenClause; }
	}

	public final WhenClauseContext whenClause() throws RecognitionException {
		WhenClauseContext _localctx = new WhenClauseContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_whenClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(163);
			match(WHEN);
			setState(164);
			match(LPAREN);
			setState(165);
			expression(0);
			setState(166);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StatementContext extends ParserRuleContext {
		public StatementBodyContext statementBody() {
			return getRuleContext(StatementBodyContext.class,0);
		}
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(171);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==ANNOTATION) {
				{
				{
				setState(168);
				annotation();
				}
				}
				setState(173);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(174);
			statementBody();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StatementBodyContext extends ParserRuleContext {
		public StatementBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statementBody; }
	 
		public StatementBodyContext() { }
		public void copyFrom(StatementBodyContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class WhileStatementContext extends StatementBodyContext {
		public TerminalNode WHILE() { return getToken(MacroScriptParser.WHILE, 0); }
		public TerminalNode LPAREN() { return getToken(MacroScriptParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(MacroScriptParser.RPAREN, 0); }
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public WhileStatementContext(StatementBodyContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class RepeatStatementContext extends StatementBodyContext {
		public TerminalNode REPEAT() { return getToken(MacroScriptParser.REPEAT, 0); }
		public TerminalNode LPAREN() { return getToken(MacroScriptParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(MacroScriptParser.RPAREN, 0); }
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public RepeatStatementContext(StatementBodyContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AssignmentStatementContext extends StatementBodyContext {
		public AssignmentContext assignment() {
			return getRuleContext(AssignmentContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(MacroScriptParser.SEMI, 0); }
		public AssignmentStatementContext(StatementBodyContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ForStatementContext extends StatementBodyContext {
		public TerminalNode FOR() { return getToken(MacroScriptParser.FOR, 0); }
		public TerminalNode LPAREN() { return getToken(MacroScriptParser.LPAREN, 0); }
		public List<TerminalNode> IDENT() { return getTokens(MacroScriptParser.IDENT); }
		public TerminalNode IDENT(int i) {
			return getToken(MacroScriptParser.IDENT, i);
		}
		public TerminalNode COMMA() { return getToken(MacroScriptParser.COMMA, 0); }
		public TerminalNode COLON() { return getToken(MacroScriptParser.COLON, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(MacroScriptParser.RPAREN, 0); }
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public ForStatementContext(StatementBodyContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BreakStatementContext extends StatementBodyContext {
		public TerminalNode BREAK() { return getToken(MacroScriptParser.BREAK, 0); }
		public TerminalNode SEMI() { return getToken(MacroScriptParser.SEMI, 0); }
		public BreakStatementContext(StatementBodyContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CallStatementContext extends StatementBodyContext {
		public CallContext call() {
			return getRuleContext(CallContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(MacroScriptParser.SEMI, 0); }
		public WhenClauseContext whenClause() {
			return getRuleContext(WhenClauseContext.class,0);
		}
		public CallStatementContext(StatementBodyContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DoWhileStatementContext extends StatementBodyContext {
		public TerminalNode DO() { return getToken(MacroScriptParser.DO, 0); }
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public TerminalNode WHILE() { return getToken(MacroScriptParser.WHILE, 0); }
		public TerminalNode LPAREN() { return getToken(MacroScriptParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(MacroScriptParser.RPAREN, 0); }
		public TerminalNode SEMI() { return getToken(MacroScriptParser.SEMI, 0); }
		public DoWhileStatementContext(StatementBodyContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ContinueStatementContext extends StatementBodyContext {
		public TerminalNode CONTINUE() { return getToken(MacroScriptParser.CONTINUE, 0); }
		public TerminalNode SEMI() { return getToken(MacroScriptParser.SEMI, 0); }
		public ContinueStatementContext(StatementBodyContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class IfStatementContext extends StatementBodyContext {
		public TerminalNode IF() { return getToken(MacroScriptParser.IF, 0); }
		public TerminalNode LPAREN() { return getToken(MacroScriptParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(MacroScriptParser.RPAREN, 0); }
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public List<ElseIfClauseContext> elseIfClause() {
			return getRuleContexts(ElseIfClauseContext.class);
		}
		public ElseIfClauseContext elseIfClause(int i) {
			return getRuleContext(ElseIfClauseContext.class,i);
		}
		public ElseClauseContext elseClause() {
			return getRuleContext(ElseClauseContext.class,0);
		}
		public IfStatementContext(StatementBodyContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ReturnStatementContext extends StatementBodyContext {
		public TerminalNode RETURN() { return getToken(MacroScriptParser.RETURN, 0); }
		public TerminalNode SEMI() { return getToken(MacroScriptParser.SEMI, 0); }
		public ReturnStatementContext(StatementBodyContext ctx) { copyFrom(ctx); }
	}

	public final StatementBodyContext statementBody() throws RecognitionException {
		StatementBodyContext _localctx = new StatementBodyContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_statementBody);
		int _la;
		try {
			int _alt;
			setState(235);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,20,_ctx) ) {
			case 1:
				_localctx = new CallStatementContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(176);
				call();
				setState(178);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WHEN) {
					{
					setState(177);
					whenClause();
					}
				}

				setState(180);
				match(SEMI);
				}
				break;
			case 2:
				_localctx = new AssignmentStatementContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(182);
				assignment();
				setState(183);
				match(SEMI);
				}
				break;
			case 3:
				_localctx = new IfStatementContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(185);
				match(IF);
				setState(186);
				match(LPAREN);
				setState(187);
				expression(0);
				setState(188);
				match(RPAREN);
				setState(189);
				body();
				setState(193);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(190);
						elseIfClause();
						}
						} 
					}
					setState(195);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
				}
				setState(197);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELSE) {
					{
					setState(196);
					elseClause();
					}
				}

				}
				break;
			case 4:
				_localctx = new WhileStatementContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(199);
				match(WHILE);
				setState(200);
				match(LPAREN);
				setState(201);
				expression(0);
				setState(202);
				match(RPAREN);
				setState(203);
				body();
				}
				break;
			case 5:
				_localctx = new DoWhileStatementContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(205);
				match(DO);
				setState(206);
				body();
				setState(207);
				match(WHILE);
				setState(208);
				match(LPAREN);
				setState(209);
				expression(0);
				setState(210);
				match(RPAREN);
				setState(211);
				match(SEMI);
				}
				break;
			case 6:
				_localctx = new RepeatStatementContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(213);
				match(REPEAT);
				setState(214);
				match(LPAREN);
				setState(215);
				expression(0);
				setState(216);
				match(RPAREN);
				setState(217);
				body();
				}
				break;
			case 7:
				_localctx = new ForStatementContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(219);
				match(FOR);
				setState(220);
				match(LPAREN);
				setState(221);
				match(IDENT);
				setState(222);
				match(COMMA);
				setState(223);
				match(IDENT);
				setState(224);
				match(COLON);
				setState(225);
				expression(0);
				setState(226);
				match(RPAREN);
				setState(227);
				body();
				}
				break;
			case 8:
				_localctx = new BreakStatementContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(229);
				match(BREAK);
				setState(230);
				match(SEMI);
				}
				break;
			case 9:
				_localctx = new ContinueStatementContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(231);
				match(CONTINUE);
				setState(232);
				match(SEMI);
				}
				break;
			case 10:
				_localctx = new ReturnStatementContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(233);
				match(RETURN);
				setState(234);
				match(SEMI);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ElseIfClauseContext extends ParserRuleContext {
		public TerminalNode ELSE() { return getToken(MacroScriptParser.ELSE, 0); }
		public TerminalNode IF() { return getToken(MacroScriptParser.IF, 0); }
		public TerminalNode LPAREN() { return getToken(MacroScriptParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(MacroScriptParser.RPAREN, 0); }
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public ElseIfClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elseIfClause; }
	}

	public final ElseIfClauseContext elseIfClause() throws RecognitionException {
		ElseIfClauseContext _localctx = new ElseIfClauseContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_elseIfClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(237);
			match(ELSE);
			setState(238);
			match(IF);
			setState(239);
			match(LPAREN);
			setState(240);
			expression(0);
			setState(241);
			match(RPAREN);
			setState(242);
			body();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ElseClauseContext extends ParserRuleContext {
		public TerminalNode ELSE() { return getToken(MacroScriptParser.ELSE, 0); }
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public ElseClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elseClause; }
	}

	public final ElseClauseContext elseClause() throws RecognitionException {
		ElseClauseContext _localctx = new ElseClauseContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_elseClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(244);
			match(ELSE);
			setState(245);
			body();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BodyContext extends ParserRuleContext {
		public TerminalNode LBRACE() { return getToken(MacroScriptParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(MacroScriptParser.RBRACE, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public BodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_body; }
	}

	public final BodyContext body() throws RecognitionException {
		BodyContext _localctx = new BodyContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_body);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(247);
			match(LBRACE);
			setState(251);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 139586453408L) != 0)) {
				{
				{
				setState(248);
				statement();
				}
				}
				setState(253);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(254);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AssignmentContext extends ParserRuleContext {
		public Token op;
		public AssignTargetContext assignTarget() {
			return getRuleContext(AssignTargetContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode ASSIGN() { return getToken(MacroScriptParser.ASSIGN, 0); }
		public TerminalNode ADD_ASSIGN() { return getToken(MacroScriptParser.ADD_ASSIGN, 0); }
		public TerminalNode SUB_ASSIGN() { return getToken(MacroScriptParser.SUB_ASSIGN, 0); }
		public TerminalNode MUL_ASSIGN() { return getToken(MacroScriptParser.MUL_ASSIGN, 0); }
		public TerminalNode DIV_ASSIGN() { return getToken(MacroScriptParser.DIV_ASSIGN, 0); }
		public TerminalNode MOD_ASSIGN() { return getToken(MacroScriptParser.MOD_ASSIGN, 0); }
		public TerminalNode INCREMENT() { return getToken(MacroScriptParser.INCREMENT, 0); }
		public TerminalNode DECREMENT() { return getToken(MacroScriptParser.DECREMENT, 0); }
		public AssignmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignment; }
	}

	public final AssignmentContext assignment() throws RecognitionException {
		AssignmentContext _localctx = new AssignmentContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_assignment);
		int _la;
		try {
			setState(263);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(256);
				assignTarget();
				setState(257);
				((AssignmentContext)_localctx).op = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 17732923532771328L) != 0)) ) {
					((AssignmentContext)_localctx).op = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(258);
				expression(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(260);
				assignTarget();
				setState(261);
				((AssignmentContext)_localctx).op = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==INCREMENT || _la==DECREMENT) ) {
					((AssignmentContext)_localctx).op = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AssignTargetContext extends ParserRuleContext {
		public TerminalNode IDENT() { return getToken(MacroScriptParser.IDENT, 0); }
		public List<TerminalNode> LBRACKET() { return getTokens(MacroScriptParser.LBRACKET); }
		public TerminalNode LBRACKET(int i) {
			return getToken(MacroScriptParser.LBRACKET, i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> RBRACKET() { return getTokens(MacroScriptParser.RBRACKET); }
		public TerminalNode RBRACKET(int i) {
			return getToken(MacroScriptParser.RBRACKET, i);
		}
		public AssignTargetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignTarget; }
	}

	public final AssignTargetContext assignTarget() throws RecognitionException {
		AssignTargetContext _localctx = new AssignTargetContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_assignTarget);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(265);
			match(IDENT);
			setState(272);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==LBRACKET) {
				{
				{
				setState(266);
				match(LBRACKET);
				setState(267);
				expression(0);
				setState(268);
				match(RBRACKET);
				}
				}
				setState(274);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CallContext extends ParserRuleContext {
		public TerminalNode IDENT() { return getToken(MacroScriptParser.IDENT, 0); }
		public TerminalNode LPAREN() { return getToken(MacroScriptParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(MacroScriptParser.RPAREN, 0); }
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public CallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_call; }
	}

	public final CallContext call() throws RecognitionException {
		CallContext _localctx = new CallContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_call);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(275);
			match(IDENT);
			setState(276);
			match(LPAREN);
			setState(278);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 6045166469118L) != 0) || _la==NOT || _la==MINUS) {
				{
				setState(277);
				arguments();
				}
			}

			setState(280);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArgumentsContext extends ParserRuleContext {
		public List<ArgumentContext> argument() {
			return getRuleContexts(ArgumentContext.class);
		}
		public ArgumentContext argument(int i) {
			return getRuleContext(ArgumentContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MacroScriptParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MacroScriptParser.COMMA, i);
		}
		public ArgumentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arguments; }
	}

	public final ArgumentsContext arguments() throws RecognitionException {
		ArgumentsContext _localctx = new ArgumentsContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_arguments);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(282);
			argument();
			setState(287);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(283);
					match(COMMA);
					setState(284);
					argument();
					}
					} 
				}
				setState(289);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
			}
			setState(291);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(290);
				match(COMMA);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArgumentContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public TerminalNode COLON() { return getToken(MacroScriptParser.COLON, 0); }
		public ArgumentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_argument; }
	}

	public final ArgumentContext argument() throws RecognitionException {
		ArgumentContext _localctx = new ArgumentContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_argument);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(296);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
			case 1:
				{
				setState(293);
				name();
				setState(294);
				match(COLON);
				}
				break;
			}
			setState(298);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionContext extends ParserRuleContext {
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
	 
		public ExpressionContext() { }
		public void copyFrom(ExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PrimaryExpressionContext extends ExpressionContext {
		public PrimaryContext primary() {
			return getRuleContext(PrimaryContext.class,0);
		}
		public PrimaryExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BinaryExpressionContext extends ExpressionContext {
		public Token op;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode STAR() { return getToken(MacroScriptParser.STAR, 0); }
		public TerminalNode SLASH() { return getToken(MacroScriptParser.SLASH, 0); }
		public TerminalNode PERCENT() { return getToken(MacroScriptParser.PERCENT, 0); }
		public TerminalNode PLUS() { return getToken(MacroScriptParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(MacroScriptParser.MINUS, 0); }
		public TerminalNode LT() { return getToken(MacroScriptParser.LT, 0); }
		public TerminalNode GT() { return getToken(MacroScriptParser.GT, 0); }
		public TerminalNode LE() { return getToken(MacroScriptParser.LE, 0); }
		public TerminalNode GE() { return getToken(MacroScriptParser.GE, 0); }
		public TerminalNode EQ() { return getToken(MacroScriptParser.EQ, 0); }
		public TerminalNode NEQ() { return getToken(MacroScriptParser.NEQ, 0); }
		public TerminalNode AND() { return getToken(MacroScriptParser.AND, 0); }
		public TerminalNode OR() { return getToken(MacroScriptParser.OR, 0); }
		public BinaryExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CastExpressionContext extends ExpressionContext {
		public TerminalNode LPAREN() { return getToken(MacroScriptParser.LPAREN, 0); }
		public TypeNameContext typeName() {
			return getRuleContext(TypeNameContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(MacroScriptParser.RPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public CastExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MethodCallExpressionContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode DOT() { return getToken(MacroScriptParser.DOT, 0); }
		public TerminalNode IDENT() { return getToken(MacroScriptParser.IDENT, 0); }
		public TerminalNode LPAREN() { return getToken(MacroScriptParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(MacroScriptParser.RPAREN, 0); }
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public MethodCallExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class IndexExpressionContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode LBRACKET() { return getToken(MacroScriptParser.LBRACKET, 0); }
		public TerminalNode RBRACKET() { return getToken(MacroScriptParser.RBRACKET, 0); }
		public IndexExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class UnaryExpressionContext extends ExpressionContext {
		public Token op;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode NOT() { return getToken(MacroScriptParser.NOT, 0); }
		public TerminalNode MINUS() { return getToken(MacroScriptParser.MINUS, 0); }
		public UnaryExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
	}

	public final ExpressionContext expression() throws RecognitionException {
		return expression(0);
	}

	private ExpressionContext expression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExpressionContext _localctx = new ExpressionContext(_ctx, _parentState);
		ExpressionContext _prevctx = _localctx;
		int _startState = 40;
		enterRecursionRule(_localctx, 40, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(309);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
			case 1:
				{
				_localctx = new PrimaryExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(301);
				primary();
				}
				break;
			case 2:
				{
				_localctx = new CastExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(302);
				match(LPAREN);
				setState(303);
				typeName();
				setState(304);
				match(RPAREN);
				setState(305);
				expression(8);
				}
				break;
			case 3:
				{
				_localctx = new UnaryExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(307);
				((UnaryExpressionContext)_localctx).op = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==NOT || _la==MINUS) ) {
					((UnaryExpressionContext)_localctx).op = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(308);
				expression(7);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(344);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,31,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(342);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
					case 1:
						{
						_localctx = new BinaryExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(311);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(312);
						((BinaryExpressionContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 7L) != 0)) ) {
							((BinaryExpressionContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(313);
						expression(7);
						}
						break;
					case 2:
						{
						_localctx = new BinaryExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(314);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(315);
						((BinaryExpressionContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==PLUS || _la==MINUS) ) {
							((BinaryExpressionContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(316);
						expression(6);
						}
						break;
					case 3:
						{
						_localctx = new BinaryExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(317);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(318);
						((BinaryExpressionContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 4323455642275676160L) != 0)) ) {
							((BinaryExpressionContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(319);
						expression(5);
						}
						break;
					case 4:
						{
						_localctx = new BinaryExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(320);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(321);
						((BinaryExpressionContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==EQ || _la==NEQ) ) {
							((BinaryExpressionContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(322);
						expression(4);
						}
						break;
					case 5:
						{
						_localctx = new BinaryExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(323);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(324);
						((BinaryExpressionContext)_localctx).op = match(AND);
						setState(325);
						expression(3);
						}
						break;
					case 6:
						{
						_localctx = new BinaryExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(326);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(327);
						((BinaryExpressionContext)_localctx).op = match(OR);
						setState(328);
						expression(2);
						}
						break;
					case 7:
						{
						_localctx = new IndexExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(329);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(330);
						match(LBRACKET);
						setState(331);
						expression(0);
						setState(332);
						match(RBRACKET);
						}
						break;
					case 8:
						{
						_localctx = new MethodCallExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(334);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(335);
						match(DOT);
						setState(336);
						match(IDENT);
						setState(337);
						match(LPAREN);
						setState(339);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 6045166469118L) != 0) || _la==NOT || _la==MINUS) {
							{
							setState(338);
							arguments();
							}
						}

						setState(341);
						match(RPAREN);
						}
						break;
					}
					} 
				}
				setState(346);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,31,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PrimaryContext extends ParserRuleContext {
		public PrimaryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primary; }
	 
		public PrimaryContext() { }
		public void copyFrom(PrimaryContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ListPrimaryContext extends PrimaryContext {
		public TerminalNode LBRACKET() { return getToken(MacroScriptParser.LBRACKET, 0); }
		public TerminalNode RBRACKET() { return getToken(MacroScriptParser.RBRACKET, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MacroScriptParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MacroScriptParser.COMMA, i);
		}
		public ListPrimaryContext(PrimaryContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ObjectPrimaryContext extends PrimaryContext {
		public TerminalNode LBRACE() { return getToken(MacroScriptParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(MacroScriptParser.RBRACE, 0); }
		public List<ObjectEntryContext> objectEntry() {
			return getRuleContexts(ObjectEntryContext.class);
		}
		public ObjectEntryContext objectEntry(int i) {
			return getRuleContext(ObjectEntryContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MacroScriptParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MacroScriptParser.COMMA, i);
		}
		public ObjectPrimaryContext(PrimaryContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ParenPrimaryContext extends PrimaryContext {
		public TerminalNode LPAREN() { return getToken(MacroScriptParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(MacroScriptParser.RPAREN, 0); }
		public ParenPrimaryContext(PrimaryContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SysPrimaryContext extends PrimaryContext {
		public TerminalNode SYS() { return getToken(MacroScriptParser.SYS, 0); }
		public TerminalNode DOT() { return getToken(MacroScriptParser.DOT, 0); }
		public TerminalNode IDENT() { return getToken(MacroScriptParser.IDENT, 0); }
		public SysPrimaryContext(PrimaryContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LiteralPrimaryContext extends PrimaryContext {
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public LiteralPrimaryContext(PrimaryContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CallPrimaryContext extends PrimaryContext {
		public CallContext call() {
			return getRuleContext(CallContext.class,0);
		}
		public CallPrimaryContext(PrimaryContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NamePrimaryContext extends PrimaryContext {
		public TerminalNode IDENT() { return getToken(MacroScriptParser.IDENT, 0); }
		public NamePrimaryContext(PrimaryContext ctx) { copyFrom(ctx); }
	}

	public final PrimaryContext primary() throws RecognitionException {
		PrimaryContext _localctx = new PrimaryContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_primary);
		int _la;
		try {
			int _alt;
			setState(387);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,38,_ctx) ) {
			case 1:
				_localctx = new LiteralPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(347);
				literal();
				}
				break;
			case 2:
				_localctx = new CallPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(348);
				call();
				}
				break;
			case 3:
				_localctx = new SysPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(349);
				match(SYS);
				setState(350);
				match(DOT);
				setState(351);
				match(IDENT);
				}
				break;
			case 4:
				_localctx = new NamePrimaryContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(352);
				match(IDENT);
				}
				break;
			case 5:
				_localctx = new ParenPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(353);
				match(LPAREN);
				setState(354);
				expression(0);
				setState(355);
				match(RPAREN);
				}
				break;
			case 6:
				_localctx = new ListPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(357);
				match(LBRACKET);
				setState(369);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((((_la - 14)) & ~0x3f) == 0 && ((1L << (_la - 14)) & 5629499903115271L) != 0)) {
					{
					setState(358);
					expression(0);
					setState(363);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,32,_ctx);
					while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(359);
							match(COMMA);
							setState(360);
							expression(0);
							}
							} 
						}
						setState(365);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,32,_ctx);
					}
					setState(367);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==COMMA) {
						{
						setState(366);
						match(COMMA);
						}
					}

					}
				}

				setState(371);
				match(RBRACKET);
				}
				break;
			case 7:
				_localctx = new ObjectPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(372);
				match(LBRACE);
				setState(384);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 208305913854L) != 0)) {
					{
					setState(373);
					objectEntry();
					setState(378);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,35,_ctx);
					while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(374);
							match(COMMA);
							setState(375);
							objectEntry();
							}
							} 
						}
						setState(380);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,35,_ctx);
					}
					setState(382);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==COMMA) {
						{
						setState(381);
						match(COMMA);
						}
					}

					}
				}

				setState(386);
				match(RBRACE);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ObjectEntryContext extends ParserRuleContext {
		public TerminalNode COLON() { return getToken(MacroScriptParser.COLON, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public TerminalNode STRING_LITERAL() { return getToken(MacroScriptParser.STRING_LITERAL, 0); }
		public ObjectEntryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_objectEntry; }
	}

	public final ObjectEntryContext objectEntry() throws RecognitionException {
		ObjectEntryContext _localctx = new ObjectEntryContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_objectEntry);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(391);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case VARIABLES:
			case TRIGGERS:
			case CONSTRAINTS:
			case ACTIONS:
			case IF:
			case ELSE:
			case WHILE:
			case DO:
			case REPEAT:
			case FOR:
			case BREAK:
			case CONTINUE:
			case RETURN:
			case TRUE:
			case FALSE:
			case NULL:
			case AS:
			case WHEN:
			case GLOBAL:
			case SECURE:
			case IN:
			case OUT:
			case BOOL:
			case INT:
			case STRING:
			case DECIMAL:
			case DICT:
			case ARRAY:
			case COLOR:
			case SYS:
			case IDENT:
				{
				setState(389);
				name();
				}
				break;
			case STRING_LITERAL:
				{
				setState(390);
				match(STRING_LITERAL);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(393);
			match(COLON);
			setState(394);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LiteralContext extends ParserRuleContext {
		public TerminalNode INT_LITERAL() { return getToken(MacroScriptParser.INT_LITERAL, 0); }
		public TerminalNode DECIMAL_LITERAL() { return getToken(MacroScriptParser.DECIMAL_LITERAL, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(MacroScriptParser.STRING_LITERAL, 0); }
		public TerminalNode DURATION_LITERAL() { return getToken(MacroScriptParser.DURATION_LITERAL, 0); }
		public TerminalNode COLOR_LITERAL() { return getToken(MacroScriptParser.COLOR_LITERAL, 0); }
		public TerminalNode TRUE() { return getToken(MacroScriptParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(MacroScriptParser.FALSE, 0); }
		public TerminalNode NULL() { return getToken(MacroScriptParser.NULL, 0); }
		public LiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal; }
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_literal);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(396);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 133144100864L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NameContext extends ParserRuleContext {
		public TerminalNode IDENT() { return getToken(MacroScriptParser.IDENT, 0); }
		public TerminalNode VARIABLES() { return getToken(MacroScriptParser.VARIABLES, 0); }
		public TerminalNode TRIGGERS() { return getToken(MacroScriptParser.TRIGGERS, 0); }
		public TerminalNode CONSTRAINTS() { return getToken(MacroScriptParser.CONSTRAINTS, 0); }
		public TerminalNode ACTIONS() { return getToken(MacroScriptParser.ACTIONS, 0); }
		public TerminalNode IF() { return getToken(MacroScriptParser.IF, 0); }
		public TerminalNode ELSE() { return getToken(MacroScriptParser.ELSE, 0); }
		public TerminalNode WHILE() { return getToken(MacroScriptParser.WHILE, 0); }
		public TerminalNode DO() { return getToken(MacroScriptParser.DO, 0); }
		public TerminalNode REPEAT() { return getToken(MacroScriptParser.REPEAT, 0); }
		public TerminalNode FOR() { return getToken(MacroScriptParser.FOR, 0); }
		public TerminalNode BREAK() { return getToken(MacroScriptParser.BREAK, 0); }
		public TerminalNode CONTINUE() { return getToken(MacroScriptParser.CONTINUE, 0); }
		public TerminalNode RETURN() { return getToken(MacroScriptParser.RETURN, 0); }
		public TerminalNode TRUE() { return getToken(MacroScriptParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(MacroScriptParser.FALSE, 0); }
		public TerminalNode NULL() { return getToken(MacroScriptParser.NULL, 0); }
		public TerminalNode AS() { return getToken(MacroScriptParser.AS, 0); }
		public TerminalNode WHEN() { return getToken(MacroScriptParser.WHEN, 0); }
		public TerminalNode GLOBAL() { return getToken(MacroScriptParser.GLOBAL, 0); }
		public TerminalNode SECURE() { return getToken(MacroScriptParser.SECURE, 0); }
		public TerminalNode IN() { return getToken(MacroScriptParser.IN, 0); }
		public TerminalNode OUT() { return getToken(MacroScriptParser.OUT, 0); }
		public TerminalNode BOOL() { return getToken(MacroScriptParser.BOOL, 0); }
		public TerminalNode INT() { return getToken(MacroScriptParser.INT, 0); }
		public TerminalNode STRING() { return getToken(MacroScriptParser.STRING, 0); }
		public TerminalNode DECIMAL() { return getToken(MacroScriptParser.DECIMAL, 0); }
		public TerminalNode DICT() { return getToken(MacroScriptParser.DICT, 0); }
		public TerminalNode ARRAY() { return getToken(MacroScriptParser.ARRAY, 0); }
		public TerminalNode COLOR() { return getToken(MacroScriptParser.COLOR, 0); }
		public TerminalNode SYS() { return getToken(MacroScriptParser.SYS, 0); }
		public NameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_name; }
	}

	public final NameContext name() throws RecognitionException {
		NameContext _localctx = new NameContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_name);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(398);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 139586437118L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class InterpolationContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode EOF() { return getToken(MacroScriptParser.EOF, 0); }
		public InterpolationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interpolation; }
	}

	public final InterpolationContext interpolation() throws RecognitionException {
		InterpolationContext _localctx = new InterpolationContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_interpolation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(400);
			expression(0);
			setState(401);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 20:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 6);
		case 1:
			return precpred(_ctx, 5);
		case 2:
			return precpred(_ctx, 4);
		case 3:
			return precpred(_ctx, 3);
		case 4:
			return precpred(_ctx, 2);
		case 5:
			return precpred(_ctx, 1);
		case 6:
			return precpred(_ctx, 10);
		case 7:
			return precpred(_ctx, 9);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001L\u0194\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0001\u0000\u0005\u00006\b\u0000\n\u0000\f\u0000"+
		"9\t\u0000\u0001\u0000\u0005\u0000<\b\u0000\n\u0000\f\u0000?\t\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001F\b"+
		"\u0001\u0001\u0001\u0003\u0001I\b\u0001\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0005\u0002N\b\u0002\n\u0002\f\u0002Q\t\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0005\u0002W\b\u0002\n\u0002\f\u0002Z\t"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0005\u0002`\b"+
		"\u0002\n\u0002\f\u0002c\t\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0005\u0002i\b\u0002\n\u0002\f\u0002l\t\u0002\u0001\u0002\u0003"+
		"\u0002o\b\u0002\u0001\u0003\u0005\u0003r\b\u0003\n\u0003\f\u0003u\t\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0005\u0003{\b\u0003"+
		"\n\u0003\f\u0003~\t\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004"+
		"\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0003\u0006"+
		"\u0089\b\u0006\u0001\u0007\u0005\u0007\u008c\b\u0007\n\u0007\f\u0007\u008f"+
		"\t\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u0094\b\u0007"+
		"\u0001\u0007\u0003\u0007\u0097\b\u0007\u0001\u0007\u0001\u0007\u0001\b"+
		"\u0005\b\u009c\b\b\n\b\f\b\u009f\t\b\u0001\b\u0001\b\u0001\b\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\n\u0005\n\u00aa\b\n\n\n\f\n\u00ad\t\n"+
		"\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0003\u000b\u00b3\b\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0005\u000b\u00c0"+
		"\b\u000b\n\u000b\f\u000b\u00c3\t\u000b\u0001\u000b\u0003\u000b\u00c6\b"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0003\u000b\u00ec\b\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\r\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0005"+
		"\u000e\u00fa\b\u000e\n\u000e\f\u000e\u00fd\t\u000e\u0001\u000e\u0001\u000e"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f"+
		"\u0001\u000f\u0003\u000f\u0108\b\u000f\u0001\u0010\u0001\u0010\u0001\u0010"+
		"\u0001\u0010\u0001\u0010\u0005\u0010\u010f\b\u0010\n\u0010\f\u0010\u0112"+
		"\t\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0003\u0011\u0117\b\u0011"+
		"\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0005\u0012"+
		"\u011e\b\u0012\n\u0012\f\u0012\u0121\t\u0012\u0001\u0012\u0003\u0012\u0124"+
		"\b\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0003\u0013\u0129\b\u0013"+
		"\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0003\u0014"+
		"\u0136\b\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0003\u0014"+
		"\u0154\b\u0014\u0001\u0014\u0005\u0014\u0157\b\u0014\n\u0014\f\u0014\u015a"+
		"\t\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0005\u0015\u016a\b\u0015\n\u0015\f\u0015"+
		"\u016d\t\u0015\u0001\u0015\u0003\u0015\u0170\b\u0015\u0003\u0015\u0172"+
		"\b\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0005"+
		"\u0015\u0179\b\u0015\n\u0015\f\u0015\u017c\t\u0015\u0001\u0015\u0003\u0015"+
		"\u017f\b\u0015\u0003\u0015\u0181\b\u0015\u0001\u0015\u0003\u0015\u0184"+
		"\b\u0015\u0001\u0016\u0001\u0016\u0003\u0016\u0188\b\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0017\u0001\u0017\u0001\u0018\u0001\u0018"+
		"\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0000\u0001(\u001a\u0000"+
		"\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c"+
		"\u001e \"$&(*,.02\u0000\u000b\u0001\u0000\u0013\u0016\u0001\u0000\u0017"+
		"\u001d\u0001\u000005\u0001\u000067\u0002\u0000@@BB\u0001\u0000CE\u0001"+
		"\u0000AB\u0001\u0000:=\u0001\u000089\u0002\u0000\u000e\u0010 $\u0002\u0000"+
		"\u0001\u001e%%\u01b7\u00007\u0001\u0000\u0000\u0000\u0002B\u0001\u0000"+
		"\u0000\u0000\u0004n\u0001\u0000\u0000\u0000\u0006s\u0001\u0000\u0000\u0000"+
		"\b\u0081\u0001\u0000\u0000\u0000\n\u0083\u0001\u0000\u0000\u0000\f\u0085"+
		"\u0001\u0000\u0000\u0000\u000e\u008d\u0001\u0000\u0000\u0000\u0010\u009d"+
		"\u0001\u0000\u0000\u0000\u0012\u00a3\u0001\u0000\u0000\u0000\u0014\u00ab"+
		"\u0001\u0000\u0000\u0000\u0016\u00eb\u0001\u0000\u0000\u0000\u0018\u00ed"+
		"\u0001\u0000\u0000\u0000\u001a\u00f4\u0001\u0000\u0000\u0000\u001c\u00f7"+
		"\u0001\u0000\u0000\u0000\u001e\u0107\u0001\u0000\u0000\u0000 \u0109\u0001"+
		"\u0000\u0000\u0000\"\u0113\u0001\u0000\u0000\u0000$\u011a\u0001\u0000"+
		"\u0000\u0000&\u0128\u0001\u0000\u0000\u0000(\u0135\u0001\u0000\u0000\u0000"+
		"*\u0183\u0001\u0000\u0000\u0000,\u0187\u0001\u0000\u0000\u0000.\u018c"+
		"\u0001\u0000\u0000\u00000\u018e\u0001\u0000\u0000\u00002\u0190\u0001\u0000"+
		"\u0000\u000046\u0003\u0002\u0001\u000054\u0001\u0000\u0000\u000069\u0001"+
		"\u0000\u0000\u000075\u0001\u0000\u0000\u000078\u0001\u0000\u0000\u0000"+
		"8=\u0001\u0000\u0000\u000097\u0001\u0000\u0000\u0000:<\u0003\u0004\u0002"+
		"\u0000;:\u0001\u0000\u0000\u0000<?\u0001\u0000\u0000\u0000=;\u0001\u0000"+
		"\u0000\u0000=>\u0001\u0000\u0000\u0000>@\u0001\u0000\u0000\u0000?=\u0001"+
		"\u0000\u0000\u0000@A\u0005\u0000\u0000\u0001A\u0001\u0001\u0000\u0000"+
		"\u0000BH\u0005\u001f\u0000\u0000CE\u0005&\u0000\u0000DF\u0003(\u0014\u0000"+
		"ED\u0001\u0000\u0000\u0000EF\u0001\u0000\u0000\u0000FG\u0001\u0000\u0000"+
		"\u0000GI\u0005\'\u0000\u0000HC\u0001\u0000\u0000\u0000HI\u0001\u0000\u0000"+
		"\u0000I\u0003\u0001\u0000\u0000\u0000JK\u0005\u0001\u0000\u0000KO\u0005"+
		"(\u0000\u0000LN\u0003\u0006\u0003\u0000ML\u0001\u0000\u0000\u0000NQ\u0001"+
		"\u0000\u0000\u0000OM\u0001\u0000\u0000\u0000OP\u0001\u0000\u0000\u0000"+
		"PR\u0001\u0000\u0000\u0000QO\u0001\u0000\u0000\u0000Ro\u0005)\u0000\u0000"+
		"ST\u0005\u0002\u0000\u0000TX\u0005(\u0000\u0000UW\u0003\u000e\u0007\u0000"+
		"VU\u0001\u0000\u0000\u0000WZ\u0001\u0000\u0000\u0000XV\u0001\u0000\u0000"+
		"\u0000XY\u0001\u0000\u0000\u0000Y[\u0001\u0000\u0000\u0000ZX\u0001\u0000"+
		"\u0000\u0000[o\u0005)\u0000\u0000\\]\u0005\u0003\u0000\u0000]a\u0005("+
		"\u0000\u0000^`\u0003\u0010\b\u0000_^\u0001\u0000\u0000\u0000`c\u0001\u0000"+
		"\u0000\u0000a_\u0001\u0000\u0000\u0000ab\u0001\u0000\u0000\u0000bd\u0001"+
		"\u0000\u0000\u0000ca\u0001\u0000\u0000\u0000do\u0005)\u0000\u0000ef\u0005"+
		"\u0004\u0000\u0000fj\u0005(\u0000\u0000gi\u0003\u0014\n\u0000hg\u0001"+
		"\u0000\u0000\u0000il\u0001\u0000\u0000\u0000jh\u0001\u0000\u0000\u0000"+
		"jk\u0001\u0000\u0000\u0000km\u0001\u0000\u0000\u0000lj\u0001\u0000\u0000"+
		"\u0000mo\u0005)\u0000\u0000nJ\u0001\u0000\u0000\u0000nS\u0001\u0000\u0000"+
		"\u0000n\\\u0001\u0000\u0000\u0000ne\u0001\u0000\u0000\u0000o\u0005\u0001"+
		"\u0000\u0000\u0000pr\u0003\b\u0004\u0000qp\u0001\u0000\u0000\u0000ru\u0001"+
		"\u0000\u0000\u0000sq\u0001\u0000\u0000\u0000st\u0001\u0000\u0000\u0000"+
		"tv\u0001\u0000\u0000\u0000us\u0001\u0000\u0000\u0000vw\u0003\n\u0005\u0000"+
		"w|\u0003\f\u0006\u0000xy\u0005-\u0000\u0000y{\u0003\f\u0006\u0000zx\u0001"+
		"\u0000\u0000\u0000{~\u0001\u0000\u0000\u0000|z\u0001\u0000\u0000\u0000"+
		"|}\u0001\u0000\u0000\u0000}\u007f\u0001\u0000\u0000\u0000~|\u0001\u0000"+
		"\u0000\u0000\u007f\u0080\u0005,\u0000\u0000\u0080\u0007\u0001\u0000\u0000"+
		"\u0000\u0081\u0082\u0007\u0000\u0000\u0000\u0082\t\u0001\u0000\u0000\u0000"+
		"\u0083\u0084\u0007\u0001\u0000\u0000\u0084\u000b\u0001\u0000\u0000\u0000"+
		"\u0085\u0088\u0005%\u0000\u0000\u0086\u0087\u00050\u0000\u0000\u0087\u0089"+
		"\u0003(\u0014\u0000\u0088\u0086\u0001\u0000\u0000\u0000\u0088\u0089\u0001"+
		"\u0000\u0000\u0000\u0089\r\u0001\u0000\u0000\u0000\u008a\u008c\u0003\u0002"+
		"\u0001\u0000\u008b\u008a\u0001\u0000\u0000\u0000\u008c\u008f\u0001\u0000"+
		"\u0000\u0000\u008d\u008b\u0001\u0000\u0000\u0000\u008d\u008e\u0001\u0000"+
		"\u0000\u0000\u008e\u0090\u0001\u0000\u0000\u0000\u008f\u008d\u0001\u0000"+
		"\u0000\u0000\u0090\u0093\u0003\"\u0011\u0000\u0091\u0092\u0005\u0011\u0000"+
		"\u0000\u0092\u0094\u0005%\u0000\u0000\u0093\u0091\u0001\u0000\u0000\u0000"+
		"\u0093\u0094\u0001\u0000\u0000\u0000\u0094\u0096\u0001\u0000\u0000\u0000"+
		"\u0095\u0097\u0003\u0012\t\u0000\u0096\u0095\u0001\u0000\u0000\u0000\u0096"+
		"\u0097\u0001\u0000\u0000\u0000\u0097\u0098\u0001\u0000\u0000\u0000\u0098"+
		"\u0099\u0005,\u0000\u0000\u0099\u000f\u0001\u0000\u0000\u0000\u009a\u009c"+
		"\u0003\u0002\u0001\u0000\u009b\u009a\u0001\u0000\u0000\u0000\u009c\u009f"+
		"\u0001\u0000\u0000\u0000\u009d\u009b\u0001\u0000\u0000\u0000\u009d\u009e"+
		"\u0001\u0000\u0000\u0000\u009e\u00a0\u0001\u0000\u0000\u0000\u009f\u009d"+
		"\u0001\u0000\u0000\u0000\u00a0\u00a1\u0003(\u0014\u0000\u00a1\u00a2\u0005"+
		",\u0000\u0000\u00a2\u0011\u0001\u0000\u0000\u0000\u00a3\u00a4\u0005\u0012"+
		"\u0000\u0000\u00a4\u00a5\u0005&\u0000\u0000\u00a5\u00a6\u0003(\u0014\u0000"+
		"\u00a6\u00a7\u0005\'\u0000\u0000\u00a7\u0013\u0001\u0000\u0000\u0000\u00a8"+
		"\u00aa\u0003\u0002\u0001\u0000\u00a9\u00a8\u0001\u0000\u0000\u0000\u00aa"+
		"\u00ad\u0001\u0000\u0000\u0000\u00ab\u00a9\u0001\u0000\u0000\u0000\u00ab"+
		"\u00ac\u0001\u0000\u0000\u0000\u00ac\u00ae\u0001\u0000\u0000\u0000\u00ad"+
		"\u00ab\u0001\u0000\u0000\u0000\u00ae\u00af\u0003\u0016\u000b\u0000\u00af"+
		"\u0015\u0001\u0000\u0000\u0000\u00b0\u00b2\u0003\"\u0011\u0000\u00b1\u00b3"+
		"\u0003\u0012\t\u0000\u00b2\u00b1\u0001\u0000\u0000\u0000\u00b2\u00b3\u0001"+
		"\u0000\u0000\u0000\u00b3\u00b4\u0001\u0000\u0000\u0000\u00b4\u00b5\u0005"+
		",\u0000\u0000\u00b5\u00ec\u0001\u0000\u0000\u0000\u00b6\u00b7\u0003\u001e"+
		"\u000f\u0000\u00b7\u00b8\u0005,\u0000\u0000\u00b8\u00ec\u0001\u0000\u0000"+
		"\u0000\u00b9\u00ba\u0005\u0005\u0000\u0000\u00ba\u00bb\u0005&\u0000\u0000"+
		"\u00bb\u00bc\u0003(\u0014\u0000\u00bc\u00bd\u0005\'\u0000\u0000\u00bd"+
		"\u00c1\u0003\u001c\u000e\u0000\u00be\u00c0\u0003\u0018\f\u0000\u00bf\u00be"+
		"\u0001\u0000\u0000\u0000\u00c0\u00c3\u0001\u0000\u0000\u0000\u00c1\u00bf"+
		"\u0001\u0000\u0000\u0000\u00c1\u00c2\u0001\u0000\u0000\u0000\u00c2\u00c5"+
		"\u0001\u0000\u0000\u0000\u00c3\u00c1\u0001\u0000\u0000\u0000\u00c4\u00c6"+
		"\u0003\u001a\r\u0000\u00c5\u00c4\u0001\u0000\u0000\u0000\u00c5\u00c6\u0001"+
		"\u0000\u0000\u0000\u00c6\u00ec\u0001\u0000\u0000\u0000\u00c7\u00c8\u0005"+
		"\u0007\u0000\u0000\u00c8\u00c9\u0005&\u0000\u0000\u00c9\u00ca\u0003(\u0014"+
		"\u0000\u00ca\u00cb\u0005\'\u0000\u0000\u00cb\u00cc\u0003\u001c\u000e\u0000"+
		"\u00cc\u00ec\u0001\u0000\u0000\u0000\u00cd\u00ce\u0005\b\u0000\u0000\u00ce"+
		"\u00cf\u0003\u001c\u000e\u0000\u00cf\u00d0\u0005\u0007\u0000\u0000\u00d0"+
		"\u00d1\u0005&\u0000\u0000\u00d1\u00d2\u0003(\u0014\u0000\u00d2\u00d3\u0005"+
		"\'\u0000\u0000\u00d3\u00d4\u0005,\u0000\u0000\u00d4\u00ec\u0001\u0000"+
		"\u0000\u0000\u00d5\u00d6\u0005\t\u0000\u0000\u00d6\u00d7\u0005&\u0000"+
		"\u0000\u00d7\u00d8\u0003(\u0014\u0000\u00d8\u00d9\u0005\'\u0000\u0000"+
		"\u00d9\u00da\u0003\u001c\u000e\u0000\u00da\u00ec\u0001\u0000\u0000\u0000"+
		"\u00db\u00dc\u0005\n\u0000\u0000\u00dc\u00dd\u0005&\u0000\u0000\u00dd"+
		"\u00de\u0005%\u0000\u0000\u00de\u00df\u0005-\u0000\u0000\u00df\u00e0\u0005"+
		"%\u0000\u0000\u00e0\u00e1\u0005/\u0000\u0000\u00e1\u00e2\u0003(\u0014"+
		"\u0000\u00e2\u00e3\u0005\'\u0000\u0000\u00e3\u00e4\u0003\u001c\u000e\u0000"+
		"\u00e4\u00ec\u0001\u0000\u0000\u0000\u00e5\u00e6\u0005\u000b\u0000\u0000"+
		"\u00e6\u00ec\u0005,\u0000\u0000\u00e7\u00e8\u0005\f\u0000\u0000\u00e8"+
		"\u00ec\u0005,\u0000\u0000\u00e9\u00ea\u0005\r\u0000\u0000\u00ea\u00ec"+
		"\u0005,\u0000\u0000\u00eb\u00b0\u0001\u0000\u0000\u0000\u00eb\u00b6\u0001"+
		"\u0000\u0000\u0000\u00eb\u00b9\u0001\u0000\u0000\u0000\u00eb\u00c7\u0001"+
		"\u0000\u0000\u0000\u00eb\u00cd\u0001\u0000\u0000\u0000\u00eb\u00d5\u0001"+
		"\u0000\u0000\u0000\u00eb\u00db\u0001\u0000\u0000\u0000\u00eb\u00e5\u0001"+
		"\u0000\u0000\u0000\u00eb\u00e7\u0001\u0000\u0000\u0000\u00eb\u00e9\u0001"+
		"\u0000\u0000\u0000\u00ec\u0017\u0001\u0000\u0000\u0000\u00ed\u00ee\u0005"+
		"\u0006\u0000\u0000\u00ee\u00ef\u0005\u0005\u0000\u0000\u00ef\u00f0\u0005"+
		"&\u0000\u0000\u00f0\u00f1\u0003(\u0014\u0000\u00f1\u00f2\u0005\'\u0000"+
		"\u0000\u00f2\u00f3\u0003\u001c\u000e\u0000\u00f3\u0019\u0001\u0000\u0000"+
		"\u0000\u00f4\u00f5\u0005\u0006\u0000\u0000\u00f5\u00f6\u0003\u001c\u000e"+
		"\u0000\u00f6\u001b\u0001\u0000\u0000\u0000\u00f7\u00fb\u0005(\u0000\u0000"+
		"\u00f8\u00fa\u0003\u0014\n\u0000\u00f9\u00f8\u0001\u0000\u0000\u0000\u00fa"+
		"\u00fd\u0001\u0000\u0000\u0000\u00fb\u00f9\u0001\u0000\u0000\u0000\u00fb"+
		"\u00fc\u0001\u0000\u0000\u0000\u00fc\u00fe\u0001\u0000\u0000\u0000\u00fd"+
		"\u00fb\u0001\u0000\u0000\u0000\u00fe\u00ff\u0005)\u0000\u0000\u00ff\u001d"+
		"\u0001\u0000\u0000\u0000\u0100\u0101\u0003 \u0010\u0000\u0101\u0102\u0007"+
		"\u0002\u0000\u0000\u0102\u0103\u0003(\u0014\u0000\u0103\u0108\u0001\u0000"+
		"\u0000\u0000\u0104\u0105\u0003 \u0010\u0000\u0105\u0106\u0007\u0003\u0000"+
		"\u0000\u0106\u0108\u0001\u0000\u0000\u0000\u0107\u0100\u0001\u0000\u0000"+
		"\u0000\u0107\u0104\u0001\u0000\u0000\u0000\u0108\u001f\u0001\u0000\u0000"+
		"\u0000\u0109\u0110\u0005%\u0000\u0000\u010a\u010b\u0005*\u0000\u0000\u010b"+
		"\u010c\u0003(\u0014\u0000\u010c\u010d\u0005+\u0000\u0000\u010d\u010f\u0001"+
		"\u0000\u0000\u0000\u010e\u010a\u0001\u0000\u0000\u0000\u010f\u0112\u0001"+
		"\u0000\u0000\u0000\u0110\u010e\u0001\u0000\u0000\u0000\u0110\u0111\u0001"+
		"\u0000\u0000\u0000\u0111!\u0001\u0000\u0000\u0000\u0112\u0110\u0001\u0000"+
		"\u0000\u0000\u0113\u0114\u0005%\u0000\u0000\u0114\u0116\u0005&\u0000\u0000"+
		"\u0115\u0117\u0003$\u0012\u0000\u0116\u0115\u0001\u0000\u0000\u0000\u0116"+
		"\u0117\u0001\u0000\u0000\u0000\u0117\u0118\u0001\u0000\u0000\u0000\u0118"+
		"\u0119\u0005\'\u0000\u0000\u0119#\u0001\u0000\u0000\u0000\u011a\u011f"+
		"\u0003&\u0013\u0000\u011b\u011c\u0005-\u0000\u0000\u011c\u011e\u0003&"+
		"\u0013\u0000\u011d\u011b\u0001\u0000\u0000\u0000\u011e\u0121\u0001\u0000"+
		"\u0000\u0000\u011f\u011d\u0001\u0000\u0000\u0000\u011f\u0120\u0001\u0000"+
		"\u0000\u0000\u0120\u0123\u0001\u0000\u0000\u0000\u0121\u011f\u0001\u0000"+
		"\u0000\u0000\u0122\u0124\u0005-\u0000\u0000\u0123\u0122\u0001\u0000\u0000"+
		"\u0000\u0123\u0124\u0001\u0000\u0000\u0000\u0124%\u0001\u0000\u0000\u0000"+
		"\u0125\u0126\u00030\u0018\u0000\u0126\u0127\u0005/\u0000\u0000\u0127\u0129"+
		"\u0001\u0000\u0000\u0000\u0128\u0125\u0001\u0000\u0000\u0000\u0128\u0129"+
		"\u0001\u0000\u0000\u0000\u0129\u012a\u0001\u0000\u0000\u0000\u012a\u012b"+
		"\u0003(\u0014\u0000\u012b\'\u0001\u0000\u0000\u0000\u012c\u012d\u0006"+
		"\u0014\uffff\uffff\u0000\u012d\u0136\u0003*\u0015\u0000\u012e\u012f\u0005"+
		"&\u0000\u0000\u012f\u0130\u0003\n\u0005\u0000\u0130\u0131\u0005\'\u0000"+
		"\u0000\u0131\u0132\u0003(\u0014\b\u0132\u0136\u0001\u0000\u0000\u0000"+
		"\u0133\u0134\u0007\u0004\u0000\u0000\u0134\u0136\u0003(\u0014\u0007\u0135"+
		"\u012c\u0001\u0000\u0000\u0000\u0135\u012e\u0001\u0000\u0000\u0000\u0135"+
		"\u0133\u0001\u0000\u0000\u0000\u0136\u0158\u0001\u0000\u0000\u0000\u0137"+
		"\u0138\n\u0006\u0000\u0000\u0138\u0139\u0007\u0005\u0000\u0000\u0139\u0157"+
		"\u0003(\u0014\u0007\u013a\u013b\n\u0005\u0000\u0000\u013b\u013c\u0007"+
		"\u0006\u0000\u0000\u013c\u0157\u0003(\u0014\u0006\u013d\u013e\n\u0004"+
		"\u0000\u0000\u013e\u013f\u0007\u0007\u0000\u0000\u013f\u0157\u0003(\u0014"+
		"\u0005\u0140\u0141\n\u0003\u0000\u0000\u0141\u0142\u0007\b\u0000\u0000"+
		"\u0142\u0157\u0003(\u0014\u0004\u0143\u0144\n\u0002\u0000\u0000\u0144"+
		"\u0145\u0005>\u0000\u0000\u0145\u0157\u0003(\u0014\u0003\u0146\u0147\n"+
		"\u0001\u0000\u0000\u0147\u0148\u0005?\u0000\u0000\u0148\u0157\u0003(\u0014"+
		"\u0002\u0149\u014a\n\n\u0000\u0000\u014a\u014b\u0005*\u0000\u0000\u014b"+
		"\u014c\u0003(\u0014\u0000\u014c\u014d\u0005+\u0000\u0000\u014d\u0157\u0001"+
		"\u0000\u0000\u0000\u014e\u014f\n\t\u0000\u0000\u014f\u0150\u0005.\u0000"+
		"\u0000\u0150\u0151\u0005%\u0000\u0000\u0151\u0153\u0005&\u0000\u0000\u0152"+
		"\u0154\u0003$\u0012\u0000\u0153\u0152\u0001\u0000\u0000\u0000\u0153\u0154"+
		"\u0001\u0000\u0000\u0000\u0154\u0155\u0001\u0000\u0000\u0000\u0155\u0157"+
		"\u0005\'\u0000\u0000\u0156\u0137\u0001\u0000\u0000\u0000\u0156\u013a\u0001"+
		"\u0000\u0000\u0000\u0156\u013d\u0001\u0000\u0000\u0000\u0156\u0140\u0001"+
		"\u0000\u0000\u0000\u0156\u0143\u0001\u0000\u0000\u0000\u0156\u0146\u0001"+
		"\u0000\u0000\u0000\u0156\u0149\u0001\u0000\u0000\u0000\u0156\u014e\u0001"+
		"\u0000\u0000\u0000\u0157\u015a\u0001\u0000\u0000\u0000\u0158\u0156\u0001"+
		"\u0000\u0000\u0000\u0158\u0159\u0001\u0000\u0000\u0000\u0159)\u0001\u0000"+
		"\u0000\u0000\u015a\u0158\u0001\u0000\u0000\u0000\u015b\u0184\u0003.\u0017"+
		"\u0000\u015c\u0184\u0003\"\u0011\u0000\u015d\u015e\u0005\u001e\u0000\u0000"+
		"\u015e\u015f\u0005.\u0000\u0000\u015f\u0184\u0005%\u0000\u0000\u0160\u0184"+
		"\u0005%\u0000\u0000\u0161\u0162\u0005&\u0000\u0000\u0162\u0163\u0003("+
		"\u0014\u0000\u0163\u0164\u0005\'\u0000\u0000\u0164\u0184\u0001\u0000\u0000"+
		"\u0000\u0165\u0171\u0005*\u0000\u0000\u0166\u016b\u0003(\u0014\u0000\u0167"+
		"\u0168\u0005-\u0000\u0000\u0168\u016a\u0003(\u0014\u0000\u0169\u0167\u0001"+
		"\u0000\u0000\u0000\u016a\u016d\u0001\u0000\u0000\u0000\u016b\u0169\u0001"+
		"\u0000\u0000\u0000\u016b\u016c\u0001\u0000\u0000\u0000\u016c\u016f\u0001"+
		"\u0000\u0000\u0000\u016d\u016b\u0001\u0000\u0000\u0000\u016e\u0170\u0005"+
		"-\u0000\u0000\u016f\u016e\u0001\u0000\u0000\u0000\u016f\u0170\u0001\u0000"+
		"\u0000\u0000\u0170\u0172\u0001\u0000\u0000\u0000\u0171\u0166\u0001\u0000"+
		"\u0000\u0000\u0171\u0172\u0001\u0000\u0000\u0000\u0172\u0173\u0001\u0000"+
		"\u0000\u0000\u0173\u0184\u0005+\u0000\u0000\u0174\u0180\u0005(\u0000\u0000"+
		"\u0175\u017a\u0003,\u0016\u0000\u0176\u0177\u0005-\u0000\u0000\u0177\u0179"+
		"\u0003,\u0016\u0000\u0178\u0176\u0001\u0000\u0000\u0000\u0179\u017c\u0001"+
		"\u0000\u0000\u0000\u017a\u0178\u0001\u0000\u0000\u0000\u017a\u017b\u0001"+
		"\u0000\u0000\u0000\u017b\u017e\u0001\u0000\u0000\u0000\u017c\u017a\u0001"+
		"\u0000\u0000\u0000\u017d\u017f\u0005-\u0000\u0000\u017e\u017d\u0001\u0000"+
		"\u0000\u0000\u017e\u017f\u0001\u0000\u0000\u0000\u017f\u0181\u0001\u0000"+
		"\u0000\u0000\u0180\u0175\u0001\u0000\u0000\u0000\u0180\u0181\u0001\u0000"+
		"\u0000\u0000\u0181\u0182\u0001\u0000\u0000\u0000\u0182\u0184\u0005)\u0000"+
		"\u0000\u0183\u015b\u0001\u0000\u0000\u0000\u0183\u015c\u0001\u0000\u0000"+
		"\u0000\u0183\u015d\u0001\u0000\u0000\u0000\u0183\u0160\u0001\u0000\u0000"+
		"\u0000\u0183\u0161\u0001\u0000\u0000\u0000\u0183\u0165\u0001\u0000\u0000"+
		"\u0000\u0183\u0174\u0001\u0000\u0000\u0000\u0184+\u0001\u0000\u0000\u0000"+
		"\u0185\u0188\u00030\u0018\u0000\u0186\u0188\u0005$\u0000\u0000\u0187\u0185"+
		"\u0001\u0000\u0000\u0000\u0187\u0186\u0001\u0000\u0000\u0000\u0188\u0189"+
		"\u0001\u0000\u0000\u0000\u0189\u018a\u0005/\u0000\u0000\u018a\u018b\u0003"+
		"(\u0014\u0000\u018b-\u0001\u0000\u0000\u0000\u018c\u018d\u0007\t\u0000"+
		"\u0000\u018d/\u0001\u0000\u0000\u0000\u018e\u018f\u0007\n\u0000\u0000"+
		"\u018f1\u0001\u0000\u0000\u0000\u0190\u0191\u0003(\u0014\u0000\u0191\u0192"+
		"\u0005\u0000\u0000\u0001\u01923\u0001\u0000\u0000\u0000(7=EHOXajns|\u0088"+
		"\u008d\u0093\u0096\u009d\u00ab\u00b2\u00c1\u00c5\u00eb\u00fb\u0107\u0110"+
		"\u0116\u011f\u0123\u0128\u0135\u0153\u0156\u0158\u016b\u016f\u0171\u017a"+
		"\u017e\u0180\u0183\u0187";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}