package app.ui;

import app.Main;
import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

/** Switches between the login screen and the main window. */
public final class Navigator {

    private static Scene scene;
    private static boolean dark;

    private Navigator() { }

    public static void init(Stage stage) {
        applyTheme();
        scene = new Scene(new StackPane(), 1280, 800);
        scene.getStylesheets().add(
                Main.class.getResource("/css/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setMinWidth(1040);
        stage.setMinHeight(680);
    }

    public static void showLogin() {
        setRoot(new LoginView());
    }

    public static void showMain() {
        setRoot(new MainView());
    }

    public static Window window() {
        return scene.getWindow();
    }

    /** Switches between the light and dark theme; returns true if dark. */
    public static boolean toggleTheme() {
        dark = !dark;
        applyTheme();
        return dark;
    }

    private static void applyTheme() {
        String sheet = dark
                ? new PrimerDark().getUserAgentStylesheet()
                : new PrimerLight().getUserAgentStylesheet();
        Application.setUserAgentStylesheet(sheet);
    }

    private static void setRoot(Parent root) {
        root.setOpacity(0);
        scene.setRoot(root);
        FadeTransition fade = new FadeTransition(Duration.millis(300), root);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }
}
