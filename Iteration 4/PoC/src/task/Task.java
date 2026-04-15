package task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import project.Project;
import tag.Tag;

public class Task extends WorkItem {
    private final LocalDate creationDate;
    private LocalDate dueDate;
    private String description;
    private boolean isRecurring;
    private PriorityLevel priorityLevel;
    private RecurrencePattern recurrencePattern;
    private Task parentTask;
    private final List<Task> occurrences;
    private final List<SubTask> subTasks;
    private final List<Tag> tags;
    private Project project;

    public Task(String title, LocalDate dueDate, String description, Project project, String id) {
        super(title, id);
        this.dueDate = dueDate;
        this.description = description;
        this.project  = project;
        this.creationDate = LocalDate.now();
        this.isRecurring = false;
        this.priorityLevel = PriorityLevel.DEFAULT;
        this.recurrencePattern = null;
        this.parentTask  = null;
        this.occurrences = new ArrayList<>();
        this.subTasks = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    public Task(String title, LocalDate dueDate, String description, Project project) {
        super(title);
        this.dueDate = dueDate;
        this.description = description;
        this.project  = project;
        this.creationDate = LocalDate.now();
        this.isRecurring = false;
        this.priorityLevel = PriorityLevel.DEFAULT;
        this.recurrencePattern = null;
        this.parentTask  = null;
        this.occurrences = new ArrayList<>();
        this.subTasks = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    public String getUniqueName() { return getTitle() + "_" + dueDate.toString(); }

    public void addSubTask(SubTask subTask) {
        if (subTasks.size() >= 20) {
            throw new IllegalStateException("A task cannot have more than 20 sub-tasks.");
        }
        subTask.setParentTask(this);
        subTasks.add(subTask);
    }
    public void removeSubTask(SubTask subTask) { subTasks.remove(subTask); }
    public List<SubTask> getSubTasks() { return subTasks; }

    public void addOccurrence(Task occurrence) {
        occurrences.add(occurrence);
        occurrence.parentTask = this;
    }

    public boolean isOccurrence() { return parentTask != null; }

    public void setRecurrencePattern(RecurrencePattern pattern) {
        this.recurrencePattern = pattern;
        this.isRecurring = true;
    }

    public void addTag(Tag tag) {
        if (!tags.contains(tag)) tags.add(tag);
    }

    public boolean hasTag(String tagName) {
        String lower = tagName.toLowerCase().trim();
        return tags.stream().anyMatch(t -> t.getName().equals(lower));
    }

    public LocalDate getCreationDate() { return creationDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isRecurring(){ return isRecurring; }
    public PriorityLevel getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(PriorityLevel priorityLevel) { this.priorityLevel = priorityLevel; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public List<Task> getOccurrences() { return occurrences; }
    public Task getParentTask() { return parentTask; }
    public RecurrencePattern getRecurrencePattern() { return recurrencePattern; }
    public List<Tag> getTags() { return tags; }
}
