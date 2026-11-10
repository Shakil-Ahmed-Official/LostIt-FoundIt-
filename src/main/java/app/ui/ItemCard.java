package app.ui;

import app.model.Report;
import app.model.ReportType;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.format.DateTimeFormatter;

/** Reusable gallery card: photo, lost/found badge, title and details. */
public class ItemCard extends VBox {

    public static final double WIDTH = 250;
    private static final double IMAGE_HEIGHT = 160;
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("d MMM yyyy");

    public ItemCard(Report report) {
        getStyleClass().add("item-card");
        setSpacing(10);
        setPadding(new Insets(10));
        setPrefWidth(WIDTH);
        setMinWidth(WIDTH);
        setMaxWidth(WIDTH);

        StackPane photo =
                ImageUtil.photoBox(report, WIDTH - 20, IMAGE_HEIGHT);
        Label badge = badge(report.type());
        StackPane.setAlignment(badge, Pos.TOP_LEFT);
        StackPane.setMargin(badge, new Insets(8));
        photo.getChildren().add(badge);

        Label title = new Label(report.title());
        title.getStyleClass().add("card-title");
        title.setMaxWidth(WIDTH - 20);

        Label category = new Label(report.category());
        category.getStyleClass().add("chip");
        Label zone = new Label(report.zone());
        zone.getStyleClass().addAll("text-muted", "text-small");
        HBox meta = new HBox(8, category, zone);
        meta.setAlignment(Pos.CENTER_LEFT);

        Label date = new Label(report.eventDate().format(DATE));
        date.getStyleClass().addAll("text-muted", "text-small");

        getChildren().addAll(photo, title, meta, date);

        setOnMouseEntered(e -> lift(-4));
        setOnMouseExited(e -> lift(0));
    }

    public static Label badge(ReportType type) {
        Label badge = new Label(type.label().toUpperCase());
        badge.getStyleClass().addAll("badge",
                type == ReportType.LOST ? "badge-lost" : "badge-found");
        return badge;
    }

    private void lift(double y) {
        TranslateTransition move =
                new TranslateTransition(Duration.millis(140), this);
        move.setToY(y);
        move.play();
    }
}
