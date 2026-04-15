package history;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import task.WorkItem;

public class HistoryService {
    private static final String DB_URL = "jdbc:sqlite:data/history.db";

    private final Connection conn;

    public HistoryService() {
        try {
            this.conn = DriverManager.getConnection(DB_URL);
            initSchema();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize history database: " + e.getMessage(), e);
        }
    }

    private void initSchema() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS history_logs (" +
                "  id          INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  workitem_id TEXT    NOT NULL," +
                "  timestamp   TEXT    NOT NULL," +
                "  description TEXT    NOT NULL" +
                ")"
            );
        }
    }

    public void record(WorkItem item, String description) {
        record(item.getId(), description);
    }

    public void record(String workitemId, String description) {
        String sql = "INSERT INTO history_logs(workitem_id, timestamp, description) VALUES(?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, workitemId);
            ps.setString(2, LocalDateTime.now().toString());
            ps.setString(3, description);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to record history entry: " + e.getMessage(), e);
        }
    }

    public List<HistoryLog> getHistory(WorkItem item) {
        return getHistory(item.getId());
    }

    public List<HistoryLog> getHistory(String workitemId) {
        String sql =
            "SELECT workitem_id, timestamp, description" +
            " FROM history_logs" +
            " WHERE workitem_id = ?" +
            " ORDER BY id ASC";
        List<HistoryLog> logs = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, workitemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(new HistoryLog(
                        rs.getString("workitem_id"),
                        LocalDateTime.parse(rs.getString("timestamp")),
                        rs.getString("description")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load history: " + e.getMessage(), e);
        }
        return logs;
    }
}
