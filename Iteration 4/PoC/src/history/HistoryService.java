package history;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import task.Task;

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
                "  id            INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  task_title    TEXT    NOT NULL," +
                "  task_due_date TEXT    NOT NULL," +
                "  timestamp     TEXT    NOT NULL," +
                "  description   TEXT    NOT NULL" +
                ")"
            );
        }
    }

    public void record(Task task, String description) {
        record(task.getTitle(), task.getDueDate().toString(), description);
    }

    public void record(String taskTitle, String taskDueDate, String description) {
        String sql = "INSERT INTO history_logs(task_title, task_due_date, timestamp, description) VALUES(?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, taskTitle);
            ps.setString(2, taskDueDate);
            ps.setString(3, LocalDateTime.now().toString());
            ps.setString(4, description);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to record history entry: " + e.getMessage(), e);
        }
    }

    public List<HistoryLog> getHistory(Task task) {
        return getHistory(task.getTitle(), task.getDueDate().toString());
    }

    public List<HistoryLog> getHistory(String taskTitle, String taskDueDate) {
        String sql =
            "SELECT task_title, task_due_date, timestamp, description" +
            " FROM history_logs" +
            " WHERE task_title = ? AND task_due_date = ?" +
            " ORDER BY id ASC";
        List<HistoryLog> logs = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, taskTitle);
            ps.setString(2, taskDueDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(new HistoryLog(
                        rs.getString("task_title"),
                        rs.getString("task_due_date"),
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
