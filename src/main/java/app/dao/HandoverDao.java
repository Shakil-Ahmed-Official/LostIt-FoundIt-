package app.dao;

import app.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class HandoverDao {

    public void insert(int claimId, int adminId) {
        String sql = "INSERT INTO handovers (claim_id, admin_id) "
                + "VALUES (?, ?)";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, claimId);
            ps.setInt(2, adminId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not save handover", e);
        }
    }
}
