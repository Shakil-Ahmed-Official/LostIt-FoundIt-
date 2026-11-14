package app.ui;

import app.model.ClaimView;
import app.model.Report;
import app.service.ClaimService;
import app.service.ServiceException;
import app.service.Session;
import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Admin screen: review claims and record handovers. */
public class AdminClaimsView extends VBox {

    private final ClaimService service = new ClaimService();
    private final VBox pendingBox = new VBox(14);
    private final VBox handoverBox = new VBox(14);

    public AdminClaimsView() {
        setSpacing(18);
        setPadding(new Insets(28, 32, 20, 32));

        Label title = new Label("Claims");
        title.getStyleClass().add("title-2");
        Label subtitle = new Label("Check the hidden detail against the "
                + "item, approve the claim, then hand the item over.");
        subtitle.getStyleClass().add(Styles.TEXT_MUTED);

        TabPane tabs = new TabPane(
                new Tab("Waiting for review", scroll(pendingBox)),
                new Tab("Ready for handover", scroll(handoverBox)));
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabs, Priority.ALWAYS);

        getChildren().addAll(new VBox(2, title, subtitle), tabs);
        reload();
    }

    private static ScrollPane scroll(VBox content) {
        content.setPadding(new Insets(16, 4, 16, 4));
        ScrollPane pane = new ScrollPane(content);
        pane.setFitToWidth(true);
        pane.getStyleClass().add("gallery-scroll");
        return pane;
    }

    private void reload() {
        pendingBox.getChildren().clear();
        handoverBox.getChildren().clear();
        var admin = Session.get();

        var pending = service.pending(admin);
        if (pending.isEmpty()) {
            pendingBox.getChildren().add(muted("No claims are waiting."));
        }
        pending.forEach(c -> pendingBox.getChildren().add(pendingCard(c)));

        var ready = service.awaitingHandover(admin);
        if (ready.isEmpty()) {
            handoverBox.getChildren().add(muted("Nothing to hand over."));
        }
        ready.forEach(c -> handoverBox.getChildren().add(handoverCard(c)));
    }

    private VBox pendingCard(ClaimView view) {
        Label head = new Label(view.claim().claimantName()
                + " claims \"" + view.match().found().title() + "\"");
        head.getStyleClass().add("card-title");
        Label score = new Label(Math.round(
                view.match().match().finalScore() * 100) + "% match");
        score.getStyleClass().add("chip");

        Label quote = new Label(view.claim().hiddenDetail());
        quote.setWrapText(true);
        quote.getStyleClass().add("quote-box");
        Label quoteHead = new Label("Hidden detail given by the claimant");
        quoteHead.getStyleClass().add("field-label");

        Button approve = new Button("Approve");
        approve.getStyleClass().add("accent");
        approve.setOnAction(e -> act(() -> {
            var claim = service.approve(Session.get(), view.claim().id());
            Toast.show("Approved. Pickup code: " + claim.pickupCode());
        }));
        Button reject = new Button("Reject");
        reject.getStyleClass().add("button-outlined");
        reject.setOnAction(e -> act(() -> {
            service.reject(Session.get(), view.claim().id());
            Toast.show("Claim rejected");
        }));

        VBox card = new VBox(10, new HBox(12, head, score),
                itemLine("Lost", view.match().lost()),
                itemLine("Found", view.match().found()),
                quoteHead, quote, new HBox(10, approve, reject));
        card.getStyleClass().add("panel");
        return card;
    }

    private VBox handoverCard(ClaimView view) {
        Label head = new Label("Hand \"" + view.match().found().title()
                + "\" to " + view.claim().claimantName());
        head.getStyleClass().add("card-title");

        TextField code = new TextField();
        code.setPromptText("Pickup code shown by the owner");
        code.setPrefWidth(260);
        Button record = new Button("Record handover");
        record.getStyleClass().add("accent");
        record.setOnAction(e -> act(() -> {
            service.recordHandover(Session.get(), view.claim().id(),
                    code.getText());
            Toast.show("Handover recorded");
        }));
        HBox entry = new HBox(10, code, record);
        entry.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(10, head,
                itemLine("Lost", view.match().lost()),
                itemLine("Found", view.match().found()), entry);
        card.getStyleClass().add("panel");
        return card;
    }

    private static HBox itemLine(String kind, Report report) {
        Label k = new Label(kind);
        k.getStyleClass().addAll("field-label");
        k.setMinWidth(50);
        Label text = new Label(report.title() + "  |  " + report.zone()
                + "  |  " + report.eventDate() + "  |  by "
                + report.reporterName());
        text.getStyleClass().add("text-small");
        Region fill = new Region();
        HBox.setHgrow(fill, Priority.ALWAYS);
        return new HBox(10, k, text, fill);
    }

    private static Label muted(String text) {
        Label label = new Label(text);
        label.getStyleClass().add(Styles.TEXT_MUTED);
        return label;
    }

    private void act(Runnable action) {
        try {
            action.run();
            reload();
        } catch (ServiceException e) {
            Toast.error(e.getMessage());
        }
    }
}
