package app.ui;

import app.model.Catalog;
import app.model.ReportType;
import app.service.ReportService;
import app.service.ServiceException;
import app.service.Session;
import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;

/** Form for submitting a lost or found report. */
public class ReportFormView extends ScrollPane {

    private final ReportService service = new ReportService();
    private final ReportType type;
    private final Runnable onSubmitted;

    private final TextField title = new TextField();
    private final ComboBox<String> category = new ComboBox<>();
    private final ComboBox<String> zone = new ComboBox<>();
    private final DatePicker date = new DatePicker(LocalDate.now());
    private final TextArea description = new TextArea();
    private final Label error = new Label();
    private final ImageView preview = new ImageView();
    private final Label photoHint = new Label("No photo selected");
    private File photo;

    public ReportFormView(ReportType type, Runnable onSubmitted) {
        this.type = type;
        this.onSubmitted = onSubmitted;
        boolean lost = type == ReportType.LOST;

        Label heading = new Label(lost
                ? "Report a lost item" : "Report a found item");
        heading.getStyleClass().add("title-2");
        Label subtitle = new Label(lost
                ? "Describe what you lost. We will look for matching "
                        + "found reports."
                : "Thank you for helping. Describe what you found so "
                        + "its owner can recognise it.");
        subtitle.getStyleClass().add(Styles.TEXT_MUTED);
        subtitle.setWrapText(true);

        title.setPromptText("e.g. Blue Nike water bottle");
        category.getItems().addAll(Catalog.CATEGORIES);
        category.setPromptText("Choose a category");
        zone.getItems().addAll(Catalog.ZONES);
        zone.setPromptText("Choose a campus zone");
        date.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(empty || item.isAfter(LocalDate.now()));
            }
        });
        description.setPromptText("Colour, brand, size, stickers, marks "
                + "(do not write secret details here)");
        description.setPrefRowCount(5);
        description.setWrapText(true);
        for (var control : new Region[] {category, zone, date}) {
            control.setMaxWidth(Double.MAX_VALUE);
        }

        error.getStyleClass().add("error-label");
        error.setWrapText(true);

        Button submit = new Button("Submit report");
        submit.getStyleClass().add("accent");
        submit.setOnAction(e -> submit());

        VBox form = new VBox(14,
                field("Title", title),
                field("Category", category),
                field(lost ? "Where did you lose it?"
                        : "Where did you find it?", zone),
                field(lost ? "Date lost" : "Date found", date),
                field("Description", description),
                error, submit);
        form.getStyleClass().add("panel");
        form.setPrefWidth(520);
        form.setMaxWidth(520);

        HBox body = new HBox(28, form, buildPhotoPanel());
        body.setAlignment(Pos.TOP_LEFT);

        VBox page = new VBox(18, new VBox(2, heading, subtitle), body);
        page.setPadding(new Insets(28, 32, 28, 32));

        setContent(page);
        setFitToWidth(true);
        getStyleClass().add("gallery-scroll");
    }

    private VBox buildPhotoPanel() {
        preview.setFitWidth(300);
        preview.setFitHeight(220);
        preview.setPreserveRatio(true);
        photoHint.getStyleClass().add(Styles.TEXT_MUTED);

        StackPane zoneBox = new StackPane(photoHint, preview);
        zoneBox.getStyleClass().add("drop-zone");
        zoneBox.setPrefSize(320, 240);
        zoneBox.setMinSize(320, 240);
        zoneBox.setMaxSize(320, 240);

        Button choose = new Button("Choose photo...");
        choose.setOnAction(e -> choosePhoto());
        Button remove = new Button("Remove");
        remove.getStyleClass().add("flat");
        remove.setOnAction(e -> clearPhoto());

        Label tip = new Label("A clear photo makes matching much easier.");
        tip.getStyleClass().addAll(Styles.TEXT_MUTED, "text-small");
        tip.setWrapText(true);

        VBox panel = new VBox(12, new Label("Photo (optional)"), zoneBox,
                new HBox(8, choose, remove), tip);
        panel.getStyleClass().add("panel");
        panel.setPrefWidth(364);
        panel.setMaxWidth(364);
        return panel;
    }

    private void choosePhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose a photo");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"));
        File file = chooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            photo = file;
            preview.setImage(new Image(file.toURI().toString(),
                    600, 0, true, true));
            photoHint.setVisible(false);
        }
    }

    private void clearPhoto() {
        photo = null;
        preview.setImage(null);
        photoHint.setVisible(true);
    }

    private void submit() {
        try {
            service.submit(Session.get(), type, title.getText(),
                    category.getValue(), description.getText(),
                    zone.getValue(), date.getValue(), photo);
            Toast.show("Your " + type.label().toLowerCase()
                    + " report was submitted");
            onSubmitted.run();
        } catch (ServiceException e) {
            error.setText(e.getMessage());
        }
    }

    private static VBox field(String label, Node control) {
        Label l = new Label(label);
        l.getStyleClass().add("field-label");
        return new VBox(4, l, control);
    }
}
