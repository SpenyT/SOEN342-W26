package project;

import java.util.ArrayList;
import java.util.HashMap;

public class Projects {
    private final HashMap<String, Project> projectMap;

    public Projects() {
        this.projectMap = new HashMap<>();
    }

    public Project addProject(String name, String description) {
        if (projectMap.containsKey(name.toLowerCase())) {
            throw new IllegalArgumentException("A project with the name '" + name + "' already exists.");
        }
        Project project = new Project(name, description);
        projectMap.put(name.toLowerCase(), project);
        return project;
    }

    public Project findProject(String name) {
        return projectMap.get(name.toLowerCase());
    }

    public Project findOrCreateProject(String name, String description) {
        return projectMap.computeIfAbsent(name.toLowerCase(), k -> new Project(name, description));
    }

    public ArrayList<Project> getProjects() {
        return new ArrayList<>(projectMap.values());
    }
}
