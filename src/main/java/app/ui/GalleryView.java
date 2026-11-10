package app.ui;

import app.model.Catalog;
import app.model.Report;
import app.model.ReportType;
import app.service.ReportService;
import atlantafx.base.theme.Styles;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;

/** Card gallery of all open reports with simple filters. */
public class GalleryView extends VBox {

    private static final String ALL_CATEGORIES = "All categories";

    private final ReportService service = new ReportService();
    private final FlowPane grid = new FlowPane(20, 20);
    private final Label count = new Label();
    private final ToggleGroup typeGroup = new ToggleGroup();
    private final ComboBox<String> category = new ComboBox<>();
    private final TextField search = new TextField();
    private List<Report> all = List.of();

    public GalleryView() {
        setSpacing(18);
        setPadding(new Insets(28, 32, 20, 32));

        Label title = new Label("Browse items");
        title.getStyleClass().add("title-2");
        Label subtitle = new Label(
                "Lost and found reports from across the campus.");
        subtitle.getStyleClass().add(Styles.TEXT_MUTED);

        ToggleButton everything = typeToggle("All", null);
        ToggleButton lost = typeToggle("Lost", ReportType.LOST);
        ToggleButton found = typeToggle("Found", ReportType.FOUND);
        everything.setSelected(true);
        typeGroup.selectedToggleProperty().addListener((obs, old, now) -> {
            if (now == null && old != null) {
                old.setSelected(true);
            } else {
                applyFilters();
            }
        });

        category.getItems().add(ALL_CATEGORIES);
        category.getItems().addAll(Catalog.CATEGORIES);
        category.setValue(ALL_CATEGORIES);
        category.valueProperty().addListener(
                (obs, old, now) -> applyFilters());

        search.setPromptText("Search title, description or zone");
        search.setPrefWidth(280);
        search.textProperty().addListener((obs, old, now) -> applyFilters());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        count.getStyleClass().addAll(Styles.TEXT_MUTED, "text-small");

        HBox filters = new HBox(10, everything, lost, found, category,
                search, spacer, count);
        filters.setAlignment(Pos.CENTER_LEFT);

        grid.setPadding(new Insets(4));
        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("gallery-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(new VBox(2, title, subtitle), filters, scroll);
        load();
    }

    private ToggleButton typeToggle(String text, ReportType type) {
        ToggleButton button = new ToggleButton(text);
        button.setToggleGroup(typeGroup);
        button.setUserData(type);
        button.getStyleClass().add("chip-toggle");
        return button;
    }

    private void load() {
        all = service.listActive();
        applyFilters();
    }

    private void applyFilters() {
        Toggle selected = typeGroup.getSelectedToggle();
        ReportType type = selected == null
                ? null : (ReportType) selected.getUserData();
        String cat = category.getValue();
        String query = search.getText() == null
                ? "" : search.getText().trim().toLowerCase();

        List<Report> shown = all.stream()
                .filter(r -> type == null || r.type() == type)
                .filter(r -> cat == null || cat.equals(ALL_CATEGORIES)
                        || r.category().equals(cat))
                .filter(r -> query.isEmpty() || (r.title() + " "
                        + r.description() + " " + r.zone())
                        .toLowerCase().contains(query))
                .toList();
        render(shown);
    }

    private void render(List<Report> shown) {
        grid.getChildren().clear();
        count.setText(shown.size()
                + (shown.size() == 1 ? " item" : " items"));

        if (shown.isEmpty()) {
            Label empty = new Label("Nothing here yet. Use the sidebar to "
                    + "report a lost or found item.");
            empty.getStyleClass().add(Styles.TEXT_MUTED);
            grid.getChildren().add(empty);
            return;
        }
        for (int i = 0; i < shown.size(); i++) {
            Report report = shown.get(i);
            ItemCard card = new ItemCard(report);
            card.setOnMouseClicked(e -> ItemDetailDialog.show(
                    getScene().getWindow(), report, this::load));
            card.setOpacity(0);
            FadeTransition fade =
                    new FadeTransition(Duration.millis(300), card);
            fade.setDelay(Duration.millis(Math.min(i, 12) * 40L));
            fade.setToValue(1);
            fade.play();
            grid.getChildren().add(card);
        }
    }
}
