package app.dao;

import app.db.DatabaseManager;
import app.model.Claim;
import app.model.ClaimStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClaimDao {

    private static final String SELECT =
            "SELECT c.id, c.match_id, c.claimant_id, "
            + "u.full_name AS claimant_name, c.hidden_detail_text, "
            + "c.status, c.pickup_code, c.created_at "
            + "FROM claims c JOIN users u ON u.id = c.claimant_id ";

    public int insert(int matchId, int claimantId, String hiddenDetail) {
        String sql = "INSERT INTO claims (match_id, claimant_id, "
                + "hidden_detail_text) VALUES (?, ?, ?)";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, matchId);
            ps.setInt(2, claimantId);
            ps.setString(3, hiddenDetail);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not save claim", e);
        }
    }

    public Optional<Claim> findById(int id) {
        List<Claim> found = query(SELECT + "WHERE c.id = ?", id);
        return found.isEmpty() ? Optional.empty() : Optional.of(found.get(0));
    }

    public Optional<Claim> findLatestByMatch(int matchId) {
        List<Claim> found = query(SELECT + "WHERE c.match_id = ? "
                + "ORDER BY c.id DESC LIMIT 1", matchId);
        return found.isEmpty() ? Optional.empty() : Optional.of(found.get(0));
    }

    public List<Claim> findByStatus(ClaimStatus status) {
        return query(SELECT + "WHERE c.status = ? ORDER BY c.id",
                status.name());
    }

    /** Approved claims whose item has not been handed over yet. */
    public List<Claim> findAwaitingHandover() {
        return query(SELECT + "LEFT JOIN handovers h ON h.claim_id = c.id "
                + "WHERE c.status = 'APPROVED' AND h.id IS NULL "
                + "ORDER BY c.id");
    }

    public void setApproved(int id, String pickupCode) {
        run("UPDATE claims SET status = 'APPROVED', pickup_code = ? "
                + "WHERE id = ?", pickupCode, id);
    }

    public void setRejected(int id) {
        run("UPDATE claims SET status = 'REJECTED' WHERE id = ?", id);
    }

    private List<Claim> query(String sql, Object... params) {
        List<Claim> claims = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    claims.add(new Claim(rs.getInt("id"),
                            rs.getInt("match_id"), rs.getInt("claimant_id"),
                            rs.getString("claimant_name"),
                            rs.getString("hidden_detail_text"),
                            ClaimStatus.valueOf(rs.getString("status")),
                            rs.getString("pickup_code"),
                            rs.getString("created_at")));
                }
            }
            return claims;
        } catch (SQLException e) {
            throw new DataAccessException("Could not read claims", e);
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
            throw new DataAccessException("Could not update claim", e);
        }
    }
}
