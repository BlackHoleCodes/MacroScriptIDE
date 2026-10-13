package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.ast.Expr;
import com.blackholecodes.macroscript.decompiler.ValueContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** A list of contacts: {@code [contact("Mom"), contact("Dad")]}. */
final class ContactListKind implements ParamKindHandler {

    @Override
    public void compile(ArgumentContext argument) {
        ArrayNode array = argument.json().arrayNode();
        for (Expr element : ValueHelpers.elements(argument.value())) {
            Optional<ObjectNode> contact = ContactKind.read(argument, element);
            if (contact.isEmpty()) {
                return;
            }
            array.add(contact.get());
        }
        argument.write(array);
    }

    @Override
    public Optional<String> decompile(ValueContext context) {
        JsonNode value = context.value();
        List<String> contacts = new ArrayList<>();
        value.forEach(contact -> contacts.add(ContactKind.source(context, contact)));
        return Optional.of("[" + String.join(", ", contacts) + "]");
    }
}
