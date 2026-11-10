package app.ui;

import app.model.Report;
import app.model.Role;
import app.model.User;
import app.service.ReportService;
import app.service.ServiceException;
import app.service.Session;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/** Pop-up with the full details of one report. */
public final class ItemDetailDialog {

    private ItemDetailDialog() { }

    public static void show(Window owner, Report report, Runnable onChanged) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("Report details");

        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().addAll(owner.getScene().getStylesheets());
        pane.getButtonTypes().add(ButtonType.CLOSE);

        User me = Session.get();
        boolean canWithdraw = me.role() == Role.ADMIN
                || me.id() == report.userId();
        ButtonType withdraw = new ButtonType("Withdraw report",
                ButtonBar.ButtonData.LEFT);
        if (canWithdraw) {
            pane.getButtonTypes().add(withdraw);
        }

        HBox content = new HBox(24, photo(report), details(report));
        content.setPadding(new Insets(8));
        pane.setContent(content);

        dialog.showAndWait()
                .filter(result -> result == withdraw)
                .ifPresent(result ->
                        confirmWithdraw(owner, report, onChanged));
    }

    private static Node photo(Report report) {
        Image image = ImageUtil.load(report.photoPath(), 800);
        if (image == null) {
            return ImageUtil.photoBox(report, 320, 260);
        }
        ImageView view = new ImageView(image);
        view.setFitWidth(320);
        view.setFitHeight(320);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        return view;
    }

    private static VBox details(Report report) {
        Label title = new Label(report.title());
        title.getStyleClass().add("title-3");
        title.setWrapText(true);
        title.setMaxWidth(380);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(6);
        String dateLabel = report.type().label() + " on";
        addRow(grid, 0, "Category", report.category());
        addRow(grid, 1, "Campus zone", report.zone());
        addRow(grid, 2, dateLabel, report.eventDate().toString());
        addRow(grid, 3, "Status", report.status().name());
        addRow(grid, 4, "Reported by", report.reporterName());

        Label description = new Label(report.description() == null
                || report.description().isBlank()
                ? "No description provided." : report.description());
        description.setWrapText(true);
        description.setMaxWidth(380);

        VBox box = new VBox(12, ItemCard.badge(report.type()), title,
                grid, description);
        box.setAlignment(Pos.TOP_LEFT);
        return box;
    }

    private static void addRow(GridPane grid, int row, String key,
                               String value) {
        Label k = new Label(key);
        k.getStyleClass().addAll("text-muted", "text-small");
        grid.add(k, 0, row);
        grid.add(new Label(value), 1, row);
    }

    private static void confirmWithdraw(Window owner, Report report,
                                        Runnable onChanged) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Withdraw this report? It will be removed from the gallery.",
                ButtonType.OK, ButtonType.CANCEL);
        alert.initOwner(owner);
        alert.getDialogPane().getStylesheets()
                .addAll(owner.getScene().getStylesheets());
        alert.showAndWait()
                .filter(result -> result == ButtonType.OK)
                .ifPresent(result -> {
                    try {
                        new ReportService().withdraw(Session.get(), report);
                        Toast.show("Report withdrawn");
                        onChanged.run();
                    } catch (ServiceException e) {
                        Toast.error(e.getMessage());
                    }
                });
    }
}
