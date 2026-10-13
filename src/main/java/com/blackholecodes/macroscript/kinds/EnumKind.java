package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.compiler.Suggestions;
import com.blackholecodes.macroscript.decompiler.ValueContext;
import com.blackholecodes.macroscript.mapping.ConstantSpec;
import com.blackholecodes.macroscript.mapping.ParamSpec;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Optional;

/** A constant of the parameter, for example {@code TURN_ON}. A plain int is also accepted. */
final class EnumKind implements ParamKindHandler {

    /** Lists at most this many constants in an error message. */
    private static final int MAX_LISTED = 12;

    @Override
    public void compile(ArgumentContext argument) {
        ParamSpec param = argument.param();
        switch (argument.value()) {
            case Expr.Name name -> {
                Optional<ConstantSpec> constant = param.constant(name.name());
                if (constant.isPresent()) {
                    argument.write(constant.get().value());
                } else {
                    List<String> names = constantNames(param);
                    argument.error("has no constant " + name.name() + "." + Suggestions.didYouMean(name.name(), names)
                            + " Use one of: " + listed(names) + ".");
                }
            }
            case Expr.IntLiteral number -> argument.write(JsonValues.number(number.value()));
            default -> argument.error("needs one of: " + listed(constantNames(param)) + ".");
        }
    }

    static List<String> constantNames(ParamSpec param) {
        return param.constants().stream().map(ConstantSpec::name).toList();
    }

    static String listed(List<String> names) {
        if (names.size() <= MAX_LISTED) {
            return String.join(", ", names);
        }
        return String.join(", ", names.subList(0, MAX_LISTED)) + " ... (" + names.size() + " in all)";
    }

    @Override
    public Optional<String> decompile(ValueContext context) {
        JsonNode value = context.value();
        Optional<ConstantSpec> constant = context.param().constantWithValue(value);
        if (constant.isPresent()) {
            return Optional.of(constant.get().name());
        }
        if (value.isIntegralNumber()) {
            return Optional.of(value.asText());
        }
        throw context.unrepresentable("the value " + value + " has no constant");
    }
}
