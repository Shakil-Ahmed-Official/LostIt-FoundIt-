package app.ui;

import app.model.User;
import app.service.AuthService;
import atlantafx.base.theme.Styles;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Function;

/** Admin-only table of registered users. */
public class UserListView extends VBox {

    public UserListView() {
        setSpacing(18);
        setPadding(new Insets(28, 32, 20, 32));

        Label title = new Label("Registered users");
        title.getStyleClass().add("title-2");
        Label subtitle = new Label("Visible to administrators only.");
        subtitle.getStyleClass().add(Styles.TEXT_MUTED);

        TableView<User> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().add(column("ID", 70,
                u -> String.valueOf(u.id())));
        table.getColumns().add(column("Full name", 220, User::fullName));
        table.getColumns().add(column("Email", 260, User::email));
        table.getColumns().add(column("Role", 110, u -> u.role().name()));
        table.getItems().addAll(new AuthService().listUsers());
        VBox.setVgrow(table, Priority.ALWAYS);

        getChildren().addAll(new VBox(2, title, subtitle), table);
    }

    private static TableColumn<User, String> column(String name, double width,
            Function<User, String> value) {
        TableColumn<User, String> column = new TableColumn<>(name);
        column.setPrefWidth(width);
        column.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        value.apply(cell.getValue())));
        return column;
    }
}
