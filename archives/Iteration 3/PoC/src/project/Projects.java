package project;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import tag.Tag;

public class Projects {
    private static final String DB_URL = "jdbc:sqlite:data/projects.db";

    private final HashMap<String, Project> projectMap;
    private final Connection conn;

    public Projects() {
        this.projectMap = new HashMap<>();
        try {
            this.conn = DriverManager.getConnection(DB_URL);
            initSchema();
            loadAll();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize projects database: " + e.getMessage(), e);
        }
    }

    private void initSchema() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS projects (" +
                "  name        TEXT PRIMARY KEY," +
                "  description TEXT NOT NULL DEFAULT ''" +
                ")"
            );
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS project_tags (" +
                "  project_name TEXT NOT NULL," +
                "  tag_name     TEXT NOT NULL," +
                "  PRIMARY KEY (project_name, tag_name)," +
                "  FOREIGN KEY (project_name) REFERENCES projects(name)" +
                ")"
            );
        }
    }

    private void loadAll() throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name, description FROM projects")) {
            while (rs.next()) {
                String name = rs.getString("name");
                String desc = rs.getString("description");
                projectMap.put(name.toLowerCase(), new Project(name, desc));
            }
        }
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT project_name, tag_name FROM project_tags")) {
            while (rs.next()) {
                Project proj = projectMap.get(rs.getString("project_name").toLowerCase());
                if (proj != null) proj.addTag(new Tag(rs.getString("tag_name")));
            }
        }
    }

    public Project addProject(String name, String description) {
        if (projectMap.containsKey(name.toLowerCase())) {
            throw new IllegalArgumentException("A project with the name '" + name + "' already exists.");
        }
        Project project = new Project(name, description);
        projectMap.put(name.toLowerCase(), project);
        saveProject(project);
        return project;
    }

    public Project findOrCreateProject(String name, String description) {
        return projectMap.computeIfAbsent(name.toLowerCase(), k -> {
            Project project = new Project(name, description);
            saveProject(project);
            return project;
        });
    }

    public void addTagToProject(Project project, Tag tag) {
        project.addTag(tag);
        String sql = "INSERT OR IGNORE INTO project_tags(project_name, tag_name) VALUES(?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, project.getName());
            ps.setString(2, tag.getName());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save project tag: " + e.getMessage(), e);
        }
    }

    private void saveProject(Project project) {
        String sql = "INSERT OR IGNORE INTO projects(name, description) VALUES(?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, project.getName());
            ps.setString(2, project.getDescription() != null ? project.getDescription() : "");
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save project: " + e.getMessage(), e);
        }
    }

    public Project findProject(String name) {
        return projectMap.get(name.toLowerCase());
    }

    public ArrayList<Project> getProjects() {
        return new ArrayList<>(projectMap.values());
    }
}
