package app.db;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:lostit.db";

    private DatabaseManager() { }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }

    public static void initialize() {
        try (InputStream in = DatabaseManager.class
                     .getResourceAsStream("/schema.sql");
             Connection conn = getConnection();
             Statement st = conn.createStatement()) {

            String script = new String(in.readAllBytes(),
                    StandardCharsets.UTF_8);
            for (String sql : script.split(";")) {
                if (!sql.isBlank()) {
                    st.execute(sql);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Database initialization failed", e);
        }
    }
}
