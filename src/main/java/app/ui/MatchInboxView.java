package app.ui;

import app.model.ClaimStatus;
import app.model.MatchStatus;
import app.model.MatchView;
import app.model.ReportStatus;
import app.model.User;
import app.service.MatchService;
import app.service.Session;
import atlantafx.base.theme.Styles;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;
import java.util.function.Consumer;

/** List of the possible matches that involve the signed-in user. */
public class MatchInboxView extends VBox {

    private final Consumer<Node> show;

    public MatchInboxView(Consumer<Node> show) {
        this.show = show;
        setSpacing(18);
        setPadding(new Insets(28, 32, 20, 32));

        Label title = new Label("Match inbox");
        title.getStyleClass().add("title-2");
        Label subtitle = new Label("Possible matches between lost and "
                + "found reports. Click one to compare the two items.");
        subtitle.getStyleClass().add(Styles.TEXT_MUTED);

        VBox rows = new VBox(12);
        List<MatchView> matches = new MatchService().listFor(Session.get());
        if (matches.isEmpty()) {
            Label empty = new Label("No matches yet. When a report looks "
                    + "like one of yours, it will show up here.");
            empty.getStyleClass().add(Styles.TEXT_MUTED);
            rows.getChildren().add(empty);
        }
        for (int i = 0; i < matches.size(); i++) {
            HBox row = row(matches.get(i));
            row.setOpacity(0);
            FadeTransition fade =
                    new FadeTransition(Duration.millis(300), row);
            fade.setDelay(Duration.millis(Math.min(i, 10) * 50L));
            fade.setToValue(1);
            fade.play();
            rows.getChildren().add(row);
        }

        ScrollPane scroll = new ScrollPane(rows);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("gallery-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        getChildren().addAll(new VBox(2, title, subtitle), scroll);
    }

    private HBox row(MatchView view) {
        ScoreRing ring = new ScoreRing(64, 7);
        ring.setScore(view.match().finalScore());

        Label lost = new Label("Lost: " + view.lost().title());
        lost.getStyleClass().add("card-title");
        Label found = new Label("Found: " + view.found().title());
        found.getStyleClass().add("card-title");
        Label meta = new Label(view.lost().zone() + "  \u2194  "
                + view.found().zone() + "   |   " + role(view));
        meta.getStyleClass().addAll(Styles.TEXT_MUTED, "text-small");
        VBox text = new VBox(3, lost, found, meta);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label status = new Label(statusText(view));
        status.getStyleClass().add("chip");

        HBox row = new HBox(18, ring, text, spacer, status);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("match-row");
        row.setOnMouseClicked(e -> show.accept(new MatchDetailView(view,
                () -> show.accept(new MatchInboxView(show)))));
        return row;
    }

    private static String role(MatchView view) {
        User me = Session.get();
        if (me.id() == view.lost().userId()) {
            return "your lost item";
        }
        if (me.id() == view.found().userId()) {
            return "your found item";
        }
        return "lost by " + view.lost().reporterName() + ", found by "
                + view.found().reporterName();
    }

    /** Short description of where a match stands. */
    static String statusText(MatchView view) {
        if (view.claim() == null) {
            return view.match().status() == MatchStatus.REJECTED
                    ? "Dismissed" : "New match";
        }
        if (view.claim().status() == ClaimStatus.SUBMITTED) {
            return "Claim submitted";
        }
        if (view.claim().status() == ClaimStatus.APPROVED) {
            return view.lost().status() == ReportStatus.RETURNED
                    ? "Returned" : "Claim approved";
        }
        return "Claim rejected";
    }
}
