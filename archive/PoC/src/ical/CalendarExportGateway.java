package ical;

import task.Task;
import java.io.IOException;
import java.util.List;

public interface CalendarExportGateway {
    void exportTasks(List<Task> tasks, String filePath) throws IOException;
}
