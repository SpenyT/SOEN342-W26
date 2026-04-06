package task;

import collaborator.Collaborator;

public class SubTask extends WorkItem {
    private Collaborator assignedCollaborator;

    public SubTask(String title) {
        super(title);
        this.assignedCollaborator = null;
    }

    public SubTask(String title, Collaborator collaborator) {
        super(title);
        this.assignedCollaborator = collaborator;
    }

    public void assignCollaborator(Collaborator collaborator) {
        collaborator.assignSubTask(this);
        this.assignedCollaborator = collaborator;
    }

    public boolean hasCollaborator() { return assignedCollaborator != null; }
    public Collaborator getAssignedCollaborator() { return assignedCollaborator; }
    public void setAssignedCollaborator(Collaborator collab) { this.assignedCollaborator = collab; }
}
