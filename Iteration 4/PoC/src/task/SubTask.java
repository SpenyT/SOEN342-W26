package task;

import collaborator.Collaborator;

public class SubTask extends WorkItem {
    private Collaborator assignedCollaborator;
    private Task parentTask;

    public SubTask(String title, String id) {
        super(title, id);
        this.assignedCollaborator = null;
        this.parentTask = null;
    }

    public SubTask(String title) {
        super(title);
        this.assignedCollaborator = null;
        this.parentTask = null;
    }

    public SubTask(String title, Collaborator collaborator) {
        super(title);
        this.assignedCollaborator = collaborator;
        this.parentTask = null;
    }

    public void assignCollaborator(Collaborator collaborator) {
        collaborator.assignSubTask(this);
        this.assignedCollaborator = collaborator;
    }

    @Override
    public void setStatus(Status status) {
        Status previous = getStatus();
        super.setStatus(status);
        if (assignedCollaborator != null && previous == Status.OPEN && status != Status.OPEN) {
            assignedCollaborator.releaseSubTask(this);
        }
    }

    public boolean hasCollaborator() { return assignedCollaborator != null; }
    public Collaborator getAssignedCollaborator() { return assignedCollaborator; }
    public void setAssignedCollaborator(Collaborator collab) { this.assignedCollaborator = collab; }
    public Task getParentTask() { return parentTask; }
    public void setParentTask(Task parentTask) { this.parentTask = parentTask; }

    @Override
    public String toString() {
        String collabName = assignedCollaborator != null ? assignedCollaborator.getName() : "unassigned";
        String parentTitle = parentTask != null ? parentTask.getTitle() : "none";
        return "SubTask{id='" + getId() + "', title='" + getTitle() + "', status=" + getStatus() + ", collaborator='" + collabName + "', parentTask='" + parentTitle + "'}";
    }
}
