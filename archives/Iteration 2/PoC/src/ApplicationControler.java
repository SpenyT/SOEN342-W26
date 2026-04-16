import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import project.Collaborator;
import project.CollaboratorCat;
import project.Project;
import project.Projects;
import task.PriorityLevel;
import task.Status;
import task.SubTask;
import task.Task;
import task.TaskService;

public class ApplicationControler {
    private final TaskService taskService;
    private final Projects projects;

    public ApplicationControler() {
        this.taskService = new TaskService();
        this.projects = new Projects();
    }

    public List<Task> searchTasks(String name, String statusStr, String startDateStr, String endDateStr, String dayOfWeekStr) {
        return taskService.filterSearch(name, statusStr, startDateStr, endDateStr, dayOfWeekStr);
    }

    public void importFromCSV(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                List<String> fields = parseCSVLine(line);
        
                String taskName = get(fields, 0);
                String description = get(fields, 1);
                String subtaskName = get(fields, 2);
                String statusStr = get(fields, 3);
                String priorityStr = get(fields, 4);
                String dueDateStr = get(fields, 5);
                String projectName = get(fields, 6);
                String projectDesc = get(fields, 7);
                String collaboratorName = get(fields, 8);
                String collaboratorCat = get(fields, 9);

                if (taskName.isEmpty() || dueDateStr.isEmpty()) continue;

                LocalDate dueDate;
                try {
                    dueDate = LocalDate.parse(dueDateStr);
                } catch (Exception e) {
                    System.out.println("Skipping row: invalid date '" + dueDateStr + "'");
                    continue;
                }

                Project proj = projects.findOrCreateProject(projectName, projectDesc);

                Task currentTask = taskService.findTaskByNameAndDate(taskName, dueDate);
                if (currentTask == null) {
                    currentTask = taskService.createTask(taskName, dueDate, description, proj);
                    if (!statusStr.isEmpty()) {
                        try { currentTask.setStatus(Status.valueOf(statusStr.toUpperCase())); }
                        catch (IllegalArgumentException ignored) {}
                    }
                    if (!priorityStr.isEmpty()) {
                        try { currentTask.setPriorityLevel(PriorityLevel.valueOf(priorityStr.toUpperCase())); }
                        catch (IllegalArgumentException ignored) {}
                    }
                }

                if (!subtaskName.isEmpty()) {
                    if (!collaboratorName.isEmpty()) {
                        Collaborator collab = proj.findCollaboratorByName(collaboratorName);
                        if (collab == null) {
                            CollaboratorCat cat = CollaboratorCat.JUNIOR;
                            if (!collaboratorCat.isEmpty()) {
                                try { cat = CollaboratorCat.valueOf(collaboratorCat.toUpperCase()); }
                                catch (IllegalArgumentException ignored) {}
                            }
                            collab = proj.addCollaborator(collaboratorName, cat);
                        }
                        try {
                            proj.assignCollaboratorToTask(collab, currentTask, subtaskName);
                        } catch (IllegalStateException e) {
                            System.out.println("Warning: " + e.getMessage() + " Adding subtask without collaborator.");
                            currentTask.addSubTask(new SubTask(subtaskName));
                        }
                    } else {
                        currentTask.addSubTask(new SubTask(subtaskName));
                    }
                }
            }
        }
    }

    public void exportToCSV(String filePath) throws IOException {
        List<Task> tasks = taskService.getAllTasks();
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.println("TaskName,Description,Subtask,Status,Priority,DueDate,ProjectName,ProjectDescription,Collaborator,CollaboratorCategory");
            for (Task t : tasks) {
                String taskName = escapeCSV(t.getTitle());
                String desc     = escapeCSV(t.getDescription());
                String status   = t.getStatus().name();
                String priority = t.getPriorityLevel().name();
                String dueDate  = t.getDueDate().toString();
                String projName = escapeCSV(t.getProject().getName());
                String projDesc = escapeCSV(t.getProject().getDescription());

                List<SubTask> subtasks = t.getSubTasks();
                if (subtasks.isEmpty()) {
                    pw.println(taskName + "," + desc + ",," + status + "," + priority + ","
                            + dueDate + "," + projName + "," + projDesc + ",,");
                } else {
                    for (SubTask st : subtasks) {
                        String stName     = escapeCSV(st.getTitle());
                        String collabName = "";
                        String collabCat  = "";
                        if (st.getAssignedCollaborator() != null) {
                            collabName = escapeCSV(st.getAssignedCollaborator().getName());
                            collabCat  = st.getAssignedCollaborator().getType().name();
                        }
                        pw.println(
                            taskName + "," + desc + "," + stName + "," + status + ","
                            + priority + "," + dueDate + "," + projName + "," + projDesc
                            + "," + collabName + "," + collabCat
                        );
                    }
                }
            }
        }
    }

    public int getTaskCount() {
        return taskService.getAllTasks().size();
    }

    private List<String> parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        sb.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    sb.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(sb.toString());
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
            }
        }
        fields.add(sb.toString());
        return fields;
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String get(List<String> fields, int index) {
        return index < fields.size() ? fields.get(index).trim() : "";
    }
}
