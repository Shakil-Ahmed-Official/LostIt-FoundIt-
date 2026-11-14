package app.dao;

import app.db.DatabaseManager;
import app.model.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificationDao {

    public void insert(int userId, String message) {
        run("INSERT INTO notifications (user_id, message) VALUES (?, ?)",
                userId, message);
    }

    public List<Notification> findForUser(int userId) {
        String sql = "SELECT id, user_id, message, is_read, created_at "
                + "FROM notifications WHERE user_id = ? "
                + "ORDER BY id DESC LIMIT 100";
        List<Notification> list = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Notification(rs.getInt("id"),
                            rs.getInt("user_id"), rs.getString("message"),
                            rs.getInt("is_read") == 1,
                            rs.getString("created_at")));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Could not read notifications", e);
        }
    }

    public int countUnread(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications "
                + "WHERE user_id = ? AND is_read = 0";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not count notifications", e);
        }
    }

    public void markAllRead(int userId) {
        run("UPDATE notifications SET is_read = 1 WHERE user_id = ?", userId);
    }

    private void run(String sql, Object... params) {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not save notification", e);
        }
    }
}
