package task;

import java.util.UUID;

public abstract class WorkItem {
    private final String id;
    private String title;
    private Status status;

    public WorkItem(String title) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.status = Status.OPEN;
    }

    public WorkItem(String title, String id) {
        this.id = id;
        this.title = title;
        this.status = Status.OPEN;
    }

    public String getId()    { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
