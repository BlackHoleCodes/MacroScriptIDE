package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.decompiler.ValueContext;
import com.blackholecodes.macroscript.mapping.ConstantSpec;
import com.blackholecodes.macroscript.mapping.ParamSpec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A set of constants, written as a list ({@code [MON, FRI]}), a day group
 * ({@code WEEKDAYS}) or one constant. Days of the month can be ints: {@code [1, 15]}.
 * The compiler writes a bool array where the constant's value is the index.
 */
final class FlagsKind implements ParamKindHandler {

    @Override
    public void compile(ArgumentContext argument) {
        ParamSpec param = argument.param();
        boolean[] flags = new boolean[length(param)];
        boolean ok = true;
        for (Expr element : elements(argument)) {
            Optional<ConstantSpec> constant = constantFor(argument, element);
            if (constant.isPresent()) {
                flags[constant.get().value().asInt()] = true;
            } else {
                ok = false;
            }
        }
        if (ok) {
            ArrayNode array = argument.json().arrayNode();
            for (boolean flag : flags) {
                array.add(flag);
            }
            argument.write(array);
        }
    }

    /** The array length: from the mapping, or one more than the largest constant value. */
    static int length(ParamSpec param) {
        if (param.length() > 0) {
            return param.length();
        }
        return param.constants().stream().mapToInt(c -> c.value().asInt()).max().orElse(-1) + 1;
    }

    /** The constants the value names, with day groups expanded. */
    private static List<Expr> elements(ArgumentContext argument) {
        Expr value = argument.value();
        List<Expr> listed = value instanceof Expr.ListLiteral list ? list.elements() : List.of(value);
        List<Expr> expanded = new ArrayList<>();
        for (Expr element : listed) {
            Optional<List<String>> group = element instanceof Expr.Name name
                    ? argument.compilation().mapping().dayGroup(name.name())
                    : Optional.empty();
            if (group.isPresent()) {
                group.get().forEach(day -> expanded.add(new Expr.Name(day, element.range())));
            } else {
                expanded.add(element);
            }
        }
        return expanded;
    }

    private static Optional<ConstantSpec> constantFor(ArgumentContext argument, Expr element) {
        ParamSpec param = argument.param();
        Optional<ConstantSpec> constant = switch (element) {
            case Expr.Name name -> param.constant(name.name());
            // Constants for numbers are named with an underscore, for example _15.
            case Expr.IntLiteral number -> param.constant("_" + number.value());
            default -> Optional.empty();
        };
        if (constant.isEmpty()) {
            String groups = param.constant("MON").isPresent() ? " or a day group (WEEKDAYS, WEEKENDS, EVERY_DAY)" : "";
            argument.error(element, "needs a list of: " + EnumKind.listed(EnumKind.constantNames(param)) + groups + ".");
        }
        return constant;
    }

    /** Day groups to try, the largest first. */
    private static final List<String> DAY_GROUPS = List.of("EVERY_DAY", "WEEKDAYS", "WEEKENDS");

    @Override
    public Optional<String> decompile(ValueContext context) {
        JsonNode value = context.value();
        if (!value.isArray()) {
            throw context.unrepresentable("expected a list of flags");
        }
        ParamSpec param = context.param();
        List<String> names = new ArrayList<>();
        for (int i = 0; i < value.size(); i++) {
            if (!value.get(i).asBoolean()) {
                continue;
            }
            int index = i;
            ConstantSpec constant = param.constants().stream().filter(c -> c.value().asInt() == index).findFirst()
                    .orElseThrow(() -> context.unrepresentable("flag " + index + " has no constant"));
            // Constants for numbers are named with an underscore; write the number itself.
            names.add(constant.name().matches("_\\d+") ? constant.name().substring(1) : constant.name());
        }
        Set<String> chosen = new HashSet<>(names);
        Map<String, List<String>> groups = context.decompilation().mapping().dayGroups();
        if (param.constant("MON").isPresent()) {
            for (String group : DAY_GROUPS) {
                List<String> days = groups.get(group);
                if (days != null && chosen.equals(new HashSet<>(days))) {
                    return Optional.of(group);
                }
            }
        }
        return Optional.of("[" + String.join(", ", names) + "]");
    }
}
