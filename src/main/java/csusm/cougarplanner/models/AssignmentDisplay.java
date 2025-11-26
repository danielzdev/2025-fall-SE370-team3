package csusm.cougarplanner.models;

/**
 * Data Transfer Object (DTO) for displaying assignment information in the UI.
 * Enriches the base Assignment model with course name for direct display.
 * This prevents the UI from needing to perform additional data lookups.
 */
public class AssignmentDisplay {

    private final String assignmentId;
    private final String courseId;
    private final String courseName;
    private final String assignmentName;
    private final String dueDate; // YYYY-MM-DD format
    private final String dueTime; // HH:MM format (24-hour)
    private final Integer difficulty; // 1-5 scale, null if not set
    private final String createdAt; // ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ)

    /**
     * Constructs a new AssignmentDisplay by combining Assignment data with course information.
     *
     * @param assignment The base assignment data from assignments.csv
     * @param courseName The course name looked up from courses.csv
     */
    public AssignmentDisplay(Assignment assignment, String courseName) {
        this.assignmentId = assignment.getAssignmentId();
        this.courseId = assignment.getCourseId();
        this.courseName = courseName;
        this.assignmentName = assignment.getAssignmentName();
        this.dueDate = assignment.getDueDate();
        this.dueTime = assignment.getDueTime();
        this.difficulty = assignment.getDifficulty();
        this.createdAt = assignment.getCreatedAt();
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getAssignmentName() {
        return assignmentName;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getDueTime() {
        return dueTime;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
