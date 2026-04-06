package ical;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.immutable.ImmutableCalScale;
import net.fortuna.ical4j.model.property.immutable.ImmutableVersion;
import task.SubTask;
import task.Task;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;


/* 
    I could not make this work, I'm sorry.
    It seemed ok when compared to documentation and examples, 
    but I kept getting errors. It compiles though... strangely.
*/
public class ICalCalendarGateway implements CalendarExportGateway {

    @Override
    public void exportTasks(List<Task> tasks, String filePath) throws IOException {
        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//SOEN342 Task Scheduler//EN"));
        calendar.getProperties().add(ImmutableVersion.VERSION_2_0);
        calendar.getProperties().add(ImmutableCalScale.GREGORIAN);

        for (Task task : tasks) {
            if (task.getDueDate() == null) continue;

            VEvent event = new VEvent(task.getDueDate(), task.getTitle());
            event.getProperties().add(new Uid(task.getTitle() + "_" + task.getDueDate()));
            event.getProperties().add(new DtStamp(Instant.now()));

            StringBuilder desc = new StringBuilder();
            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                desc.append(task.getDescription()).append("\n");
            }
            desc.append("Status: ").append(task.getStatus());
            desc.append("\nPriority: ").append(task.getPriorityLevel());
            if (task.getProject() != null) {
                desc.append("\nProject: ").append(task.getProject().getName());
            }
            List<SubTask> subtasks = task.getSubTasks();
            if (!subtasks.isEmpty()) {
                desc.append("\nSubtasks:");
                for (SubTask st : subtasks) {
                    desc.append("\n  - ").append(st.getTitle()).append(" [").append(st.getStatus()).append("]");
                }
            }
            event.getProperties().add(new Description(desc.toString()));
            calendar.getComponents().add(event);
        }

        String path = filePath.endsWith(".ics") ? filePath : filePath + ".ics";
        try (FileOutputStream fos = new FileOutputStream(path)) {
            new CalendarOutputter().output(calendar, fos);
        }
    }
}
