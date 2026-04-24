package csusm.cougarplanner;

import csusm.cougarplanner.config.Profile;
import csusm.cougarplanner.config.ProfileReader;
import csusm.cougarplanner.theme.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.nio.file.Path;

/**
 * JavaFX application entry point. On startup, reads the saved profile to
 * decide whether to open the login screen or jump straight into the main
 * weekly view, then hands off all subsequent scene swaps to
 * {@link #loadScene(String, String, boolean)}.
 */
public class Launcher extends Application {

    private static Stage primaryStage;
    private static Profile profileConfig;
    private static final Path PROFILE_PATH = Path.of("data/profile.properties");

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        // Load profile
        ProfileReader reader = new ProfileReader(PROFILE_PATH);
        ProfileReader.ReadResult result = reader.readProfile();
        profileConfig = result.getProfile();

        // Decide which scene to load based on whether login is complete
        if (profileConfig.isLoginCompleted() && !profileConfig.getAuthToken().isEmpty()) {
            loadScene("MainPage.fxml", "Cougar Planner - Weekly View", true);
        } else {
            loadScene("Login.fxml", "Cougar Planner - Login", false);
        }
    }

    /**
     * Loads an FXML file into a new stage, applies the user's current theme,
     * then closes the previous stage. Used for the login → main view handoff
     * and any other full-scene swap.
     *
     * @param transparent if true, uses a chromeless transparent stage (the main
     *                    view draws its own custom window chrome); if false,
     *                    uses the OS-decorated window frame (login screen).
     */
    public static void loadScene(String fxmlPath, String title, boolean transparent) throws Exception {
        FXMLLoader loader = new FXMLLoader(Launcher.class.getResource(fxmlPath));
        Scene scene = new Scene(loader.load());

        String themeName = profileConfig != null ? profileConfig.getTheme() : ThemeManager.COUGAR;
        ThemeManager.apply(scene, themeName);

        scene.getStylesheets().add(
                Launcher.class.getResource("/csusm/cougarplanner/styles/filter-styles.css").toExternalForm()
        );

        Stage newStage = new Stage();

        // app logo :)
        newStage.getIcons().add(
                new javafx.scene.image.Image(Launcher.class.getResourceAsStream("/csusm/cougarplanner/images/logo1.png"))
        );

        // Transparent stages let us draw rounded corners / custom chrome in the main view;
        // the login screen uses the OS-decorated frame since it doesn't draw its own chrome.
        if (transparent) {
            newStage.initStyle(StageStyle.TRANSPARENT);
            scene.setFill(Color.TRANSPARENT);
        } else {
            newStage.initStyle(StageStyle.DECORATED);
            scene.setFill(Color.WHITE);
        }

        newStage.setTitle(title);
        newStage.setScene(scene);
        newStage.show();

        // Closes the previous scene
        if (primaryStage != null) {
            primaryStage.close();
        }
        primaryStage = newStage;
    }

    // JavaFX calls this when the last window closes. Force-exit so any
    // background threads (HTTP clients, executors used by Canvas API calls, etc.)
    // don't keep the JVM alive and leave IntelliJ's Run tab "running."
    @Override
    public void stop() {
        System.exit(0);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static Profile getProfileConfig() {
        return profileConfig;
    }

    public static void main(String[] args) {
        launch();
    }
}
