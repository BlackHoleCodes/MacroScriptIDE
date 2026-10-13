package com.blackholecodes.macroscript;

import com.blackholecodes.macroscript.compiler.ActionBlockResolver;
import com.blackholecodes.macroscript.compiler.CompileResult;
import com.blackholecodes.macroscript.compiler.Compiler;
import com.blackholecodes.macroscript.compiler.SourceUnit;
import com.blackholecodes.macroscript.decompiler.DecompileResult;
import com.blackholecodes.macroscript.decompiler.Decompiler;
import com.blackholecodes.macroscript.diagnostics.Diagnostic;
import com.blackholecodes.macroscript.mapping.CommandCategory;
import com.blackholecodes.macroscript.mapping.CommandSpec;
import com.blackholecodes.macroscript.mapping.Mapping;
import com.blackholecodes.macroscript.mapping.MappingLoader;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Every callable command of the mapping, called with sample values for its required
 * arguments, compiles, decompiles, and compiles again to the same JSON.
 */
class AllCommandsTest {

    private static final Pattern MISSING_ARGUMENT = Pattern.compile("needs the argument '(\\w+)'");

    private final Mapping mapping = MappingLoader.loadDefault();
    private final Compiler compiler = new Compiler(mapping);
    private final ActionBlockResolver blocks = name -> name.equals("Sample Block")
            ? Optional.of(new SourceUnit(null, SampleArguments.SAMPLE_BLOCK))
            : Optional.empty();

    @TestFactory
    List<DynamicTest> everyCommandCompilesAndRoundTrips() {
        List<DynamicTest> tests = new ArrayList<>();
        for (CommandCategory category : CommandCategory.values()) {
            for (CommandSpec command : mapping.commands(category)) {
                if (SampleArguments.isCallable(command)) {
                    tests.add(DynamicTest.dynamicTest(category.displayName() + " " + command.name(), () -> check(command)));
                }
            }
        }
        return tests;
    }

    private void check(CommandSpec command) {
        String source = SampleArguments.file(command);
        CompileResult result = compiler.compile(new SourceUnit(null, source), blocks);
        // Some arguments are needed only when another argument has a value. Add those.
        for (int attempt = 0; attempt < 3 && !result.diagnostics().isEmpty(); attempt++) {
            String call = SampleArguments.call(command);
            String extended = call;
            for (Diagnostic diagnostic : result.diagnostics()) {
                Matcher missing = MISSING_ARGUMENT.matcher(diagnostic.message());
                if (missing.find() && !extended.contains(missing.group(1) + ":")) {
                    String value = SampleArguments.sample(command.param(missing.group(1)).orElseThrow());
                    extended = extended.substring(0, extended.length() - 1)
                            + (extended.endsWith("()") ? "" : ", ") + missing.group(1) + ": " + value + ")";
                }
            }
            source = source.replace(call, extended);
            result = compiler.compile(new SourceUnit(null, source), blocks);
            if (extended.equals(call)) {
                break;
            }
        }
        if (result.hasErrors()) {
            fail("Does not compile:\n" + source + "\n" + TestSupport.describe(result));
        }
        DecompileResult decompiled = new Decompiler(mapping).decompile(result.output());
        CompileResult again = compiler.compile(new SourceUnit(null, decompiled.main().source()), blocks);
        if (again.hasErrors()) {
            fail("The decompiled source does not compile:\n" + decompiled.main().source() + "\n" + TestSupport.describe(again));
        }
        assertEquals(TestSupport.withoutPlaceholderIds(result.output()), TestSupport.withoutPlaceholderIds(again.output()),
                "Round trip changed the macro:\n" + decompiled.main().source());
    }
}
