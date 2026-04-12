import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import collaborator.Collaborator;
import collaborator.CollaboratorCat;
import collaborator.CollaboratorService;
import history.HistoryLog;
import history.HistoryService;
import ical.CalendarExportGateway;
import ical.ICalCalendarGateway;
import project.Project;
import project.Projects;
import tag.Tag;
import task.PriorityLevel;
import task.RecurrencePattern;
import task.Status;
import task.SubTask;
import task.Task;
import task.TaskService;

public class ApplicationControler {
    private final Projects projects;
    private final CollaboratorService collaboratorService;
    private final TaskService taskService;
    private final HistoryService historyService;
    private final CalendarExportGateway calendarGateway;

    public ApplicationControler() {
        this.projects            = new Projects();
        this.collaboratorService = new CollaboratorService(projects);
        this.taskService         = new TaskService(projects);
        this.historyService      = new HistoryService();
        this.calendarGateway     = new ICalCalendarGateway();
    }

    public void exportToCalendar(String taskTitle, String dueDateStr, String filePath) throws IOException {
        LocalDate dueDate = LocalDate.parse(dueDateStr);
        Task task = taskService.findTaskByNameAndDate(taskTitle, dueDate);
        if (task == null) {
            System.out.println("Task not found.");
            return;
        }
        calendarGateway.exportTasks(List.of(task), filePath);
    }

    public void exportProjectTasksToCalendar(String projectName, String filePath) throws IOException {
        List<Task> tasks = taskService.getAllTasks().stream()
            .filter(t -> t.getProject().getName().equalsIgnoreCase(projectName))
            .collect(Collectors.toList());
        calendarGateway.exportTasks(tasks, filePath);
    }

    public void exportFilteredTasksToCalendar(String name, String period, String dayOfWeek, String status, String filePath) throws IOException {
        List<Task> tasks = taskService.filterSearch(name, status, period, "", dayOfWeek, "");
        calendarGateway.exportTasks(tasks, filePath);
    }

    public List<Task> searchTasks(String name, String statusStr, String startDateStr, String endDateStr, String dayOfWeekStr, String tag) {
        return taskService.filterSearch(name, statusStr, startDateStr, endDateStr, dayOfWeekStr, tag);
    }

    public List<Collaborator> getOverloadedCollaborators() {
        return collaboratorService.getOverloadedCollaborators();
    }


    public void addTagToTask(String taskTitle, String dueDateStr, String tagName) {
        LocalDate dueDate = LocalDate.parse(dueDateStr);
        Task task = taskService.findTaskByNameAndDate(taskTitle, dueDate);
        if (task == null) throw new IllegalArgumentException("Task not found: " + taskTitle + " / " + dueDateStr);
        taskService.addTagToTask(task, new Tag(tagName));
        historyService.record(task, "Tag '" + tagName.toLowerCase().trim() + "' added.");
    }

    public void addTagToProject(String projectName, String tagName) {
        Project proj = projects.findProject(projectName);
        if (proj == null) throw new IllegalArgumentException("Project not found: " + projectName);
        projects.addTagToProject(proj, new Tag(tagName));
    }

    public List<HistoryLog> getTaskHistory(String taskTitle, String dueDateStr) {
        return historyService.getHistory(taskTitle, dueDateStr);
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
                String tagsStr         = get(fields, 10);

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
                    historyService.record(currentTask, "Task created.");

                    if (!statusStr.isEmpty()) {
                        try {
                            Status newStatus = Status.valueOf(statusStr.toUpperCase());
                            currentTask.setStatus(newStatus);
                            taskService.saveTask(currentTask);
                            historyService.record(currentTask, "Status set to " + newStatus + ".");
                        } catch (IllegalArgumentException ignored) {}
                    }
                    if (!priorityStr.isEmpty()) {
                        try {
                            PriorityLevel newPriority = PriorityLevel.valueOf(priorityStr.toUpperCase());
                            currentTask.setPriorityLevel(newPriority);
                            taskService.saveTask(currentTask);
                            historyService.record(currentTask, "Priority set to " + newPriority + ".");
                        } catch (IllegalArgumentException ignored) {}
                    }
                }

                if (!subtaskName.isEmpty()) {
                    SubTask subTask;
                    if (!collaboratorName.isEmpty()) {
                        Collaborator collab = proj.findCollaboratorByName(collaboratorName);
                        if (collab == null) {
                            CollaboratorCat cat = CollaboratorCat.JUNIOR;
                            if (!collaboratorCat.isEmpty()) {
                                try { cat = CollaboratorCat.valueOf(collaboratorCat.toUpperCase()); }
                                catch (IllegalArgumentException ignored) {}
                            }
                            collab = collaboratorService.addCollaborator(proj, collaboratorName, cat);
                        }
                        try {
                            subTask = proj.assignCollaboratorToTask(collab, currentTask, subtaskName);
                            historyService.record(currentTask,
                                "Subtask '" + subtaskName + "' added and assigned to " + collaboratorName + ".");
                        } catch (IllegalStateException e) {
                            System.out.println("Warning: " + e.getMessage() + " Adding subtask without collaborator.");
                            subTask = new SubTask(subtaskName);
                            currentTask.addSubTask(subTask);
                            historyService.record(currentTask, "Subtask '" + subtaskName + "' added.");
                        }
                    } else {
                        subTask = new SubTask(subtaskName);
                        currentTask.addSubTask(subTask);
                        historyService.record(currentTask, "Subtask '" + subtaskName + "' added.");
                    }
                    taskService.saveSubTask(subTask, currentTask);
                }

                if (!tagsStr.isEmpty()) {
                    for (String rawTag : tagsStr.split(",")) {
                        String tagName = rawTag.trim();
                        if (!tagName.isEmpty()) {
                            taskService.addTagToTask(currentTask, new Tag(tagName));
                        }
                    }
                }
            }
        }
    }

    public void exportToCSV(String filePath) throws IOException {
        List<Task> tasks = taskService.getAllTasks();
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.println("TaskName,Description,Subtask,Status,Priority,DueDate,ProjectName,ProjectDescription,Collaborator,CollaboratorCategory,Tags");
            for (Task t : tasks) {
                String taskName = escapeCSV(t.getTitle());
                String desc = escapeCSV(t.getDescription());
                String status = t.getStatus().name();
                String priority = t.getPriorityLevel().name();
                String dueDate = t.getDueDate().toString();
                String projName = escapeCSV(t.getProject().getName());
                String projDesc = escapeCSV(t.getProject().getDescription());
                String tagsStr = t.getTags().isEmpty() ? ""
                    : escapeCSV(t.getTags().stream()
                        .map(tag -> tag.getName())
                        .collect(java.util.stream.Collectors.joining(",")));

                List<SubTask> subtasks = t.getSubTasks();
                if (subtasks.isEmpty()) {
                    pw.println(taskName + "," + desc + ",," + status + "," + priority + ","
                            + dueDate + "," + projName + "," + projDesc + ",," + "," + tagsStr);
                } else {
                    for (SubTask st : subtasks) {
                        String stName = escapeCSV(st.getTitle());
                        String collabName = "";
                        String collabCat  = "";
                        if (st.getAssignedCollaborator() != null) {
                            collabName = escapeCSV(st.getAssignedCollaborator().getName());
                            collabCat  = st.getAssignedCollaborator().getType().name();
                        }
                        pw.println(
                            taskName + "," + desc + "," + stName + "," + status + ","
                            + priority + "," + dueDate + "," + projName + "," + projDesc
                            + "," + collabName + "," + collabCat + "," + tagsStr
                        );
                    }
                }
            }
        }
    }

    public void updateTask(String taskTitle, String dueDateStr, String newTitle, String newDescription, String newStatusStr, String newPriorityStr, String newDueDateStr, String newProjectName, String newTagsStr, RecurrencePattern recurrencePattern) {
        Task task;
        
        // If due date is provided, search by name and date; otherwise search by name only
        if (dueDateStr != null && !dueDateStr.trim().isEmpty()) {
            try {
                LocalDate dueDate = LocalDate.parse(dueDateStr.trim());
                task = taskService.findTaskByNameAndDate(taskTitle, dueDate);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid due date format: " + dueDateStr);
            }
        } else {
            task = taskService.findTaskByName(taskTitle);
        }
        
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskTitle + (dueDateStr != null && !dueDateStr.trim().isEmpty() ? " on " + dueDateStr : ""));
        }

        String oldTitle = task.getTitle();
        LocalDate oldDueDate = task.getDueDate();
        boolean titleChanged = false;
        boolean dueDateChanged = false;
        boolean tagsChanged = newTagsStr != null && !newTagsStr.trim().isEmpty();

        StringBuilder changes = new StringBuilder();

        if (newTitle != null && !newTitle.trim().isEmpty() && !newTitle.trim().equals(oldTitle)) {
            // Check uniqueness
            if (taskService.getAllTasks().stream().anyMatch(t -> t.getTitle().equals(newTitle.trim()) && !t.equals(task))) {
                throw new IllegalArgumentException("Task title already exists: " + newTitle);
            }
            titleChanged = true;
            changes.append("Title changed to '").append(newTitle.trim()).append("'. ");
        }

        if (newDescription != null && !newDescription.trim().isEmpty()) {
            task.setDescription(newDescription.trim());
            changes.append("Description updated. ");
        }

        if (newStatusStr != null && !newStatusStr.trim().isEmpty()) {
            try {
                Status newStatus = Status.valueOf(newStatusStr.toUpperCase().trim());
                task.setStatus(newStatus);
                changes.append("Status set to ").append(newStatus).append(". ");
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status: " + newStatusStr);
            }
        }

        if (newPriorityStr != null && !newPriorityStr.trim().isEmpty()) {
            try {
                PriorityLevel newPriority = PriorityLevel.valueOf(newPriorityStr.toUpperCase().trim());
                task.setPriorityLevel(newPriority);
                changes.append("Priority set to ").append(newPriority).append(". ");
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid priority: " + newPriorityStr);
            }
        }

        if (newDueDateStr != null && !newDueDateStr.trim().isEmpty()) {
            try {
                LocalDate newDueDate = LocalDate.parse(newDueDateStr.trim());
                if (!newDueDate.equals(oldDueDate)) {
                    dueDateChanged = true;
                    task.setDueDate(newDueDate);
                    changes.append("Due date set to ").append(newDueDate).append(". ");
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid due date: " + newDueDateStr);
            }
        }

        if (newProjectName != null && !newProjectName.trim().isEmpty()) {
            Project newProject = projects.findProject(newProjectName.trim());
            if (newProject == null) {
                throw new IllegalArgumentException("Project not found: " + newProjectName);
            }
            task.setProject(newProject);
            changes.append("Project changed to '").append(newProject.getName()).append("'. ");
        }

        if (tagsChanged) {
            // Clear current tags
            task.getTags().clear();
            // Add new tags
            for (String tagName : newTagsStr.split(",")) {
                String trimmed = tagName.trim();
                if (!trimmed.isEmpty()) {
                    task.addTag(new Tag(trimmed));
                }
            }
            changes.append("Tags updated. ");
        }

        if (recurrencePattern != null) {
            task.setRecurrencePattern(recurrencePattern);
            changes.append("Recurrence details updated. ");
        }

        if (titleChanged) {
            task.setTitle(newTitle.trim());
        }

        if (changes.length() > 0) {
            if (titleChanged || dueDateChanged) {
                // Delete old database records
                taskService.deleteTaskWithOldKey(oldTitle, oldDueDate);
            }
            if (tagsChanged) {
                // Delete old tags
                taskService.deleteTagsForTask(oldTitle, oldDueDate);
            }
            taskService.saveTask(task);
            // Save subtasks and tags with new keys
            for (SubTask st : task.getSubTasks()) {
                taskService.saveSubTask(st, task);
            }
            for (Tag tag : task.getTags()) {
                taskService.addTagToTask(task, tag);
            }
            historyService.record(task, changes.toString().trim());
        } else {
            throw new IllegalArgumentException("No valid updates provided.");
        }
    }

    public void createTask(String title, LocalDate dueDate, String description, String projectName, PriorityLevel priority) {
        if (dueDate == null) {
            throw new IllegalArgumentException("Due date is required.");
        }
        Project project = projects.findOrCreateProject(projectName, "");
        Task task = taskService.createTask(title, dueDate, description, project);
        task.setPriorityLevel(priority);
        taskService.saveTask(task);
        historyService.record(task, "Task created.");
    }

    public void createTask(String title, LocalDate dueDate, String description, String projectName, PriorityLevel priority, RecurrencePattern recurrencePattern) {
        if (dueDate == null) {
            throw new IllegalArgumentException("Due date is required.");
        }
        if (recurrencePattern == null) {
            throw new IllegalArgumentException("Recurrence pattern is required for this method.");
        }
        Project project = projects.findOrCreateProject(projectName, "");
        Task task = taskService.createTask(title, dueDate, description, project);
        task.setPriorityLevel(priority);
        task.setRecurrencePattern(recurrencePattern);
        taskService.saveTask(task);
        historyService.record(task, "Task created with recurrence pattern (" + recurrencePattern.getType() + ", interval: " + recurrencePattern.getInterval() + ").");
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
                switch (c) {
                    case '"':
                        if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                            sb.append('"');
                            i++;
                        } else {
                            inQuotes = false;
                        }
                        break;
                    default: sb.append(c);
                }
            } else {
                switch (c) {
                    case '"': inQuotes = true; break;
                    case ',': fields.add(sb.toString()); sb.setLength(0); break;
                    default:  sb.append(c);
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
