package app.ui;

import app.matching.MatchScorer;
import app.model.Claim;
import app.model.ClaimStatus;
import app.model.MatchStatus;
import app.model.MatchView;
import app.model.ReportStatus;
import app.model.User;
import app.service.MatchService;
import app.service.ServiceException;
import app.service.Session;
import atlantafx.base.theme.Styles;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** Side-by-side comparison of a lost and a found report with the score. */
public class MatchDetailView extends ScrollPane {

    private final MatchService matchService = new MatchService();
    private final Runnable onBack;
    private final int matchId;

    public MatchDetailView(MatchView view, Runnable onBack) {
        this.onBack = onBack;
        this.matchId = view.match().id();
        setFitToWidth(true);
        getStyleClass().add("gallery-scroll");
        setContent(build(view, true));
    }

    private VBox build(MatchView view, boolean animate) {
        Button back = new Button("\u2190  Back to inbox");
        back.getStyleClass().add("flat");
        back.setOnAction(e -> onBack.run());

        Label title = new Label("Possible match");
        title.getStyleClass().add("title-2");
        Label status = new Label(MatchInboxView.statusText(view));
        status.getStyleClass().add("chip");
        HBox header = new HBox(14, title, status);
        header.setAlignment(Pos.CENTER_LEFT);

        ReportPanel lostPanel = new ReportPanel(view.lost(), "LOST ITEM");
        ReportPanel foundPanel = new ReportPanel(view.found(), "FOUND ITEM");
        double score = view.match().finalScore();

        ScoreRing ring = new ScoreRing(170, 16);
        Label ringCaption = new Label("Match score");
        ringCaption.getStyleClass().addAll(Styles.TEXT_MUTED, "text-small");
        VBox center = new VBox(10, ring, ringCaption,
                breakdown(view));
        center.setAlignment(Pos.TOP_CENTER);
        center.setPrefWidth(270);
        center.setMinWidth(270);

        HBox row = new HBox(20, lostPanel, center, foundPanel);
        row.setAlignment(Pos.TOP_CENTER);

        VBox page = new VBox(16, back, header, row, actions(view));
        page.setPadding(new Insets(20, 32, 28, 32));

        if (animate) {
            slideIn(lostPanel, -70, 0);
            slideIn(foundPanel, 70, 0);
            slideIn(center, 0, 150);
            PauseTransition wait = new PauseTransition(Duration.millis(450));
            wait.setOnFinished(e -> ring.play(score));
            wait.play();
        } else {
            ring.setScore(score);
        }
        return page;
    }

    private VBox breakdown(MatchView view) {
        double text = view.match().textScore();
        double zone = MatchScorer.zoneScore(view.lost().zone(),
                view.found().zone());
        double date = MatchScorer.dateScore(view.lost().eventDate(),
                view.found().eventDate());
        Label note = new Label("Score = 70% description + 15% zone "
                + "+ 15% date");
        note.getStyleClass().addAll(Styles.TEXT_MUTED, "text-small");
        note.setWrapText(true);
        VBox box = new VBox(8, bar("Description", text), bar("Zone", zone),
                bar("Date", date), note);
        box.setPadding(new Insets(10, 0, 0, 0));
        return box;
    }

    private static VBox bar(String name, double value) {
        Label label = new Label(name + "  " + Math.round(value * 100) + "%");
        label.getStyleClass().add("text-small");
        ProgressBar bar = new ProgressBar(value);
        bar.setMaxWidth(Double.MAX_VALUE);
        return new VBox(3, label, bar);
    }

    private VBox actions(MatchView view) {
        User me = Session.get();
        boolean owner = me.id() == view.lost().userId();
        boolean finder = me.id() == view.found().userId();
        Claim claim = view.claim();
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);

        if (claim == null && view.match().status() == MatchStatus.PENDING) {
            if (owner) {
                Button claimButton = new Button("This is my item: claim it");
                claimButton.getStyleClass().add("accent");
                claimButton.setOnAction(e -> ClaimDialog.show(
                        getScene().getWindow(), view, this::reload));
                Button dismiss = new Button("Not my item");
                dismiss.getStyleClass().add("button-outlined");
                dismiss.setOnAction(e -> dismiss(view));
                HBox buttons = new HBox(12, claimButton, dismiss);
                buttons.setAlignment(Pos.CENTER);
                box.getChildren().add(buttons);
            } else if (finder) {
                box.getChildren().add(info("The owner of the lost report has "
                        + "been told. They must describe a hidden detail "
                        + "before the item is handed over."));
            } else {
                box.getChildren().add(info(
                        "Claims are reviewed in the Claims screen."));
            }
        } else if (claim == null) {
            box.getChildren().add(info("This match was dismissed."));
        } else if (claim.status() == ClaimStatus.SUBMITTED) {
            box.getChildren().add(info(owner
                    ? "Your claim is waiting for approval by the admin."
                    : "A claim was submitted and waits for the admin."));
        } else if (claim.status() == ClaimStatus.APPROVED) {
            if (view.lost().status() == ReportStatus.RETURNED) {
                box.getChildren().add(info(
                        "This item was returned to its owner."));
            } else if (owner) {
                Label code = new Label(claim.pickupCode());
                code.getStyleClass().add("big-code");
                box.getChildren().addAll(info("Claim approved. Your pickup "
                        + "code is:"), code, info(
                        "Show this code at the admin office."));
            } else if (finder) {
                box.getChildren().add(info("The owner was verified. Please "
                        + "hand the item to the admin office."));
            } else {
                box.getChildren().add(info("Approved. Record the handover "
                        + "in the Claims screen."));
            }
        } else {
            box.getChildren().add(info("The claim was not approved."));
        }
        return box;
    }

    private static Label info(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(560);
        label.getStyleClass().add(Styles.TEXT_MUTED);
        return label;
    }

    private void dismiss(MatchView view) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Dismiss this match? You will not see it as a suggestion "
                + "again.", ButtonType.OK, ButtonType.CANCEL);
        alert.initOwner(getScene().getWindow());
        alert.getDialogPane().getStylesheets()
                .addAll(getScene().getStylesheets());
        alert.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            try {
                matchService.dismiss(Session.get(), view.match().id());
                Toast.show("Match dismissed");
                onBack.run();
            } catch (ServiceException e) {
                Toast.error(e.getMessage());
            }
        });
    }

    private void reload() {
        setContent(build(matchService.reload(matchId), false));
    }

    private static void slideIn(Node node, double fromX, double delayMs) {
        node.setOpacity(0);
        node.setTranslateX(fromX);
        FadeTransition fade = new FadeTransition(Duration.millis(450), node);
        fade.setToValue(1);
        TranslateTransition slide =
                new TranslateTransition(Duration.millis(450), node);
        slide.setToX(0);
        slide.setInterpolator(Interpolator.EASE_OUT);
        ParallelTransition both = new ParallelTransition(fade, slide);
        both.setDelay(Duration.millis(delayMs));
        both.play();
    }
}
