package com.blackholecodes.macroscript.mapping;

/** The three kinds of MacroDroid item. Each one has its own block and its own name space. */
public enum CommandCategory {
    TRIGGER("trigger", "triggers"),
    CONSTRAINT("constraint", "constraints"),
    ACTION("action", "actions");

    private final String jsonName;
    private final String sectionName;

    CommandCategory(String jsonName, String sectionName) {
        this.jsonName = jsonName;
        this.sectionName = sectionName;
    }

    /** The section of the mapping file that lists the commands of this category. */
    public String sectionName() {
        return sectionName;
    }

    /** The word used for this category in messages, for example "trigger". */
    public String displayName() {
        return jsonName;
    }
}
