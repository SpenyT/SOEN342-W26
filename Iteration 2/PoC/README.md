# Task Scheduler App — SOEN 342 Iteration II

A Java console application for managing tasks, subtasks, projects, and collaborators. Supports CSV import/export, task search with multiple filters, recurring tasks, and collaborator capacity tracking.

---

## Requirements

- Java JDK 8 or later (`javac` and `java` on your PATH)
- No build tools, frameworks, or external libraries required

---

## Build & Run

All commands are run from the **root of the project** (the `Iteration 2/` folder, one level above `src/`).

### 1. Compile

```bash
javac -d out src/*.java src/task/*.java src/project/*.java
```

This compiles all source files and places the `.class` files into an `out/` directory.

> If `out/` does not exist yet, create it first:
> ```bash
> mkdir out
> ```

### 2. Run

```bash
java -cp out Main
```

The menu will appear immediately in your terminal.

---

## Menu Options

```
||========================================||
||           Task Scheduler App           ||
||========================================||
|| 1. Import Tasks from CSV               ||
|| 2. Export Tasks to CSV                 ||
|| 3. Search Tasks                        ||
|| 4. Exit                                ||
||========================================||
  Choice:
```

The system starts with empty state (non-persistent) on every launch. Load data first via option 1.

---

### Option 1 — Import Tasks from CSV

Loads tasks, subtasks, projects, and collaborators from a CSV file.

**Steps:**
1. Select `1` at the menu.
2. Enter the path to your CSV file (absolute or relative to where you ran `java`).
   ```
   CSV file path: data/tasks.csv
   ```
3. The system prints how many tasks are now loaded:
   ```
   Import complete. 5 task(s) in system.
   ```

**CSV format** — the file must have this header row, followed by one data row per task/subtask:

```
TaskName,Description,Subtask,Status,Priority,DueDate,ProjectName,ProjectDescription,Collaborator,CollaboratorCategory
```

| Column | Required | Values / Format |
|--------|----------|-----------------|
| `TaskName` | Yes | Any string |
| `Description` | Yes | Any string |
| `Subtask` | No | Subtask name, or leave empty |
| `Status` | Yes | `OPEN`, `COMPLETED`, or `CANCELLED` |
| `Priority` | Yes | `DEFAULT`, `LOW`, `MEDIUM`, `HIGH`, or `CRITICAL` |
| `DueDate` | Yes | `yyyy-MM-dd` (e.g. `2026-04-15`) |
| `ProjectName` | Yes | Any string — created if it doesn't exist |
| `ProjectDescription` | Yes | Any string |
| `Collaborator` | No | Collaborator name, or leave empty |
| `CollaboratorCategory` | No | `SENIOR`, `INTERMEDIATE`, or `JUNIOR` |

**Rules:**
- A task with multiple subtasks is represented as **multiple rows** sharing the same `TaskName` and `DueDate`.
- If `Subtask` is non-empty and `Collaborator` is also provided, the collaborator is assigned to that subtask.
- If `Subtask` is non-empty but `Collaborator` is empty, a plain subtask (no assignment) is created.
- If both `Subtask` and `Collaborator` are empty, the row represents a task with no subtask.
- Fields containing commas or double quotes must be wrapped in double quotes; inner double quotes are escaped by doubling them (standard CSV escaping).

**Example CSV:**
```
TaskName,Description,Subtask,Status,Priority,DueDate,ProjectName,ProjectDescription,Collaborator,CollaboratorCategory
Fix login bug,Resolve auth issue,Write unit tests,OPEN,HIGH,2026-04-10,Backend,Backend services,Alice,SENIOR
Fix login bug,Resolve auth issue,Code review,OPEN,HIGH,2026-04-10,Backend,Backend services,Bob,JUNIOR
Design homepage,Create mockups,,OPEN,MEDIUM,2026-04-20,Frontend,UI work,,
```

---

### Option 2 — Export Tasks to CSV

Saves all tasks currently in memory to a CSV file.

**Steps:**
1. Select `2` at the menu.
2. Enter the desired output file path.
   ```
   Export file path: data/export.csv
   ```
3. The system confirms:
   ```
   Exported 5 task(s) to data/export.csv
   ```

The exported file uses the **same format** as the import (see Option 1). Tasks with multiple subtasks produce multiple rows. The file can be re-imported in a future session.

---

### Option 3 — Search Tasks

Filters the loaded tasks using one or more criteria. All criteria are optional — press Enter to skip any.

**Steps:**
1. Select `3` at the menu.
2. Answer each prompt (or press Enter to skip):

   ```
   --- Task Search ---
   Leave blank to skip a criterion.
   (No criteria = all open tasks, sorted by due date)

   Task name contains:
   Status (OPEN/COMPLETED/CANCELLED):
   Period start (yyyy-MM-dd):
   Period end   (yyyy-MM-dd):
   Day of week  (MONDAY..SUNDAY):
   ```

3. Results are displayed in a table, sorted by due date ascending:

   ```
   --- Results (2 task(s)) ---
   TaskName             DueDate      Status      Priority   Project
   ----------------------------------------------------------------------
   Fix login bug        2026-04-10   OPEN        HIGH       Backend
     └─ Write unit tests  OPEN        [Alice]
     └─ Code review       OPEN        [Bob]
   Design homepage      2026-04-20   OPEN        MEDIUM     Frontend
   ----------------------------------------------------------------------
   ```

**Filter behaviour:**
- **Task name contains** — case-sensitive substring match on the task title.
- **Status** — must be exactly `OPEN`, `COMPLETED`, or `CANCELLED`. Leave blank to select any.
- **Period start / Period end** — filters tasks whose due date falls within the range (inclusive). Either bound can be omitted.
- **Day of week** — filters tasks whose due date falls on that weekday (e.g. `MONDAY`).
- Multiple criteria are combined with AND logic (all specified criteria must match).
- **No criteria** — returns all `OPEN` tasks, sorted by due date.

---

### Option 4 — Exit

Exits the application. All in-memory data is lost; export first if you want to keep it.

```
  Choice: 4
Goodbye!
```
