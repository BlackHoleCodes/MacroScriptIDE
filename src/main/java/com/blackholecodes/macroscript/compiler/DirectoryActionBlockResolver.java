package com.blackholecodes.macroscript.compiler;

import com.blackholecodes.macroscript.ast.MacroFile;
import com.blackholecodes.macroscript.parser.SourceParser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Finds action blocks in the {@code .ms} files under a folder, normally the project folder.
 *
 * <p>The IDE passes a function that gives the text of files that are open in an editor, so
 * that changes that are not saved yet are used.
 */
public final class DirectoryActionBlockResolver implements ActionBlockResolver {

    private final Path root;
    private final Function<Path, Optional<String>> openFileText;

    public DirectoryActionBlockResolver(Path root) {
        this(root, path -> Optional.empty());
    }

    /**
     * @param openFileText gives the editor text of a file, or empty to read it from disk
     */
    public DirectoryActionBlockResolver(Path root, Function<Path, Optional<String>> openFileText) {
        this.root = root;
        this.openFileText = openFileText;
    }

    @Override
    public Optional<SourceUnit> find(String name) {
        for (Path file : sourceFiles()) {
            Optional<SourceUnit> unit = read(file);
            if (unit.isEmpty()) {
                continue;
            }
            MacroFile parsed = SourceParser.parse(unit.get().text()).file();
            if (parsed.isActionBlock() && name.equals(parsed.declaredName().orElse(unit.get().baseName()))) {
                return unit;
            }
        }
        return Optional.empty();
    }

    private List<Path> sourceFiles() {
        if (root == null || !Files.isDirectory(root)) {
            return List.of();
        }
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(p -> p.toString().endsWith(".ms") && Files.isRegularFile(p)).sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not list the files in " + root, e);
        }
    }

    private Optional<SourceUnit> read(Path file) {
        Optional<String> open = openFileText.apply(file);
        if (open.isPresent()) {
            return Optional.of(new SourceUnit(file, open.get()));
        }
        try {
            return Optional.of(new SourceUnit(file, Files.readString(file)));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
