package csusm.cougarplanner.models;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a user-created task in the Cougar Planner Tasks tab.
 * <p>
 * Tasks are persisted as CSV rows via {@link csusm.cougarplanner.io.TasksRepository};
 * the no-arg constructor exists specifically for that deserialization path.
 * The string constants below are the canonical values written to disk — use
 * them rather than raw literals so filters and CSVs stay in sync.
 */
public class Task
{
    // Canonical status values written to the CSV. Keep these in sync with the StatusFilter dropdown options.
    public static final String STATUS_NOT_STARTED = "not-started";
    public static final String STATUS_IN_PROGRESS = "in-progress";
    public static final String STATUS_COMPLETED   = "completed";

    // Canonical priority values written to the CSV. Keep these in sync with the PriorityFilter dropdown options.
    public static final String PRIORITY_HIGH   = "high";
    public static final String PRIORITY_MEDIUM = "medium";
    public static final String PRIORITY_LOW    = "low";

    private String  taskId;
    private String  title;
    private String  description;
    private String  createdDate;
    private String  dueDate;      // may be null
    private String  courseId;     // may be null
    private String  status;
    private String  priority;
    private boolean completed;

    // Required for CSV deserialization.
    public Task() {}

    // Creates a new task with a generated ID and today's creation date.
    public Task(String title, String description,
                String dueDate, String courseId,
                String status, String priority)
    {
        this.taskId       = UUID.randomUUID().toString();
        this.title        = title;
        this.description  = description;
        this.createdDate  = LocalDate.now().toString();
        this.dueDate      = dueDate;
        this.courseId     = courseId;
        this.status       = (status   != null && !status.isBlank())   ? status   : STATUS_NOT_STARTED;
        this.priority     = (priority != null && !priority.isBlank()) ? priority : PRIORITY_MEDIUM;
        this.completed    = false;
    }

    // Full constructor used when loading from CSV (Kenny's TaskRepository).
    public Task(String taskId, String title, String description,
                String createdDate, String dueDate, String courseId,
                String status, String priority, boolean completed)
    {
        this.taskId      = taskId;
        this.title       = title;
        this.description = description;
        this.createdDate = createdDate;
        this.dueDate     = dueDate;
        this.courseId    = courseId;
        this.status      = (status   != null && !status.isBlank())   ? status   : STATUS_NOT_STARTED;
        this.priority    = (priority != null && !priority.isBlank()) ? priority : PRIORITY_MEDIUM;
        this.completed   = completed;
    }

    public String  getTaskId()                        { return taskId; }
    public void    setTaskId(String taskId)           { this.taskId = taskId; }

    public String  getTitle()                         { return title; }
    public void    setTitle(String title)             { this.title = title; }

    public String  getDescription()                   { return description; }
    public void    setDescription(String description) { this.description = description; }

    public String  getCreatedDate()                   { return createdDate; }
    public void    setCreatedDate(String createdDate) { this.createdDate = createdDate; }

    public String  getDueDate()                       { return dueDate; }
    public void    setDueDate(String dueDate)         { this.dueDate = dueDate; }

    public String  getCourseId()                      { return courseId; }
    public void    setCourseId(String courseId)       { this.courseId = courseId; }

    public String  getStatus()                        { return status; }
    public void    setStatus(String status)           { this.status = status; }

    public String  getPriority()                      { return priority; }
    public void    setPriority(String priority)       { this.priority = priority; }

    public boolean isCompleted()                      { return completed; }
    public void    setCompleted(boolean completed)    { this.completed = completed; }
}
