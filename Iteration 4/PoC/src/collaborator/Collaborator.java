package collaborator;

import java.util.ArrayList;

import task.Status;
import task.SubTask;

public class Collaborator {
    private String name;
    private final CollaboratorCat type;
    private final int maxTasks;
    private int nOpenTasks;
    private final ArrayList<SubTask> assignedSubTasks;

    public Collaborator(String name, CollaboratorCat type) {
        this.name = name;
        this.type = type;
        this.maxTasks = type.getMaxOpenTasks();
        this.nOpenTasks = 0;
        this.assignedSubTasks = new ArrayList<>();
    }

    public void assignSubTask(SubTask subTask) {
        if (nOpenTasks >= maxTasks) {
            throw new IllegalStateException(
                "Collaborator '" + name + "' has reached the maximum number of open tasks (" + maxTasks + ")."
            );
        }
        assignedSubTasks.add(subTask);
        nOpenTasks++;
    }

    public void restoreAssignment(SubTask subTask, boolean parentTaskIsOpen) {
        assignedSubTasks.add(subTask);
        if (parentTaskIsOpen && subTask.getStatus() == Status.OPEN) nOpenTasks++;
    }

    public void releaseSubTask(SubTask subTask) {
        if (nOpenTasks > 0) nOpenTasks--;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public CollaboratorCat getType() { return type; }
    public int getMaxTasks() { return maxTasks; }
    public int getNOpenTasks() { return nOpenTasks; }
    public boolean isAtCapacity() { return nOpenTasks >= maxTasks; }
    public ArrayList<SubTask> getAssignedSubTasks() { return assignedSubTasks; }

    @Override
    public String toString() {
        return "Collaborator{name='" + name + "', type=" + type + ", nOpenTasks=" + nOpenTasks + "/" + maxTasks + "}";
    }
}
