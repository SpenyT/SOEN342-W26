package collaborator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import project.Project;
import project.Projects;

public class CollaboratorService {
    private static final String DB_URL = "jdbc:sqlite:data/collaborator.db";

    private final List<Collaborator> collaborators;
    private final Connection conn;

    public CollaboratorService(Projects projects) {
        this.collaborators = new ArrayList<>();
        try {
            this.conn = DriverManager.getConnection(DB_URL);
            initSchema();
            loadAll(projects);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize collaborator database: " + e.getMessage(), e);
        }
    }

    private void initSchema() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS collaborators (" +
                "  name         TEXT NOT NULL," +
                "  project_name TEXT NOT NULL," +
                "  category     TEXT NOT NULL," +
                "  PRIMARY KEY (name, project_name)" +
                ")"
            );
        }
    }

    private void loadAll(Projects projects) throws SQLException {
        String sql = "SELECT name, project_name, category FROM collaborators";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String name = rs.getString("name");
                String projName = rs.getString("project_name");
                String catStr = rs.getString("category");

                Project proj = projects.findProject(projName);
                if (proj == null) continue;

                CollaboratorCat cat;
                try { cat = CollaboratorCat.valueOf(catStr); }
                catch (IllegalArgumentException e) { cat = CollaboratorCat.JUNIOR; }

                Collaborator collab = proj.addCollaborator(name, cat);
                collaborators.add(collab);
            }
        }
    }

    public Collaborator addCollaborator(Project project, String name, CollaboratorCat cat) {
        Collaborator collab = project.addCollaborator(name, cat);
        collaborators.add(collab);
        save(project.getName(), collab);
        return collab;
    }

    private void save(String projectName, Collaborator collaborator) {
        String sql = "INSERT OR IGNORE INTO collaborators(name, project_name, category) VALUES(?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, collaborator.getName());
            ps.setString(2, projectName);
            ps.setString(3, collaborator.getType().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save collaborator '" + collaborator.getName() + "': " + e.getMessage(), e);
        }
    }

    public List<Collaborator> getAllCollaborators() {
        return new ArrayList<>(collaborators);
    }

    public List<Collaborator> getOverloadedCollaborators() {
        List<Collaborator> overloaded = new ArrayList<>();
        for (Collaborator c : collaborators) {
            if (c.isAtCapacity()) overloaded.add(c);
        }
        return overloaded;
    }
}
