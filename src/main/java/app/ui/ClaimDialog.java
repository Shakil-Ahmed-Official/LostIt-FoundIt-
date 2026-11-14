package app.ui;

import app.model.MatchView;
import app.service.ClaimService;
import app.service.ServiceException;
import app.service.Session;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/** Asks the owner for a hidden detail that proves the item is theirs. */
final class ClaimDialog {

    private ClaimDialog() { }

    static void show(Window owner, MatchView view, Runnable onSubmitted) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("Claim this item");

        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().addAll(owner.getScene().getStylesheets());
        ButtonType submit = new ButtonType("Submit claim",
                ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(submit, ButtonType.CANCEL);

        Label intro = new Label("Describe something about \""
                + view.found().title() + "\" that only its owner would "
                + "know: a scratch, a sticker, what is inside, the lock "
                + "screen. The administrator compares it with the item "
                + "before anything is handed over.");
        intro.setWrapText(true);
        intro.setMaxWidth(440);

        TextArea detail = new TextArea();
        detail.setPromptText("The hidden detail...");
        detail.setPrefRowCount(5);
        detail.setWrapText(true);

        Label error = new Label();
        error.getStyleClass().add("error-label");
        error.setWrapText(true);

        VBox content = new VBox(12, intro, detail, error);
        content.setPadding(new Insets(8));
        pane.setContent(content);

        // keep the dialog open while the input is not valid
        pane.lookupButton(submit).addEventFilter(ActionEvent.ACTION, e -> {
            try {
                new ClaimService().submit(Session.get(),
                        view.match().id(), detail.getText());
            } catch (ServiceException ex) {
                error.setText(ex.getMessage());
                e.consume();
            }
        });

        dialog.showAndWait()
                .filter(result -> result == submit)
                .ifPresent(result -> {
                    Toast.show("Claim submitted. The admin will review it.");
                    onSubmitted.run();
                });
    }
}
