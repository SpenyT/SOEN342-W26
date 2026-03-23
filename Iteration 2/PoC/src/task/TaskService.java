package task;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import project.Project;

public class TaskService {
    private final List<Task> tasks;

    public TaskService() {
        this.tasks = new ArrayList<>();
    }

    public Task createTask(String title, LocalDate dueDate, String description, Project project) {
        List<Task> tempTasks = new ArrayList<>(tasks);
        boolean exists = tempTasks.stream().anyMatch(t -> t.getTitle().equals(title));
        if (exists) {
            throw new IllegalArgumentException("Task name already exists.");
        }
        Task t = new Task(title, dueDate, description, project);
        tasks.add(t);
        return t;
    }

    public Task createRecurringTask(String title, String description, Project project, RecurrencePattern pattern) {
        Task template = new Task(title, pattern.getStartDate(), description, project);
        template.setRecurrencePattern(pattern);
        tasks.add(template);

        for (LocalDate date : pattern.generateOccurrenceDates()) {
            if (findTaskByNameAndDate(title, date) != null) continue;
            Task occurrence = new Task(title, date, description, project);
            occurrence.setPriorityLevel(template.getPriorityLevel());
            template.addOccurrence(occurrence);
            tasks.add(occurrence);
        }
        return template;
    }

    public List<Task> filterSearch(String name, String statusStr, String startDateStr, String endDateStr, String dayOfWeekStr) {
        List<Task> result = new ArrayList<>(tasks);

        if (!isBlank(name)) {
            String lower = name.toLowerCase();
            result = result.stream().filter(t -> t.getTitle().toLowerCase().contains(lower)).collect(Collectors.toList());
        }
        if (!isBlank(statusStr)) {
            try {
                Status s = Status.valueOf(statusStr.toUpperCase());
                result = result.stream().filter(t -> t.getStatus() == s).collect(Collectors.toList());
            } catch (Exception e) {
                // Do Nothing
            }
        }
        if (!isBlank(startDateStr)) {
            try {
                LocalDate start = LocalDate.parse(startDateStr);
                result = result.stream().filter(t -> !t.getDueDate().isBefore(start)).collect(Collectors.toList());
            } catch (Exception e) {
                // Do Nothing
            }
        }
        if (!isBlank(endDateStr)) {
            try {
                LocalDate end = LocalDate.parse(endDateStr);
                result = result.stream().filter(t -> !t.getDueDate().isAfter(end)).collect(Collectors.toList());
            } catch (Exception e) {
                // Do Nothing
            }
        }
        if (!isBlank(dayOfWeekStr)) {
            try {
                DayOfWeek dow = DayOfWeek.valueOf(dayOfWeekStr.toUpperCase());
                result = result.stream().filter(t -> t.getDueDate().getDayOfWeek() == dow).collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                // Do Nothing
            }
        }

        result.sort(Comparator.comparing(Task::getDueDate));
        return result;
    }

    public Task findTaskByNameAndDate(String title, LocalDate dueDate) {
        return tasks.stream().filter(t -> t.getTitle().equals(title) && t.getDueDate().equals(dueDate)).findFirst().orElse(null);
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
