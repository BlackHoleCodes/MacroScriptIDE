package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.decompiler.StringLiterals;
import com.blackholecodes.macroscript.decompiler.ValueContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Optional;

/**
 * A contact: {@code contact("Mom")} or {@code contact("Mom", "+15551234567")}. The compiler
 * writes a Contact object with placeholder IDs that MacroDroid resolves on the phone.
 */
final class ContactKind implements ParamKindHandler {

    private static final String EXAMPLE = "contact(\"Mom\") or contact(\"Mom\", \"+15551234567\")";

    @Override
    public void compile(ArgumentContext argument) {
        read(argument, argument.value()).ifPresent(argument::write);
    }

    static Optional<ObjectNode> read(ArgumentContext argument, Expr value) {
        if (!(value instanceof Expr.Call call) || !call.name().equals("contact")) {
            argument.error(value, "needs " + EXAMPLE + ".");
            return Optional.empty();
        }
        Optional<List<String>> strings = ValueHelpers.strings(argument, call, 2, EXAMPLE);
        return strings.map(s -> {
            ObjectNode contact = argument.json().objectNode();
            contact.put("m_id", "-1");
            contact.put("m_lookupKey", "-1");
            contact.put("m_name", s.get(0));
            contact.put("m_number", s.size() > 1 ? s.get(1) : "");
            return contact;
        });
    }

    @Override
    public Optional<String> decompile(ValueContext context) {
        JsonNode value = context.value();
        return Optional.of(source(context, value));
    }

    static String source(ValueContext context, JsonNode contact) {
        if (!contact.isObject() || !contact.path("m_name").isTextual()) {
            throw context.unrepresentable("expected a contact object");
        }
        String number = contact.path("m_number").asText("");
        String name = StringLiterals.quote(contact.path("m_name").asText());
        return number.isEmpty() ? "contact(" + name + ")" : "contact(" + name + ", " + StringLiterals.quote(number) + ")";
    }
}
