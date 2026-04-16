package history;

import java.time.LocalDateTime;

public class HistoryLog {
    private final String taskTitle;
    private final String taskDueDate;
    private final LocalDateTime timestamp;
    private final String description;

    public HistoryLog(String taskTitle, String taskDueDate, LocalDateTime timestamp, String description) {
        this.taskTitle = taskTitle;
        this.taskDueDate = taskDueDate;
        this.timestamp = timestamp;
        this.description = description;
    }

    public String getTaskTitle()   { return taskTitle; }
    public String getTaskDueDate() { return taskDueDate; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + description;
    }
}
