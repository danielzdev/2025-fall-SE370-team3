package csusm.cougarplanner.theme;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Centralized theme switcher. Each theme corresponds to a CSS file under
 * {@code resources/csusm/cougarplanner/themes/theme-<name>.css}; applying a
 * theme means swapping that stylesheet onto a scene (or all open stages).
 * <p>
 * {@link #resolve(String)} is forgiving — null, wrong case, or unknown names
 * all fall back to {@link #COUGAR} so bad values in a saved profile can't
 * leave the user stuck with a broken-looking window.
 */
public final class ThemeManager {
    public static final String COUGAR = "cougar";
    public static final String DARK = "dark";
    public static final String SUNSET = "sunset";

    private static final List<String> VALID = Arrays.asList(COUGAR, DARK, SUNSET);
    // Placeholder gets replaced with the resolved theme name to build the classpath URL.
    private static final String STYLESHEET_PATH_FMT = "/csusm/cougarplanner/themes/theme-%s.css";

    private static String currentTheme = COUGAR;

    private ThemeManager() {
    }

    /**
     * Normalizes a user-supplied theme name to a known theme id. Returns the
     * default ({@link #COUGAR}) for null, unknown, or mis-cased inputs rather
     * than throwing, since theme names come from saved profiles and bad data
     * shouldn't break the UI.
     */
    public static String resolve(String name) {
        if (name == null)
            return COUGAR;
        String lower = name.toLowerCase();
        return VALID.contains(lower) ? lower : COUGAR;
    }

    public static String getCurrentTheme() {
        return currentTheme;
    }

    /**
     * Swaps the theme stylesheet on a scene. Any previously attached theme
     * stylesheet is removed first so themes don't stack; non-theme stylesheets
     * (component-specific CSS, etc.) are left alone.
     */
    public static void apply(Scene scene, String themeName) {
        if (scene == null)
            return;
        String resolved = resolve(themeName);
        currentTheme = resolved;
        URL url = ThemeManager.class.getResource(String.format(STYLESHEET_PATH_FMT, resolved));
        if (url == null)
            return;
        String href = url.toExternalForm();

        // Remove any currently-attached theme sheet so we don't end up with two active themes.
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
