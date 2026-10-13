package com.blackholecodes.macroscript.ide;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * A project: a folder on disk that holds {@code .ms} files. Like an IntelliJ project, it
 * keeps its own settings in a hidden folder, here {@code .macroscript/project.properties}.
 */
public final class Project {

    /** The folder inside the project that holds the project settings. */
    public static final String SETTINGS_FOLDER = ".macroscript";
    private static final String SETTINGS_FILE = "project.properties";
    private static final String OUTPUT_FOLDER = "outputFolder";
    private static final String OPEN_FILES = "openFiles";
    private static final String DEFAULT_OUTPUT_FOLDER = "out";

    private final Path root;
    private final Properties settings = new Properties();

    private Project(Path root) {
        this.root = root.toAbsolutePath().normalize();
    }

    /** Opens a folder as a project. The settings folder is created when it does not exist. */
    public static Project open(Path root) throws IOException {
        Project project = new Project(root);
        Path settingsFile = project.settingsFile();
        if (Files.exists(settingsFile)) {
            try (Reader reader = Files.newBufferedReader(settingsFile)) {
                project.settings.load(reader);
            }
        } else {
            project.save();
        }
        return project;
    }

    public Path root() {
        return root;
    }

    public String name() {
        Path fileName = root.getFileName();
        return fileName == null ? root.toString() : fileName.toString();
    }

    /** The folder that compiled JSON files are written to. */
    public Path outputFolder() {
        return root.resolve(settings.getProperty(OUTPUT_FOLDER, DEFAULT_OUTPUT_FOLDER));
    }

    /** The files that were open in the editor when the project was last closed. */
    public List<Path> openFiles() {
        List<Path> files = new ArrayList<>();
        for (String relative : settings.getProperty(OPEN_FILES, "").split("\\|")) {
            if (!relative.isBlank() && Files.isRegularFile(root.resolve(relative))) {
                files.add(root.resolve(relative));
            }
        }
        return files;
    }

    public void setOpenFiles(List<Path> files) {
        List<String> relative = new ArrayList<>();
        for (Path file : files) {
            if (file.startsWith(root)) {
                relative.add(root.relativize(file).toString().replace('\\', '/'));
            }
        }
        settings.setProperty(OPEN_FILES, String.join("|", relative));
    }

    public boolean contains(Path file) {
        return file.toAbsolutePath().normalize().startsWith(root);
    }

    public void save() throws IOException {
        Files.createDirectories(root.resolve(SETTINGS_FOLDER));
        try (Writer writer = Files.newBufferedWriter(settingsFile())) {
            settings.store(writer, "MacroScript project settings");
        }
    }

    private Path settingsFile() {
        return root.resolve(SETTINGS_FOLDER).resolve(SETTINGS_FILE);
    }
}
