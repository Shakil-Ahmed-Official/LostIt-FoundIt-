package app.ui;

import app.model.Notification;
import app.model.User;
import app.service.NotificationService;
import app.service.Session;
import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/** The user's in-app messages. Opening the screen marks them as read. */
public class NotificationsView extends VBox {

    public NotificationsView(Runnable onRead) {
        setSpacing(18);
        setPadding(new Insets(28, 32, 20, 32));

        Label title = new Label("Notifications");
        title.getStyleClass().add("title-2");
        Label subtitle = new Label("Messages about matches and claims.");
        subtitle.getStyleClass().add(Styles.TEXT_MUTED);

        User me = Session.get();
        NotificationService service = new NotificationService();
        List<Notification> list = service.list(me);

        VBox rows = new VBox(10);
        if (list.isEmpty()) {
            Label empty = new Label("You have no notifications yet.");
            empty.getStyleClass().add(Styles.TEXT_MUTED);
            rows.getChildren().add(empty);
        }
        for (Notification n : list) {
            Label message = new Label(n.message());
            message.setWrapText(true);
            Label time = new Label(TimeUtil.local(n.createdAt()));
            time.getStyleClass().addAll(Styles.TEXT_MUTED, "text-small");
            VBox row = new VBox(3, message, time);
            row.getStyleClass().add("notification-row");
            if (!n.read()) {
                row.getStyleClass().add("notification-unread");
            }
            rows.getChildren().add(row);
        }

        service.markAllRead(me);
        onRead.run();

        ScrollPane scroll = new ScrollPane(rows);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("gallery-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        getChildren().addAll(new VBox(2, title, subtitle), scroll);
    }
}
