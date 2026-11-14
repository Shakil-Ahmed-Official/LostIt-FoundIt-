package app.service;

import app.dao.NotificationDao;
import app.model.Notification;
import app.model.User;

import java.util.List;

public class NotificationService {

    private final NotificationDao notifications = new NotificationDao();

    public List<Notification> list(User user) {
        return notifications.findForUser(user.id());
    }

    public int unreadCount(User user) {
        return notifications.countUnread(user.id());
    }

    public void markAllRead(User user) {
        notifications.markAllRead(user.id());
    }
}
