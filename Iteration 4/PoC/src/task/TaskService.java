package task;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import collaborator.Collaborator;
import project.Project;
import project.Projects;
import tag.Tag;

public class TaskService {
    private static final String DB_URL = "jdbc:sqlite:data/tasks.db";

    private final List<Task> tasks;
    private final Connection conn;

    public TaskService(Projects projects) {
        this.tasks = new ArrayList<>();
        try {
            this.conn = DriverManager.getConnection(DB_URL);
            initSchema();
            loadAll(projects);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize tasks database: " + e.getMessage(), e);
        }
    }

    private void initSchema() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS tasks (" +
                "  id                  TEXT    NOT NULL," +
                "  title               TEXT    NOT NULL," +
                "  due_date            TEXT    NOT NULL," +
                "  description         TEXT    NOT NULL DEFAULT ''," +
                "  status              TEXT    NOT NULL DEFAULT 'OPEN'," +
                "  priority            TEXT    NOT NULL DEFAULT 'DEFAULT'," +
                "  creation_date       TEXT    NOT NULL," +
                "  is_recurring        INTEGER NOT NULL DEFAULT 0," +
                "  project_name        TEXT    NOT NULL," +
                "  parent_title        TEXT," +
                "  parent_due_date     TEXT," +
                "  recurrence_type     TEXT," +
                "  recurrence_interval INTEGER," +
                "  recurrence_end      TEXT," +
                "  recurrence_days     TEXT," +
                "  recurrence_dom      INTEGER," +
                "  PRIMARY KEY (title, due_date)" +
                ")"
            );
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS subtasks (" +
                "  id                   TEXT NOT NULL," +
                "  task_title           TEXT NOT NULL," +
                "  task_due_date        TEXT NOT NULL," +
                "  title                TEXT NOT NULL," +
                "  status               TEXT NOT NULL DEFAULT 'OPEN'," +
                "  collaborator_project TEXT," +
                "  collaborator_name    TEXT," +
                "  PRIMARY KEY (task_title, task_due_date, title)," +
                "  FOREIGN KEY (task_title, task_due_date) REFERENCES tasks(title, due_date)" +
                ")"
            );
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS task_tags (" +
                "  task_title    TEXT NOT NULL," +
                "  task_due_date TEXT NOT NULL," +
                "  tag_name      TEXT NOT NULL," +
                "  PRIMARY KEY (task_title, task_due_date, tag_name)," +
                "  FOREIGN KEY (task_title, task_due_date) REFERENCES tasks(title, due_date)" +
                ")"
            );
        }
    }

    private void loadAll(Projects projects) throws SQLException {
        Map<String, Task> taskMap = loadTasks(projects);
        linkOccurrences(taskMap);
        loadSubTasks(taskMap, projects);
        loadTags(taskMap);
        tasks.addAll(taskMap.values());
    }

    private Map<String, Task> loadTasks(Projects projects) throws SQLException {
        Map<String, Task> taskMap = new LinkedHashMap<>();
        String sql =
            "SELECT id, title, due_date, description, status, priority," +
            "       is_recurring, project_name," +
            "       recurrence_type, recurrence_interval, recurrence_end," +
            "       recurrence_days, recurrence_dom" +
            " FROM tasks";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String id    = rs.getString("id");
                String title = rs.getString("title");
                LocalDate dueDate = LocalDate.parse(rs.getString("due_date"));
                String desc = rs.getString("description");
                String projName = rs.getString("project_name");
                Project proj = projects.findProject(projName);
                if (proj == null) continue;

                Task task = new Task(title, dueDate, desc, proj, id);
                try { task.setStatus(Status.valueOf(rs.getString("status"))); }
                catch (IllegalArgumentException ignored) {}
                try { task.setPriorityLevel(PriorityLevel.valueOf(rs.getString("priority"))); }
                catch (IllegalArgumentException ignored) {}

                String recType = rs.getString("recurrence_type");
                if (recType != null && !recType.isEmpty()) {
                    try {
                        RecurrenceType rType = RecurrenceType.valueOf(recType);
                        int interval = rs.getInt("recurrence_interval");
                        LocalDate endDate = LocalDate.parse(rs.getString("recurrence_end"));
                        RecurrencePattern rp  = new RecurrencePattern(rType, interval, dueDate, endDate);
                        String daysStr = rs.getString("recurrence_days");
                        if (daysStr != null && !daysStr.isEmpty()) {
                            List<DayOfWeek> days = Arrays.stream(daysStr.split(","))
                                .map(String::trim).filter(s -> !s.isEmpty())
                                .map(DayOfWeek::valueOf).collect(Collectors.toList());
                            rp.setSelectedDays(days);
                        }
                        int dom = rs.getInt("recurrence_dom");
                        if (dom > 0) rp.setDayOfMonth(dom);
                        task.setRecurrencePattern(rp);
                    } catch (Exception ignored) {}
                }

                taskMap.put(key(title, dueDate), task);
            }
        }
        return taskMap;
    }

    private void linkOccurrences(Map<String, Task> taskMap) throws SQLException {
        String sql = "SELECT title, due_date, parent_title, parent_due_date FROM tasks WHERE parent_title IS NOT NULL";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Task child  = taskMap.get(key(rs.getString("title"), LocalDate.parse(rs.getString("due_date"))));
                Task parent = taskMap.get(key(rs.getString("parent_title"), LocalDate.parse(rs.getString("parent_due_date"))));
                if (child != null && parent != null) parent.addOccurrence(child);
            }
        }
    }

    private void loadSubTasks(Map<String, Task> taskMap, Projects projects) throws SQLException {
        String sql =
            "SELECT id, task_title, task_due_date, title, status, collaborator_project, collaborator_name" +
            " FROM subtasks";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Task parentTask = taskMap.get(key(rs.getString("task_title"), LocalDate.parse(rs.getString("task_due_date"))));
                if (parentTask == null) continue;

                SubTask st = new SubTask(rs.getString("title"), rs.getString("id"));
                try { st.setStatus(Status.valueOf(rs.getString("status"))); }
                catch (IllegalArgumentException ignored) {}

                String collabProject = rs.getString("collaborator_project");
                String collabName    = rs.getString("collaborator_name");
                if (collabName != null && !collabName.isEmpty() && collabProject != null) {
                    Project proj = projects.findProject(collabProject);
                    if (proj != null) {
                        Collaborator collab = proj.findCollaboratorByName(collabName);
                        if (collab != null) {
                            st.setAssignedCollaborator(collab);
                            collab.restoreAssignment(st, parentTask.getStatus() == Status.OPEN);
                        }
                    }
                }
                parentTask.addSubTask(st);
            }
        }
    }

    private void loadTags(Map<String, Task> taskMap) throws SQLException {
        String sql = "SELECT task_title, task_due_date, tag_name FROM task_tags";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Task task = taskMap.get(key(rs.getString("task_title"), LocalDate.parse(rs.getString("task_due_date"))));
                if (task != null) task.addTag(new Tag(rs.getString("tag_name")));
            }
        }
    }

    public Task createTask(String title, LocalDate dueDate, String description, Project project) {
        boolean exists = tasks.stream().anyMatch(t -> t.getTitle().equals(title) && t.getDueDate().equals(dueDate));
        if (exists) {
            throw new IllegalArgumentException("Task name + due date already exists.");
        }
        Task t = new Task(title, dueDate, description, project);
        tasks.add(t);
        saveTask(t);
        return t;
    }

    public Task createRecurringTask(String title, String description, Project project, RecurrencePattern pattern) {
        Task template = new Task(title, pattern.getStartDate(), description, project);
        template.setRecurrencePattern(pattern);
        tasks.add(template);
        saveTask(template);

        for (LocalDate date : pattern.generateOccurrenceDates()) {
            if (findTaskByNameAndDate(title, date) != null) continue;
            Task occurrence = new Task(title, date, description, project);
            occurrence.setPriorityLevel(template.getPriorityLevel());
            template.addOccurrence(occurrence);
            tasks.add(occurrence);
            saveTask(occurrence);
        }
        return template;
    }

    public void saveTask(Task task) {
        String sql =
            "INSERT OR REPLACE INTO tasks(" +
            "  id, title, due_date, description, status, priority, creation_date," +
            "  is_recurring, project_name, parent_title, parent_due_date," +
            "  recurrence_type, recurrence_interval, recurrence_end, recurrence_days, recurrence_dom" +
            ") VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            RecurrencePattern rp = task.getRecurrencePattern();
            ps.setString(1,  task.getId());
            ps.setString(2,  task.getTitle());
            ps.setString(3,  task.getDueDate().toString());
            ps.setString(4,  task.getDescription() != null ? task.getDescription() : "");
            ps.setString(5,  task.getStatus().name());
            ps.setString(6,  task.getPriorityLevel().name());
            ps.setString(7,  task.getCreationDate().toString());
            ps.setInt(8,     task.isRecurring() ? 1 : 0);
            ps.setString(9,  task.getProject().getName());
            if (task.getParentTask() != null) {
                ps.setString(10, task.getParentTask().getTitle());
                ps.setString(11, task.getParentTask().getDueDate().toString());
            } else {
                ps.setNull(10, Types.VARCHAR);
                ps.setNull(11, Types.VARCHAR);
            }
            if (rp != null) {
                ps.setString(12, rp.getType().name());
                ps.setInt(13,    rp.getInterval());
                ps.setString(14, rp.getEndDate().toString());
                String days = rp.getSelectedDays().stream().map(DayOfWeek::name).collect(Collectors.joining(","));
                ps.setString(15, days);
                ps.setInt(16,    rp.getDayOfMonth());
            } else {
                ps.setNull(12, Types.VARCHAR);
                ps.setNull(13, Types.INTEGER);
                ps.setNull(14, Types.VARCHAR);
                ps.setNull(15, Types.VARCHAR);
                ps.setNull(16, Types.INTEGER);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save task '" + task.getTitle() + "': " + e.getMessage(), e);
        }
    }

    public void saveSubTask(SubTask subTask, Task parentTask) {
        String sql =
            "INSERT OR REPLACE INTO subtasks(" +
            "  id, task_title, task_due_date, title, status, collaborator_project, collaborator_name" +
            ") VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subTask.getId());
            ps.setString(2, parentTask.getTitle());
            ps.setString(3, parentTask.getDueDate().toString());
            ps.setString(4, subTask.getTitle());
            ps.setString(5, subTask.getStatus().name());
            Collaborator collab = subTask.getAssignedCollaborator();
            if (collab != null) {
                ps.setString(6, parentTask.getProject().getName());
                ps.setString(7, collab.getName());
            } else {
                ps.setNull(6, Types.VARCHAR);
                ps.setNull(7, Types.VARCHAR);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save subtask '" + subTask.getTitle() + "': " + e.getMessage(), e);
        }
    }

    public void addTagToTask(Task task, Tag tag) {
        task.addTag(tag);
        String sql = "INSERT OR IGNORE INTO task_tags(task_title, task_due_date, tag_name) VALUES(?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDueDate().toString());
            ps.setString(3, tag.getName());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save tag '" + tag.getName() + "': " + e.getMessage(), e);
        }
    }

    public List<Task> filterSearch(String name, String statusStr, String startDateStr, String endDateStr,
                                   String dayOfWeekStr, String tagStr) {
        List<Task> result = new ArrayList<>(tasks);

        if (!isBlank(name)) {
            String lower = name.toLowerCase();
            result = result.stream().filter(t -> t.getTitle().toLowerCase().contains(lower)).collect(Collectors.toList());
        }
        if (!isBlank(statusStr)) {
            try {
                Status s = Status.valueOf(statusStr.toUpperCase());
                result = result.stream().filter(t -> t.getStatus() == s).collect(Collectors.toList());
            } catch (Exception ignored) {}
        }
        if (!isBlank(startDateStr)) {
            try {
                LocalDate start = LocalDate.parse(startDateStr);
                result = result.stream().filter(t -> !t.getDueDate().isBefore(start)).collect(Collectors.toList());
            } catch (Exception ignored) {}
        }
        if (!isBlank(endDateStr)) {
            try {
                LocalDate end = LocalDate.parse(endDateStr);
                result = result.stream().filter(t -> !t.getDueDate().isAfter(end)).collect(Collectors.toList());
            } catch (Exception ignored) {}
        }
        if (!isBlank(dayOfWeekStr)) {
            try {
                DayOfWeek dow = DayOfWeek.valueOf(dayOfWeekStr.toUpperCase());
                result = result.stream().filter(t -> t.getDueDate().getDayOfWeek() == dow).collect(Collectors.toList());
            } catch (IllegalArgumentException ignored) {}
        }
        if (!isBlank(tagStr)) {
            String lower = tagStr.toLowerCase().trim();
            result = result.stream().filter(t -> t.hasTag(lower)).collect(Collectors.toList());
        }

        result.sort(Comparator.comparing(Task::getDueDate));
        return result;
    }

    public Task findTaskByNameAndDate(String title, LocalDate dueDate) {
        return tasks.stream()
                .filter(t -> t.getTitle().equals(title) && t.getDueDate().equals(dueDate))
                .findFirst().orElse(null);
    }

    public Task findTaskByName(String title) {
        return tasks.stream()
                .filter(t -> t.getTitle().equals(title))
                .findFirst().orElse(null);
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String key(String title, LocalDate date) {
        return title + "|" + date.toString();
    }

    public void deleteTaskWithOldKey(String title, LocalDate dueDate) {
        try {
            String sql1 = "DELETE FROM subtasks WHERE task_title = ? AND task_due_date = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql1)) {
                ps.setString(1, title);
                ps.setString(2, dueDate.toString());
                ps.executeUpdate();
            }
            String sql2 = "DELETE FROM task_tags WHERE task_title = ? AND task_due_date = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql2)) {
                ps.setString(1, title);
                ps.setString(2, dueDate.toString());
                ps.executeUpdate();
            }
            String sql3 = "DELETE FROM tasks WHERE title = ? AND due_date = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql3)) {
                ps.setString(1, title);
                ps.setString(2, dueDate.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete task '" + title + "': " + e.getMessage(), e);
        }
    }

    public void deleteTagsForTask(String title, LocalDate dueDate) {
        String sql = "DELETE FROM task_tags"
                   + " WHERE task_title = ? AND task_due_date = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, dueDate.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete tags for task '" + title + "': " + e.getMessage(), e);
        }
    }
}