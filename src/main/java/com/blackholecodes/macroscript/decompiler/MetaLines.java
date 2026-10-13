package com.blackholecodes.macroscript.decompiler;

import com.fasterxml.jackson.databind.JsonNode;

/** Writes the comment and {@code @disabled} of an item on the lines before its statement. */
final class MetaLines {

    private MetaLines() {
    }

    static void write(JsonNode item, SourceWriter out) {
        String comment = item.path("m_comment").asText("");
        if (!comment.isBlank()) {
            if (comment.contains("*/")) {
                out.line("@comment(" + StringLiterals.quote(comment) + ")");
            } else if (comment.contains("\n")) {
                out.line("/**");
                for (String line : comment.split("\n")) {
                    out.line(" * " + line);
                }
                out.line(" */");
            } else {
                out.line("/** " + comment + " */");
            }
        }
        if (item.path("m_isDisabled").asBoolean()) {
            out.line("@disabled");
        }
    }
}
