package project;

import java.util.ArrayList;
import task.SubTask;
import task.Task;

public class Project {
    private final String name;
    private String description;
    private final ArrayList<Collaborator> collaborators;

    public Project(String name, String description) {
        this.name = name;
        this.description = description;
        this.collaborators = new ArrayList<>();
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ArrayList<Collaborator> getCollaborators() { return collaborators; }

    public Collaborator addCollaborator(String collaboratorName, CollaboratorCat type) {
        Collaborator collaborator = new Collaborator(collaboratorName, type);
        collaborators.add(collaborator);
        return collaborator;
    }

    public Collaborator findCollaboratorByName(String collaboratorName) {
        return collaborators.stream()
                .filter(c -> c.getName().equalsIgnoreCase(collaboratorName))
                .findFirst()
                .orElse(null);
    }

    public SubTask assignCollaboratorToTask(Collaborator collaborator, Task t, String subtaskName) {
        if (collaborator.isAtCapacity()) {
            throw new IllegalStateException(
                "Collaborator '" + collaborator.getName() + "' is at capacity ("
                + collaborator.getNOpenTasks() + "/" + collaborator.getMaxTasks() + ")."
            );
        }
        SubTask subTask = new SubTask(subtaskName);
        t.addSubTask(subTask);
        subTask.assignCollaborator(collaborator);
        return subTask;
    }

    @Override
    public String toString() {
        return "Project{name='" + name + "', description='" + description + "', collaborators=" + collaborators + "}";
    }
}
