package app;

import app.db.DatabaseManager;
import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Styles;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Application.setUserAgentStylesheet(
                new PrimerLight().getUserAgentStylesheet());
        DatabaseManager.initialize();

        Label title = new Label("LostIt? FoundIt!");
        title.getStyleClass().add(Styles.TITLE_1);

        Label subtitle = new Label(
            "Automated Lost-and-Found Matching for Campus Communities");
        subtitle.getStyleClass().add(Styles.TEXT_MUTED);

        VBox root = new VBox(8, title, subtitle);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(24));

        stage.setTitle("LostIt? FoundIt!");
        stage.setScene(new Scene(root, 900, 600));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
