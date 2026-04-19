package csusm.cougarplanner.theme;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class ThemeManager {
    public static final String COUGAR = "cougar";
    public static final String DARK = "dark";
    public static final String SUNSET = "sunset";

    private static final List<String> VALID = Arrays.asList(COUGAR, DARK, SUNSET);
    private static final String STYLESHEET_PATH_FMT = "/csusm/cougarplanner/themes/theme-%s.css";

    private static String currentTheme = COUGAR;

    private ThemeManager() {
    }

    public static String resolve(String name) {
        if (name == null)
            return COUGAR;
        String lower = name.toLowerCase();
        return VALID.contains(lower) ? lower : COUGAR;
    }

    public static String getCurrentTheme() {
        return currentTheme;
    }

    public static void apply(Scene scene, String themeName) {
        if (scene == null)
            return;
        String resolved = resolve(themeName);
        currentTheme = resolved;
        URL url = ThemeManager.class.getResource(String.format(STYLESHEET_PATH_FMT, resolved));
        if (url == null)
            return;
        String href = url.toExternalForm();

        scene.getStylesheets().removeIf(ThemeManager::isThemeStylesheet);
        scene.getStylesheets().add(href);
    }

    public static void applyToAllOpenWindows(String themeName) {
        String resolved = resolve(themeName);
        for (Window w : Window.getWindows()) {
            if (w instanceof Stage stage && stage.getScene() != null) {
                apply(stage.getScene(), resolved);
            }
        }
    }

    private static boolean isThemeStylesheet(String href) {
        return href != null && href.contains("/themes/theme-");
    }
}
