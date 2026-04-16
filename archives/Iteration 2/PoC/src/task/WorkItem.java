package task;

public abstract class WorkItem {
    private String title;
    private Status status;

    public WorkItem(String title) {
        this.title = title;
        this.status = Status.OPEN;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
