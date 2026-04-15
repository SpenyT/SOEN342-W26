# Task Scheduler App — SOEN 342 Iteration IV

A Java console application for managing tasks, subtasks, projects, and collaborators. Supports CSV import/export, task search with multiple filters, history logging, iCalendar export, and collaborator capacity tracking.

---

## Requirements

- Java 21+
- Maven (I used 3.9.14)

Maven handles all the dependencies, they download automatically when you build.

---

## Build & Run

All commands are run from the **root of the project** (the `PoC/` folder).

### Compile

```bash
mvn compile
```

### Run

```bash
mvn exec:java
```

### Compile + Run in one step

```bash
mvn compile exec:java
```

---

## Menu Options

```
||========================================||
||           Task Scheduler App           ||
||========================================||
|| 1. Import Tasks from CSV               ||
|| 2. Export Tasks to CSV                 ||
|| 3. Create Task                         ||
|| 4. Update Task                         ||
|| 5. Search Tasks                        ||
|| 6. View Task History                   ||
|| 7. Export Tasks to iCalendar (.ics)    ||
|| 8. List Overloaded Collaborators       ||
|| 9. Exit                                ||
||========================================||
```

---

### Option 1 — Import Tasks from CSV

Loads tasks, subtasks, projects, collaborators, and tags from a CSV file.

**Steps:**
1. Select `1` at the menu.
2. Enter the path to your CSV file (relative to the `PoC/` folder).
   ```
   CSV file path: data/tasks.csv
   ```
3. The system confirms how many tasks are loaded:
   ```
   Import complete. 15 task(s) in system.
   ```

**CSV format:**

```
TaskName,Description,Subtask,Status,Priority,DueDate,ProjectName,ProjectDescription,Collaborator,CollaboratorCategory,Tags
```

| Column | Required | Values / Format |
|--------|----------|-----------------|
| `TaskName` | Yes | Any string |
| `Description` | No | Any string |
| `Subtask` | No | Subtask name, or leave empty |
| `Status` | No | `OPEN` (default), `COMPLETED`, or `CANCELLED` |
| `Priority` | No | `DEFAULT`, `LOW`, `MEDIUM`, `HIGH`, or `CRITICAL` |
| `DueDate` | Yes | `yyyy-MM-dd` (e.g. `2026-04-15`) |
| `ProjectName` | Yes | Any string — created if it doesn't exist |
| `ProjectDescription` | No | Any string |
| `Collaborator` | No | Collaborator name, or leave empty |
| `CollaboratorCategory` | No | `SENIOR`, `INTERMEDIATE`, or `JUNIOR` |
| `Tags` | No | Comma-separated tags in a quoted field, e.g. `"urgent,backend"` |

**Rules:**
- A task with multiple subtasks is represented as **multiple rows** sharing the same `TaskName` and `DueDate`.
- Tags in the `Tags` column are applied to the task and persisted; duplicates are ignored.
- Fields containing commas must be wrapped in double quotes (standard CSV).

**Example:**
```
TaskName,Description,Subtask,Status,Priority,DueDate,ProjectName,ProjectDescription,Collaborator,CollaboratorCategory,Tags
Fix login bug,Resolve auth issue,Write unit tests,OPEN,HIGH,2026-04-10,Backend,Backend services,Alice,SENIOR,"auth,urgent"
Fix login bug,Resolve auth issue,Code review,OPEN,HIGH,2026-04-10,Backend,Backend services,Bob,JUNIOR,"auth,urgent"
Design homepage,Create mockups,,OPEN,MEDIUM,2026-04-20,Frontend,UI work,,,frontend
```

---

### Option 2 — Export Tasks to CSV

Saves all tasks to a CSV file in the same format as import (including tags).

```
Export file path: data/export.csv
```
---

### Option 3 — Create Tasks

Create task by providing a title. Description, due date, project name and recurrence pattern are optional. Press enter to skip if field not mandatory. 
When a task is created it also contains a creation date, a priority level, and a status.

```
--- Create Task ---
Task title (required)
Description (optional)
Due Date (optional)
Priority Level (default: Default)
Status (default: OPEN)
Project Name (optional)
Recurrence Patter (optional)
```

---

### Option 4 — Update Tasks

To update task enter new data. Press enter to skip.
Changes can be made to title, description, priority, due date, status, associated project, tags or recurrence pattern.

---

### Option 5 — Search Tasks

Filters tasks by any combination of criteria. All fields are optional — press Enter to skip.

```
--- Task Search ---
Task name contains:
Status (OPEN/COMPLETED/CANCELLED):
Period start (yyyy-MM-dd):
Period end   (yyyy-MM-dd):
Day of week  (MONDAY..SUNDAY):
Tag:
```

- Leaving all fields blank returns all **OPEN** tasks sorted by due date.
- Multiple criteria are combined with AND logic.

---

### Option 6 — View Task History

Shows the recorded history for a specific task.

```
Task title: Fix login bug
Due date (yyyy-MM-dd): 2026-04-10
```

---

### Option 7 — Export Tasks to iCalendar (.ics)

Exports tasks to a `.ics` file compatible with Google Calendar, Outlook, and Apple Calendar.

```
a) Single task by title and due date
b) All tasks in a project
c) Filtered tasks (by name, period, day of week, status)
```

Enter an output file path without the `.ics` extension — it is added automatically.

---

### Option 8 — List Overloaded Collaborators

Lists collaborators who are at or over their open-task capacity:

| Category | Max open tasks |
|----------|---------------|
| SENIOR | 2 |
| INTERMEDIATE | 5 |
| JUNIOR | 10 |

---

### Option 9 — Exit

All data is persisted in `data/` and reloaded automatically on the next startup.

---

## Data Files

| File | Contents |
|------|----------|
| `data/tasks.db` | Tasks, subtasks, tags |
| `data/projects.db` | Projects and project tags |
| `data/collaborator.db` | Collaborators and their assignments |
| `data/history.db` | History log entries |

To reset all data, delete the `.db` files in `data/`.
