package com.blackholecodes.macroscript.ide;

import java.awt.Rectangle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;

/** The settings of the IDE that are kept between runs, in the user preferences of the system. */
final class IdeSettings {

    private static final int MAX_RECENT_PROJECTS = 10;
    private static final String DARK_THEME = "darkTheme";
    private static final String FONT_SIZE = "fontSize";
    private static final String RECENT_PROJECT = "recentProject.";
    private static final String WINDOW = "window.";
    private static final int DEFAULT_FONT_SIZE = 14;

    private final Preferences preferences = Preferences.userNodeForPackage(IdeSettings.class);

    boolean darkTheme() {
        return preferences.getBoolean(DARK_THEME, false);
    }

    void setDarkTheme(boolean dark) {
        preferences.putBoolean(DARK_THEME, dark);
    }

    int fontSize() {
        return preferences.getInt(FONT_SIZE, DEFAULT_FONT_SIZE);
    }

    void setFontSize(int size) {
        preferences.putInt(FONT_SIZE, size);
    }

    /** The projects opened most recently, newest first. Folders that no longer exist are left out. */
    List<Path> recentProjects() {
        List<Path> projects = new ArrayList<>();
        for (int i = 0; i < MAX_RECENT_PROJECTS; i++) {
            String value = preferences.get(RECENT_PROJECT + i, null);
            if (value != null && Files.isDirectory(Path.of(value))) {
                projects.add(Path.of(value));
            }
        }
        return projects;
    }

    void addRecentProject(Path root) {
        List<Path> projects = recentProjects();
        projects.remove(root);
        projects.add(0, root);
        for (int i = 0; i < MAX_RECENT_PROJECTS; i++) {
            if (i < projects.size()) {
                preferences.put(RECENT_PROJECT + i, projects.get(i).toString());
            } else {
                preferences.remove(RECENT_PROJECT + i);
            }
        }
    }

    Optional<Path> lastProject() {
        List<Path> projects = recentProjects();
        return projects.isEmpty() ? Optional.empty() : Optional.of(projects.get(0));
    }

    Optional<Rectangle> windowBounds() {
        int width = preferences.getInt(WINDOW + "width", -1);
        if (width <= 0) {
            return Optional.empty();
        }
        return Optional.of(new Rectangle(preferences.getInt(WINDOW + "x", 0), preferences.getInt(WINDOW + "y", 0),
                width, preferences.getInt(WINDOW + "height", 700)));
    }

    void setWindowBounds(Rectangle bounds) {
        preferences.putInt(WINDOW + "x", bounds.x);
        preferences.putInt(WINDOW + "y", bounds.y);
        preferences.putInt(WINDOW + "width", bounds.width);
        preferences.putInt(WINDOW + "height", bounds.height);
    }
}
