package app.dao;

import app.db.DatabaseManager;
import app.model.Match;
import app.model.MatchStatus;
import app.model.MatchView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatchDao {

    private static final String SELECT =
            "SELECT m.id, m.lost_report_id, m.found_report_id, "
            + "m.text_score, m.final_score, m.status AS match_status, "
            + "m.created_at AS match_created, "
            + report("l_", "l", "lu") + ", " + report("f_", "f", "fu")
            + " FROM matches m "
            + "JOIN reports l ON l.id = m.lost_report_id "
            + "JOIN users lu ON lu.id = l.user_id "
            + "JOIN reports f ON f.id = m.found_report_id "
            + "JOIN users fu ON fu.id = f.user_id ";

    /** Column list of one report, each column renamed with a prefix. */
    private static String report(String p, String r, String u) {
        return r + ".id AS " + p + "id, " + r + ".user_id AS " + p
                + "user_id, " + u + ".full_name AS " + p + "name, "
                + r + ".type AS " + p + "type, " + r + ".title AS " + p
                + "title, " + r + ".category AS " + p + "category, "
                + r + ".description AS " + p + "description, "
                + r + ".zone AS " + p + "zone, " + r + ".event_date AS "
                + p + "event_date, " + r + ".photo_path AS " + p
                + "photo_path, " + r + ".status AS " + p + "status, "
                + r + ".created_at AS " + p + "created_at";
    }

    /** Stores a match; returns its id, or -1 if the pair already exists. */
    public int insertIfAbsent(int lostId, int foundId,
                              double textScore, double finalScore) {
        String sql = "INSERT OR IGNORE INTO matches (lost_report_id, "
                + "found_report_id, text_score, final_score) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, lostId);
            ps.setInt(2, foundId);
            ps.setDouble(3, textScore);
            ps.setDouble(4, finalScore);
            if (ps.executeUpdate() == 0) {
                return -1;
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not save match", e);
        }
    }

    /** Matches where the user owns the lost or the found report. */
    public List<MatchView> findForUser(int userId) {
        return query(SELECT + "WHERE l.user_id = ? OR f.user_id = ? "
                + "ORDER BY m.id DESC", userId, userId);
    }

    public List<MatchView> findAll() {
        return query(SELECT + "ORDER BY m.id DESC");
    }

    public Optional<MatchView> findViewById(int id) {
        List<MatchView> found = query(SELECT + "WHERE m.id = ?", id);
        return found.isEmpty() ? Optional.empty() : Optional.of(found.get(0));
    }

    public void updateStatus(int id, MatchStatus status) {
        run("UPDATE matches SET status = ? WHERE id = ?",
                status.name(), id);
    }

    /** Dismisses every pending match of a report (used when it is closed). */
    public void rejectPendingFor(int reportId) {
        run("UPDATE matches SET status = 'REJECTED' WHERE status = 'PENDING' "
                + "AND (lost_report_id = ? OR found_report_id = ?)",
                reportId, reportId);
    }

    private List<MatchView> query(String sql, Object... params) {
        List<MatchView> views = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Match match = new Match(rs.getInt("id"),
                            rs.getInt("lost_report_id"),
                            rs.getInt("found_report_id"),
                            rs.getDouble("text_score"),
                            rs.getDouble("final_score"),
                            MatchStatus.valueOf(rs.getString("match_status")),
                            rs.getString("match_created"));
                    views.add(new MatchView(match,
                            ReportDao.mapPrefixed(rs, "l_"),
                            ReportDao.mapPrefixed(rs, "f_"), null));
                }
            }
            return views;
        } catch (SQLException e) {
            throw new DataAccessException("Could not read matches", e);
        }
    }

    private void run(String sql, Object... params) {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not update match", e);
        }
    }
}
