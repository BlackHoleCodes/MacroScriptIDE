package com.blackholecodes.macroscript.compiler;

import com.blackholecodes.macroscript.ast.Block;
import com.blackholecodes.macroscript.ast.ConstraintStatement;
import com.blackholecodes.macroscript.ast.MacroFile;
import com.blackholecodes.macroscript.diagnostics.SourceRange;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

/** Compiles one parsed file to a MacroDroid macro object (the part inside the export wrapper). */
final class MacroCompiler {

    private final Compilation compilation;

    MacroCompiler(Compilation compilation) {
        this.compilation = compilation;
    }

    ObjectNode compile(MacroFile file, String defaultName) {
        MacroSettings settings = new AnnotationCompiler(compilation).compile(file.annotations(), defaultName);
        checkBlocks(file, settings.actionBlock());

        ArrayNode localVariables = new VariableCompiler(compilation).compile(file.declarations(), settings.actionBlock());
        if (file.triggers() != null) {
            compilation.triggers().registerLabels(file.triggers().items());
        }
        List<ObjectNode> triggers = file.triggers() == null ? List.of()
                : compilation.triggers().compileBlock(file.triggers().items());
        ConstraintList constraints = compileConstraints(file.constraints());
        List<ObjectNode> actions = file.actions() == null ? List.of()
                : compilation.actions().compileBlock(file.actions().items());

        ObjectNode macro = JsonNodeFactory.instance.objectNode();
        macro.put("m_name", settings.name());
        macro.put("m_description", settings.description());
        macro.put("m_category", settings.category());
        macro.put("m_enabled", settings.enabled());
        macro.put("m_completed", true);
        macro.put("m_GUID", 0L);
        macro.put("aiGenerated", 1);
        if (settings.favourite()) {
            macro.put("isFavourite", true);
        }
        if (settings.headingColor() != null) {
            macro.put("m_headingColor", settings.headingColor());
        }
        if (settings.noLog()) {
            macro.put("m_excludeLog", true);
        }
        if (settings.logLevel() != 0) {
            macro.put("loggingLevel", settings.logLevel());
        }
        if (settings.actionBlock()) {
            macro.put("isActionBlock", true);
        }
        if (constraints.isOr()) {
            macro.put("m_isOrCondition", true);
        }
        macro.set("m_triggerList", toArray(triggers));
        macro.set("m_actionList", toArray(actions));
        macro.set("m_constraintList", constraints.toJson());
        macro.set("localVariables", localVariables);
        return macro;
    }

    private void checkBlocks(MacroFile file, boolean actionBlock) {
        if (actionBlock) {
            rejectBlock(file.triggers(), "An action block cannot have a triggers block. Remove it, or remove @actionBlock.");
            rejectBlock(file.constraints(), "An action block cannot have a constraints block. Remove it, or remove @actionBlock.");
            if (file.actions() == null) {
                compilation.error(SourceRange.NONE, "An action block needs an actions block.");
            }
            return;
        }
        if (file.triggers() == null) {
            compilation.error(SourceRange.NONE, "A macro needs a triggers block. For a macro you start by hand, "
                    + "write: triggers { manual(); }");
        } else if (file.triggers().items().isEmpty()) {
            compilation.error(file.triggers().range(), "The triggers block needs at least one trigger. For a macro you "
                    + "start by hand, write manual();");
        }
        if (file.actions() == null) {
            compilation.error(SourceRange.NONE, "A macro needs an actions block.");
        }
    }

    private void rejectBlock(Block<?> block, String message) {
        if (block != null) {
            compilation.error(block.range(), message);
        }
    }

    private ConstraintList compileConstraints(Block<ConstraintStatement> block) {
        if (block == null || block.items().isEmpty()) {
            return ConstraintList.EMPTY;
        }
        return compilation.conditions().compileBlock(block.items()).orElse(ConstraintList.EMPTY);
    }

    private static ArrayNode toArray(List<ObjectNode> items) {
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        items.forEach(array::add);
        return array;
    }
}
