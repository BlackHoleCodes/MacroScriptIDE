package com.blackholecodes.macroscript.ide.editor;

import com.blackholecodes.macroscript.mapping.CommandCategory;
import com.blackholecodes.macroscript.mapping.CommandSpec;
import com.blackholecodes.macroscript.mapping.Mapping;
import com.blackholecodes.macroscript.mapping.ParamSpec;
import com.blackholecodes.macroscript.mapping.SystemValue;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.KeyStroke;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Code completion with Ctrl+Space: keywords, annotations, helpers, every command of the
 * mapping and every sys value. Each command shows its arguments and description.
 */
public final class CompletionSetup {

    private static final List<String> FILE_ANNOTATIONS = List.of("@name(\"\")", "@description(\"\")", "@category(\"\")",
            "@enabled(false)", "@favourite", "@headingColor(#FF2196F3)", "@noLog", "@logLevel(TRIGGERS_ONLY)",
            "@actionBlock", "@disabled", "@comment(\"\")");
    private static final List<String[]> HELPERS = List.of(
            new String[] {"manual()", "A trigger for a macro that you start by hand."},
            new String[] {"triggeredBy(", "Condition: true when the labeled trigger started the macro."},
            new String[] {"confirm(", "Condition of an if: asks the user. confirm(title:, text:, yes:, no:, timeout:)"},
            new String[] {"xor(", "Condition: exactly one of the conditions is true."},
            new String[] {"wait(", "Waits: wait(2s), wait(500ms), wait(count, MINUTES)."},
            new String[] {"waitFor(", "Waits for a trigger: waitFor(shakeDevice(), timeout: 30s)."},
            new String[] {"callBlock(", "Runs an action block: callBlock(\"Name\", inputs: {...}, outputs: {...})."},
            new String[] {"block(", "An action block for a button: block(\"Name\", inputs: {...})."},
            new String[] {"random(", "A random number: x = random(1, 10);"},
            new String[] {"prompt(", "Asks the user for a value: s = prompt(title: \"...\", message: \"...\");"},
            new String[] {"contact(", "A contact: contact(\"Mom\") or contact(\"Mom\", \"+15551234567\")."},
            new String[] {"app(", "An app: app(\"com.whatsapp\", \"WhatsApp\")."},
            new String[] {"raw(", "Any MacroDroid item: raw(\"ClassName\", {\"m_field\": 1})."});

    private CompletionSetup() {
    }

    /** Builds the completion list once; it can be shared by all editors. */
    public static CompletionProvider createProvider(Mapping mapping) {
        DefaultCompletionProvider provider = new DefaultCompletionProvider() {
            @Override
            protected boolean isValidChar(char ch) {
                return Character.isLetterOrDigit(ch) || ch == '_' || ch == '.' || ch == '@';
            }
        };
        for (String keyword : Keywords.all()) {
            provider.addCompletion(new BasicCompletion(provider, keyword, "keyword"));
        }
        for (String annotation : FILE_ANNOTATIONS) {
            provider.addCompletion(new BasicCompletion(provider, annotation, "annotation"));
        }
        for (String[] helper : HELPERS) {
            provider.addCompletion(new BasicCompletion(provider, helper[0], "helper", helper[1]));
        }
        for (CommandCategory category : CommandCategory.values()) {
            for (CommandSpec command : mapping.commands(category)) {
                if (command.status().isCallable()) {
                    provider.addCompletion(new BasicCompletion(provider, command.name() + "(",
                            category.displayName(), summary(command)));
                }
            }
        }
        for (SystemValue value : mapping.systemValues()) {
            provider.addCompletion(new BasicCompletion(provider, "sys." + value.name(), "sys value",
                    value.magicText() + " (" + value.source() + ")"));
        }
        return provider;
    }

    public static void install(RSyntaxTextArea textArea, CompletionProvider provider) {
        AutoCompletion completion = new AutoCompletion(provider);
        completion.setShowDescWindow(true);
        completion.setAutoCompleteSingleChoices(false);
        completion.setTriggerKey(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_DOWN_MASK));
        completion.install(textArea);
    }

    /** The HTML shown next to a command in the completion list. */
    private static String summary(CommandSpec command) {
        StringBuilder html = new StringBuilder("<b>").append(escape(command.name())).append("</b> &nbsp;<i>")
                .append(command.classType()).append("</i><br>").append(escape(command.description())).append("<br><br>");
        for (ParamSpec param : command.params()) {
            if (!param.isWrittenByUser()) {
                continue;
            }
            html.append(param.required() ? "<b>" : "").append(escape(param.name())).append(param.required() ? "</b>" : "?")
                    .append(param.positional() ? " (can be unnamed)" : "")
                    .append(": ").append(param.kind().name().toLowerCase(java.util.Locale.ROOT));
            if (!param.constants().isEmpty() && param.constants().size() <= 12) {
                html.append(" — ").append(String.join(", ", param.constants().stream().map(c -> c.name()).toList()));
            }
            html.append("<br>");
        }
        return html.toString();
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
