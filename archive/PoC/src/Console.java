import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import collaborator.Collaborator;
import history.HistoryLog;
import task.PriorityLevel;
import task.RecurrencePattern;
import task.RecurrenceType;
import task.SubTask;
import task.Task;

public class Console {
    private final ApplicationControler controller;
    private final Scanner scanner;

    public Console() {
        this.controller = new ApplicationControler();
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": importCsv();               break;
                case "2": exportCsv();               break;
                case "3": createTask();              break;
                case "4": updateTask();              break;
                case "5": search();                  break;
                case "6": viewHistory();             break;
                case "7": exportToCalendar();        break;
                case "8": listOverloadedCollaborators(); break;
                case "9":
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please enter 1-9.");
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("||========================================||");
        System.out.println("||           Task Scheduler App           ||");
        System.out.println("||========================================||");
        System.out.println("|| 1. Import Tasks from CSV               ||");
        System.out.println("|| 2. Export Tasks to CSV                 ||");
        System.out.println("|| 3. Create Task                         ||");
        System.out.println("|| 4. Update Task                         ||");
        System.out.println("|| 5. Search Tasks                        ||");
        System.out.println("|| 6. View Task History                   ||");
        System.out.println("|| 7. Export Tasks to iCalendar (.ics)    ||");
        System.out.println("|| 8. List Overloaded Collaborators       ||");
        System.out.println("|| 9. Exit                                ||");
        System.out.println("||========================================||");
        System.out.print(  "  Choice: ");
    }

    private void importCsv() {
        System.out.print("CSV file path: ");
        String path = scanner.nextLine().trim();
        try {
            controller.importFromCSV(path);
            System.out.println("Import complete. " + controller.getTaskCount() + " task(s) in system.");
        } catch (Exception e) {
            System.out.println("Import failed: " + e.getMessage());
        }
    }

    private void exportCsv() {
        System.out.print("Export file path: ");
        String path = scanner.nextLine().trim();
        try {
            controller.exportToCSV(path);
            System.out.println("Exported " + controller.getTaskCount() + " task(s) to " + path);
        } catch (Exception e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }

    private void search() {
        System.out.println();
        System.out.println("--- Task Search ---");
        System.out.println("Leave blank to skip a criterion.");
        System.out.println("(No criteria = all tasks regardless of status, sorted by due date)");
        System.out.println();

        System.out.print("Task name contains: ");
        String name = scanner.nextLine().trim();

        System.out.print("Status (OPEN/COMPLETED/CANCELLED): ");
        String status = scanner.nextLine().trim();

        System.out.print("Period start (yyyy-MM-dd): ");
        String startDate = scanner.nextLine().trim();

        System.out.print("Period end   (yyyy-MM-dd): ");
        String endDate = scanner.nextLine().trim();

        System.out.print("Day of week  (MONDAY..SUNDAY): ");
        String dayOfWeek = scanner.nextLine().trim();

        System.out.print("Tag: ");
        String tag = scanner.nextLine().trim();

        List<Task> results = controller.searchTasks(name, status, startDate, endDate, dayOfWeek, tag);
        printResults(results);
    }

    private void viewHistory() {
        System.out.println();
        System.out.println("--- View Task History ---");
        System.out.print("Task name (exact): ");
        String title = scanner.nextLine().trim();
        System.out.print("Due date (yyyy-MM-dd): ");
        String dueDate = scanner.nextLine().trim();

        if (title.isEmpty() || dueDate.isEmpty()) {
            System.out.println("Task name and due date are required.");
            return;
        }

        List<HistoryLog> logs = controller.getTaskHistory(title, dueDate);
        System.out.println();
        System.out.println("--- History for '" + title + "' (" + dueDate + ") --- " + logs.size() + " entry(s) ---");
        if (logs.isEmpty()) {
            System.out.println("No history found.");
        } else {
            for (HistoryLog log : logs) {
                System.out.println("  " + log);
            }
        }
        System.out.println("----------------------------------------------------------");
    }

    private void exportToCalendar() {
        System.out.println();
        System.out.println("--- Export to iCalendar ---");
        System.out.println("a) Single task");
        System.out.println("b) All tasks in a project");
        System.out.println("c) Filtered list of tasks");
        System.out.print("  Choice (a/b/c): ");
        String mode = scanner.nextLine().trim().toLowerCase();

        System.out.print("Output file path (without .ics): ");
        String filePath = scanner.nextLine().trim();

        try {
            switch (mode) {
                case "a":
                    System.out.print("Task name (exact): ");
                    String taskName = scanner.nextLine().trim();
                    System.out.print("Due date (yyyy-MM-dd): ");
                    String dueDate = scanner.nextLine().trim();
                    controller.exportToCalendar(taskName, dueDate, filePath);
                    break;
                case "b":
                    System.out.print("Project name: ");
                    String project = scanner.nextLine().trim();
                    controller.exportProjectTasksToCalendar(project, filePath);
                    break;
                case "c":
                    System.out.print("Name contains (blank = all): ");
                    String name = scanner.nextLine().trim();
                    System.out.print("Period start (yyyy-MM-dd, blank = none): ");
                    String period = scanner.nextLine().trim();
                    System.out.print("Day of week (MONDAY..SUNDAY, blank = any): ");
                    String day = scanner.nextLine().trim();
                    System.out.print("Status (OPEN/COMPLETED/CANCELLED, blank = OPEN): ");
                    String status = scanner.nextLine().trim();
                    controller.exportFilteredTasksToCalendar(name, period, day, status, filePath);
                    break;
                default:
                    System.out.println("Invalid choice.");
                    return;
            }
            System.out.println("Exported to " + filePath + ".ics");
        } catch (Exception e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }

    private void listOverloadedCollaborators() {
        System.out.println();
        System.out.println("--- Overloaded Collaborators ---");
        List<Collaborator> overloaded = controller.getOverloadedCollaborators();
        if (overloaded.isEmpty()) {
            System.out.println("No collaborators are overloaded.");
        } else {
            for (Collaborator c : overloaded) {
                System.out.printf("  %s (%s) — %d/%d open tasks%n",
                        c.getName(), c.getType(), c.getNOpenTasks(), c.getMaxTasks());
            }
        }
        System.out.println("----------------------------------------------------------");
    }

    private void updateTask() {
        System.out.println();
        System.out.println("--- Update Task ---");
        System.out.print("Task name (exact): ");
        String taskName = scanner.nextLine().trim();

        if (taskName.isEmpty()) {
            System.out.println("Task name is required.");
            return;
        }

        System.out.print("Task due date (yyyy-MM-dd, leave blank if no due date): ");
        String dueDate = scanner.nextLine().trim();

        System.out.println("Leave fields blank to keep current values.");
        System.out.print("New title: ");
        String newTitle = scanner.nextLine().trim();
        System.out.print("New description: ");
        String description = scanner.nextLine().trim();
        System.out.print("New status (OPEN/COMPLETED/CANCELLED): ");
        String status = scanner.nextLine().trim();
        System.out.print("New priority (DEFAULT/LOW/MEDIUM/HIGH/CRITICAL): ");
        String priority = scanner.nextLine().trim();
        System.out.print("New due date (yyyy-MM-dd): ");
        String newDueDate = scanner.nextLine().trim();
        System.out.print("New project name: ");
        String newProject = scanner.nextLine().trim();
        System.out.print("New tags (comma-separated): ");
        String newTags = scanner.nextLine().trim();
        System.out.print("Change recurrence details? (y/n): ");
        String updateRecurrence = scanner.nextLine().trim().toLowerCase();
        RecurrencePattern recurrencePattern = null;

        if (updateRecurrence.equals("y") || updateRecurrence.equals("yes")) {
            System.out.println("--- Recurrence Pattern ---");
            System.out.print("Recurrence type (DAILY/WEEKLY/MONTHLY): ");
            String typeStr = scanner.nextLine().trim();
            RecurrenceType recurrenceType = null;
            try {
                recurrenceType = RecurrenceType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid recurrence type.");
                return;
            }

            System.out.print("Interval (every N days/weeks/months): ");
            String intervalStr = scanner.nextLine().trim();
            int interval = 1;
            try {
                interval = Integer.parseInt(intervalStr);
                if (interval < 1) {
                    System.out.println("Interval must be at least 1.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid interval.");
                return;
            }

            System.out.print("Recurrence start date (yyyy-MM-dd): ");
            String rcStartDateStr = scanner.nextLine().trim();
            if (rcStartDateStr.isEmpty()) {
                System.out.println("Recurrence start date is required.");
                return;
            }
            LocalDate recStartDate;
            try {
                recStartDate = LocalDate.parse(rcStartDateStr);
            } catch (Exception e) {
                System.out.println("Invalid date format.");
                return;
            }

            System.out.print("Recurrence end date (yyyy-MM-dd): ");
            String rcEndDateStr = scanner.nextLine().trim();
            LocalDate recEndDate;
            if (rcEndDateStr.isEmpty()) {
                System.out.println("Recurrence end date is required.");
                return;
            }
            try {
                recEndDate = LocalDate.parse(rcEndDateStr);
            } catch (Exception e) {
                System.out.println("Invalid date format.");
                return;
            }

            recurrencePattern = new RecurrencePattern(recurrenceType, interval, recStartDate, recEndDate);
        }

        try {
            controller.updateTask(taskName, dueDate, newTitle, description, status, priority, newDueDate, newProject, newTags, recurrencePattern);
            System.out.println("Task updated successfully.");
        } catch (Exception e) {
            System.out.println("Update failed: " + e.getMessage());
        }
    }

    private void createTask() {
        System.out.println();
        System.out.println("--- Create New Task ---");

        System.out.print("Task title: ");
        String title = scanner.nextLine().trim();
        if (title.isEmpty()) {
            System.out.println("Title is required.");
            return;
        }

        System.out.print("Description (optional): ");
        String description = scanner.nextLine().trim();
        if (description.isEmpty()) {
            description = null;
        }

        System.out.print("Due date (yyyy-MM-dd): ");
        String dueDateStr = scanner.nextLine().trim();
        LocalDate dueDate = null;
        if (dueDateStr.isEmpty()) {
            System.out.println("Due date is required.");
            return;
        }
        try {
            dueDate = LocalDate.parse(dueDateStr);
        } catch (Exception e) {
            System.out.println("Invalid date format.");
            return;
        }

        System.out.print("Priority level (DEFAULT/LOW/MEDIUM/HIGH/CRITICAL, default DEFAULT): ");
        String priorityStr = scanner.nextLine().trim();
        PriorityLevel priority = PriorityLevel.DEFAULT;
        if (!priorityStr.isEmpty()) {
            try {
                priority = PriorityLevel.valueOf(priorityStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid priority. Using DEFAULT.");
            }
        }

        System.out.print("Project name (optional, default 'Default'): ");
        String projectName = scanner.nextLine().trim();
        if (projectName.isEmpty()) {
            projectName = "Default";
        }

        System.out.print("Add recurrence pattern? (y/n): ");
        String addRecurrence = scanner.nextLine().trim().toLowerCase();
        RecurrencePattern recurrencePattern = null;
        
        if (addRecurrence.equals("y") || addRecurrence.equals("yes")) {
            System.out.println("--- Recurrence Pattern ---");
            
            System.out.print("Recurrence type (DAILY/WEEKLY/MONTHLY): ");
            String typeStr = scanner.nextLine().trim();
            RecurrenceType recurrenceType = null;
            try {
                recurrenceType = RecurrenceType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid recurrence type.");
                return;
            }
            
            System.out.print("Interval (every N days/weeks/months): ");
            String intervalStr = scanner.nextLine().trim();
            int interval = 1;
            try {
                interval = Integer.parseInt(intervalStr);
                if (interval < 1) {
                    System.out.println("Interval must be at least 1.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid interval. Using 1.");
            }
            
            System.out.print("Recurrence start date (yyyy-MM-dd, default = task due date): ");
            String rcStartDateStr = scanner.nextLine().trim();
            LocalDate recStartDate = dueDate;
            if (!rcStartDateStr.isEmpty()) {
                try {
                    recStartDate = LocalDate.parse(rcStartDateStr);
                } catch (Exception e) {
                    System.out.println("Invalid date format. Using task due date.");
                }
            }
            
            System.out.print("Recurrence end date (yyyy-MM-dd): ");
            String rcEndDateStr = scanner.nextLine().trim();
            LocalDate recEndDate = null;
            if (rcEndDateStr.isEmpty()) {
                System.out.println("Recurrence end date is required.");
                return;
            }
            try {
                recEndDate = LocalDate.parse(rcEndDateStr);
            } catch (Exception e) {
                System.out.println("Invalid date format.");
                return;
            }
            
            recurrencePattern = new RecurrencePattern(recurrenceType, interval, recStartDate, recEndDate);
        }

        try {
            if (recurrencePattern != null) {
                controller.createTask(title, dueDate, description, projectName, priority, recurrencePattern);
            } else {
                controller.createTask(title, dueDate, description, projectName, priority);
            }
            System.out.println("Task created successfully.");
        } catch (Exception e) {
            System.out.println("Failed to create task: " + e.getMessage());
        }
    }

    private void printResults(List<Task> tasks) {
        System.out.println();
        System.out.println("--- Results (" + tasks.size() + " task(s)) ---");

        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }

        String fmt = "%-20s %-12s %-11s %-10s %s%n";
        System.out.printf(fmt, "TaskName", "DueDate", "Status", "Priority", "Project");
        System.out.println("----------------------------------------------------------------------");

        for (Task t : tasks) {
            String recurringLabel = "";
            if (t.isOccurrence()) {
                recurringLabel = " [occurrence of '" + t.getParentTask().getTitle() + "' " + t.getParentTask().getDueDate() + "]";
            } else if (t.isRecurring()) {
                recurringLabel = " [recurring - " + t.getOccurrences().size() + " occurrence(s)]";
            }
            System.out.printf(fmt,
                    t.getTitle(),
                    t.getDueDate(),
                    t.getStatus(),
                    t.getPriorityLevel(),
                    t.getProject().getName());
            if (!recurringLabel.isEmpty()) {
                System.out.println("  " + recurringLabel.trim());
            }

            if (!t.getTags().isEmpty()) {
                System.out.printf("  Tags: %s%n", t.getTags());
            }
            for (SubTask st : t.getSubTasks()) {
                String collab = st.hasCollaborator() ? "[" + st.getAssignedCollaborator().getName() + "]" : "";
                System.out.printf("  \u2514\u2500 %-18s %-11s %s%n",
                        st.getTitle(), st.getStatus(), collab);
            }
        }
        System.out.println("----------------------------------------------------------------------");
    }
}
