package app.ui;

import app.model.ReportType;
import app.model.Role;
import app.model.User;
import app.service.AuthService;
import app.service.Session;
import atlantafx.base.theme.Styles;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** Main window: sidebar navigation and a content area. */
public class MainView extends StackPane {

    private final StackPane content = new StackPane();
    private final ToggleGroup nav = new ToggleGroup();
    private ToggleButton galleryButton;

    public MainView() {
        Toast.install(this);

        BorderPane layout = new BorderPane();
        content.getStyleClass().add("content-area");
        layout.setLeft(buildSidebar());
        layout.setCenter(content);
        getChildren().add(layout);

        galleryButton.setSelected(true);
        showGallery();
    }

    private VBox buildSidebar() {
        User user = Session.get();

        Label brand = new Label("LostIt? FoundIt!");
        brand.getStyleClass().addAll("title-3", "brand-label");
        Label tagline = new Label("Campus lost and found");
        tagline.getStyleClass().addAll(Styles.TEXT_MUTED, "text-small");

        galleryButton = navButton("Browse items", this::showGallery);
        ToggleButton lostButton = navButton("Report a lost item",
                () -> show(new ReportFormView(ReportType.LOST,
                        this::showGallery)));
        ToggleButton foundButton = navButton("Report a found item",
                () -> show(new ReportFormView(ReportType.FOUND,
                        this::showGallery)));

        // keep one item selected at all times
        nav.selectedToggleProperty().addListener((obs, old, now) -> {
            if (now == null && old != null) {
                old.setSelected(true);
            }
        });

        VBox menu = new VBox(6, galleryButton, lostButton, foundButton);
        if (user.role() == Role.ADMIN) {
            menu.getChildren().add(navButton("Users",
                    () -> show(new UserListView())));
        }

        Label name = new Label(user.fullName());
        name.getStyleClass().add("card-title");
        Label role = new Label(user.role().name());
        role.getStyleClass().add("role-badge");

        Button theme = new Button("Switch light / dark");
        theme.getStyleClass().add("flat");
        theme.setMaxWidth(Double.MAX_VALUE);
        theme.setOnAction(e -> Navigator.toggleTheme());

        Button signOut = new Button("Sign out");
        signOut.getStyleClass().add("button-outlined");
        signOut.setMaxWidth(Double.MAX_VALUE);
        signOut.setOnAction(e -> {
            new AuthService().logout();
            Navigator.showLogin();
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox sidebar = new VBox(14, brand, tagline, menu, spacer,
                name, role, theme, signOut);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(250);
        sidebar.setMinWidth(250);
        sidebar.setPadding(new Insets(24, 16, 20, 16));
        VBox.setMargin(menu, new Insets(18, 0, 0, 0));
        return sidebar;
    }

    private ToggleButton navButton(String text, Runnable action) {
        ToggleButton button = new ToggleButton(text);
        button.setToggleGroup(nav);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> action.run());
        return button;
    }

    private void showGallery() {
        galleryButton.setSelected(true);
        show(new GalleryView());
    }

    private void show(Node view) {
        content.getChildren().setAll(view);
        view.setOpacity(0);
        view.setTranslateY(12);
        FadeTransition fade = new FadeTransition(Duration.millis(260), view);
        fade.setFromValue(0);
        fade.setToValue(1);
        TranslateTransition slide =
                new TranslateTransition(Duration.millis(260), view);
        slide.setToY(0);
        new ParallelTransition(fade, slide).play();
    }
}
