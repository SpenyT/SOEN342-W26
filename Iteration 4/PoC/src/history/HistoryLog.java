package history;

import java.time.LocalDateTime;

public class HistoryLog {
    private final String workitemId;
    private final LocalDateTime timestamp;
    private final String description;

    public HistoryLog(String workitemId, LocalDateTime timestamp, String description) {
        this.workitemId = workitemId;
        this.timestamp = timestamp;
        this.description = description;
    }

    public String getWorkitemId()       { return workitemId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getDescription()      { return description; }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + description;
    }
}
