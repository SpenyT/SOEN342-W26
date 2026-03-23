import java.util.List;
import java.util.Scanner;
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
                case "1":
                    importCsv();
                    break;
                case "2":
                    exportCsv();
                    break;
                case "3":
                    search();
                    break;
                case "4":
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please enter 1-4.");
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
        System.out.println("|| 3. Search Tasks                        ||");
        System.out.println("|| 4. Exit                                ||");
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
        System.out.println("(No criteria = all tasks, sorted by due date)");
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

        List<Task> results = controller.searchTasks(name, status, startDate, endDate, dayOfWeek);
        printResults(results);
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
            System.out.printf(fmt,
                    t.getTitle(),
                    t.getDueDate(),
                    t.getStatus(),
                    t.getPriorityLevel(),
                    t.getProject().getName());

            for (SubTask st : t.getSubTasks()) {
                String collab = st.hasCollaborator() ? "[" + st.getAssignedCollaborator().getName() + "]" : "";
                System.out.printf("  \u2514\u2500 %-18s %-11s %s%n",
                        st.getTitle(), st.getStatus(), collab);
            }
        }
        System.out.println("----------------------------------------------------------------------");
    }
}
