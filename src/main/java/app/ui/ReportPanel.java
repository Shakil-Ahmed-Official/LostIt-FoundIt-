package app.ui;

import app.model.Report;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Card with the full details of one report, used in the comparison. */
final class ReportPanel extends VBox {

    static final double WIDTH = 300;

    ReportPanel(Report report, String heading) {
        getStyleClass().add("panel");
        setSpacing(12);
        setPrefWidth(WIDTH);
        setMinWidth(WIDTH);
        setMaxWidth(WIDTH);

        Label head = new Label(heading);
        head.getStyleClass().add("field-label");

        StackPane photo = ImageUtil.photoBox(report, WIDTH - 44, 180);
        Label badge = ItemCard.badge(report.type());
        StackPane.setAlignment(badge, Pos.TOP_LEFT);
        StackPane.setMargin(badge, new Insets(8));
        photo.getChildren().add(badge);

        Label title = new Label(report.title());
        title.getStyleClass().add("card-title");
        title.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(4);
        row(grid, 0, "Category", report.category());
        row(grid, 1, "Zone", report.zone());
        row(grid, 2, "Date", report.eventDate().toString());
        row(grid, 3, "Reported by", report.reporterName());

        String text = report.description() == null
                || report.description().isBlank()
                ? "No description." : report.description();
        Label description = new Label(text);
        description.setWrapText(true);
        description.getStyleClass().addAll("text-muted", "text-small");

        getChildren().addAll(head, photo, title, grid, description);
    }

    private static void row(GridPane grid, int row, String key,
                            String value) {
        Label k = new Label(key);
        k.getStyleClass().addAll("text-muted", "text-small");
        Label v = new Label(value);
        v.getStyleClass().add("text-small");
        grid.add(k, 0, row);
        grid.add(v, 1, row);
    }
}
