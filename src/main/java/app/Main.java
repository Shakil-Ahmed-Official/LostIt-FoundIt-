package app;

import app.db.DatabaseManager;
import app.service.AuthService;
import app.ui.Navigator;
import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        DatabaseManager.initialize();
        new AuthService().seedAdminIfEmpty();

        loadFont("/fonts/Poppins-Regular.ttf");
        loadFont("/fonts/Poppins-Bold.ttf");

        stage.setTitle("LostIt? FoundIt!");
        Navigator.init(stage);
        Navigator.showLogin();
        stage.show();
    }

    private static void loadFont(String path) {
        try (InputStream in = Main.class.getResourceAsStream(path)) {
            if (in != null) {
                Font.loadFont(in, 14);
            }
        } catch (IOException e) {
            System.err.println("Could not load font " + path);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
