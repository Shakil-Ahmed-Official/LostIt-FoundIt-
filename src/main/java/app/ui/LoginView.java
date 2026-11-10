package app.ui;

import app.service.AuthService;
import app.service.ServiceException;
import atlantafx.base.theme.Styles;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** Sign-in and registration screen. */
public class LoginView extends HBox {

    private final AuthService auth = new AuthService();
    private final VBox card = new VBox(14);
    private final Label error = new Label();

    public LoginView() {
        VBox brand = buildBrandPanel();
        brand.setPrefWidth(520);
        brand.setMinWidth(420);

        card.getStyleClass().add("auth-card");
        card.setPadding(new Insets(34));
        card.setMaxWidth(400);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        error.getStyleClass().add("error-label");
        error.setWrapText(true);

        StackPane right = new StackPane(card);
        right.getStyleClass().add("auth-pane");
        HBox.setHgrow(right, Priority.ALWAYS);

        getChildren().addAll(brand, right);
        showSignIn();
    }

    private VBox buildBrandPanel() {
        Label title = new Label("LostIt? FoundIt!");
        title.getStyleClass().add("brand-title");
        Label tagline = new Label("Reconnect lost items with their owners.");
        tagline.getStyleClass().add("brand-tagline");
        tagline.setWrapText(true);

        VBox points = new VBox(8,
                point("Automatic matching of lost and found reports"),
                point("Ownership checked before anything is handed over"),
                point("Built for your campus community"));
        points.setPadding(new Insets(24, 0, 0, 0));

        VBox box = new VBox(10, title, tagline, points);
        box.getStyleClass().add("brand-panel");
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private static Label point(String text) {
        Label label = new Label("\u2022  " + text);
        label.getStyleClass().add("brand-point");
        label.setWrapText(true);
        return label;
    }

    private void showSignIn() {
        TextField email = new TextField();
        email.setPromptText("you@campus.edu");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        Button signIn = primaryButton("Sign in");
        signIn.setDefaultButton(true);
        signIn.setOnAction(e -> {
            try {
                auth.login(email.getText(), password.getText());
                Navigator.showMain();
            } catch (ServiceException ex) {
                error.setText(ex.getMessage());
            }
        });

        Button toRegister = linkButton("New here? Create an account");
        toRegister.setOnAction(e -> showRegister());

        Label hint = new Label("Demo admin: admin@campus.edu / admin123");
        hint.getStyleClass().addAll(Styles.TEXT_MUTED, "text-small");

        setForm("Welcome back", "Sign in to continue.",
                field("Email", email), field("Password", password),
                error, signIn, toRegister, hint);
    }

    private void showRegister() {
        TextField name = new TextField();
        name.setPromptText("Your full name");
        TextField email = new TextField();
        email.setPromptText("you@campus.edu");
        PasswordField password = new PasswordField();
        password.setPromptText("At least 6 characters");
        PasswordField confirm = new PasswordField();
        confirm.setPromptText("Repeat the password");

        Button create = primaryButton("Create account");
        create.setDefaultButton(true);
        create.setOnAction(e -> {
            try {
                auth.register(name.getText(), email.getText(),
                        password.getText(), confirm.getText());
                auth.login(email.getText(), password.getText());
                Navigator.showMain();
            } catch (ServiceException ex) {
                error.setText(ex.getMessage());
            }
        });

        Button toSignIn = linkButton("Already registered? Sign in");
        toSignIn.setOnAction(e -> showSignIn());

        setForm("Create your account", "It only takes a minute.",
                field("Full name", name), field("Email", email),
                field("Password", password),
                field("Confirm password", confirm),
                error, create, toSignIn);
    }

    private void setForm(String heading, String subtitle, Node... rows) {
        error.setText("");
        Label title = new Label(heading);
        title.getStyleClass().add("title-3");
        Label sub = new Label(subtitle);
        sub.getStyleClass().add(Styles.TEXT_MUTED);

        card.getChildren().clear();
        card.getChildren().addAll(title, sub);
        card.getChildren().addAll(rows);

        card.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(250), card);
        fade.setToValue(1);
        fade.play();
    }

    private static VBox field(String label, Control control) {
        Label l = new Label(label);
        l.getStyleClass().add("field-label");
        control.setMaxWidth(Double.MAX_VALUE);
        return new VBox(4, l, control);
    }

    private static Button primaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("accent");
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    private static Button linkButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("flat");
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }
}
