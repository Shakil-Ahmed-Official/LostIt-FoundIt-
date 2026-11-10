package app.dao;

import app.db.DatabaseManager;
import app.model.Report;
import app.model.ReportStatus;
import app.model.ReportType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReportDao {

    private static final String SELECT =
            "SELECT r.id, r.user_id, u.full_name, r.type, r.title, "
            + "r.category, r.description, r.zone, r.event_date, "
            + "r.photo_path, r.status, r.created_at "
            + "FROM reports r JOIN users u ON u.id = r.user_id ";

    public int insert(int userId, ReportType type, String title,
                      String category, String description, String zone,
                      LocalDate eventDate, String photoPath) {
        String sql = "INSERT INTO reports (user_id, type, title, category, "
                + "description, zone, event_date, photo_path) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, type.name());
            ps.setString(3, title);
            ps.setString(4, category);
            ps.setString(5, description);
            ps.setString(6, zone);
            ps.setString(7, eventDate.toString());
            ps.setString(8, photoPath);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not save report", e);
        }
    }

    /** Reports that are still visible in the gallery (OPEN or MATCHED). */
    public List<Report> findActive() {
        String sql = SELECT + "WHERE r.status IN ('OPEN', 'MATCHED') "
                + "ORDER BY r.created_at DESC, r.id DESC";
        List<Report> reports = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                reports.add(map(rs));
            }
            return reports;
        } catch (SQLException e) {
            throw new DataAccessException("Could not list reports", e);
        }
    }

    public Optional<Report> findById(int id) {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     SELECT + "WHERE r.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read report", e);
        }
    }

    public void updateStatus(int id, ReportStatus status) {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE reports SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not update report", e);
        }
    }

    private static Report map(ResultSet rs) throws SQLException {
        return new Report(
                rs.getInt("id"), rs.getInt("user_id"),
                rs.getString("full_name"),
                ReportType.valueOf(rs.getString("type")),
                rs.getString("title"), rs.getString("category"),
                rs.getString("description"), rs.getString("zone"),
                LocalDate.parse(rs.getString("event_date")),
                rs.getString("photo_path"),
                ReportStatus.valueOf(rs.getString("status")),
                rs.getString("created_at"));
    }
}
