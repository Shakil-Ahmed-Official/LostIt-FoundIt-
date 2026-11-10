package app.ui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/** Short pop-up messages at the bottom of the main window. */
public final class Toast {

    private static StackPane host;

    private Toast() { }

    public static void install(StackPane window) {
        host = window;
    }

    public static void show(String message) {
        display(message, "toast-success");
    }

    public static void error(String message) {
        display(message, "toast-error");
    }

    private static void display(String message, String styleClass) {
        if (host == null) {
            return;
        }
        StackPane layer = host;
        Label label = new Label(message);
        label.getStyleClass().addAll("toast", styleClass);
        label.setMouseTransparent(true);
        label.setOpacity(0);
        StackPane.setAlignment(label, Pos.BOTTOM_CENTER);
        StackPane.setMargin(label, new Insets(0, 0, 36, 0));
        layer.getChildren().add(label);

        FadeTransition in = new FadeTransition(Duration.millis(220), label);
        in.setToValue(1);
        PauseTransition stay = new PauseTransition(Duration.seconds(2.4));
        FadeTransition out = new FadeTransition(Duration.millis(400), label);
        out.setToValue(0);

        SequentialTransition show = new SequentialTransition(in, stay, out);
        show.setOnFinished(e -> layer.getChildren().remove(label));
        show.play();
    }
}
