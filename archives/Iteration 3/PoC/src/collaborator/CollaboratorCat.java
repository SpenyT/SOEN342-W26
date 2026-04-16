package collaborator;

public enum CollaboratorCat {
    SENIOR(2),
    INTERMEDIATE(5),
    JUNIOR(10);

    public final int maxOpenTasks;

    private CollaboratorCat(int maxOpenTasks) {
        this.maxOpenTasks = maxOpenTasks;
    }

    public int getMaxOpenTasks() { return maxOpenTasks; }
}
