package app.dao;

import app.db.DatabaseManager;
import app.model.Role;
import app.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {

    public record StoredUser(User user, String passwordHash) { }

    public int insert(String fullName, String email,
                      String passwordHash, Role role) {
        String sql = "INSERT INTO users (full_name, email, password_hash, "
                + "role) VALUES (?, ?, ?, ?)";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.setString(4, role.name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not save user", e);
        }
    }

    public Optional<StoredUser> findByEmail(String email) {
        String sql = "SELECT id, full_name, email, password_hash, role "
                + "FROM users WHERE email = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new StoredUser(map(rs),
                        rs.getString("password_hash")));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read user", e);
        }
    }

    public List<User> findAll() {
        String sql = "SELECT id, full_name, email, role FROM users "
                + "ORDER BY id";
        List<User> users = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                users.add(map(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new DataAccessException("Could not list users", e);
        }
    }

    public List<Integer> findIdsByRole(Role role) {
        List<Integer> ids = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id FROM users WHERE role = ?")) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
            }
            return ids;
        } catch (SQLException e) {
            throw new DataAccessException("Could not list users", e);
        }
    }

    public int count() {
        try (Connection c = DatabaseManager.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new DataAccessException("Could not count users", e);
        }
    }

    private static User map(ResultSet rs) throws SQLException {
        return new User(rs.getInt("id"), rs.getString("full_name"),
                rs.getString("email"), Role.valueOf(rs.getString("role")));
    }
}
