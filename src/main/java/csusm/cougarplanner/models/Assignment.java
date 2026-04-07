package csusm.cougarplanner.models;

/**
 * Represents a course assignment with due dates, times, and user-set difficulty.
 */
public class Assignment {

    private String assignmentId;
    private String courseId;
    private String assignmentName;
    private String createdAt;
    private String dueDate;
    private String dueTime;
    private Integer difficulty;

    /**
     * Default constructor for CSV reading and object creation.
     * Required in repository layer.
     */
    public Assignment() {}

    /**
     * Constructs a complete Assignment with all fields.
     *
     * @param assignmentId unique identifier for the assignment
     * @param courseId identifier of the associated course
     * @param assignmentName the assignment name/title
     * @param createdAt creation timestamp in ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ)
     * @param dueDate due date in YYYY-MM-DD format
     * @param dueTime due time in HH:MM format
     * @param difficulty user-set difficulty level 1-5
     */
    public Assignment(
        String assignmentId,
        String courseId,
        String assignmentName,
        String dueDate,
        String dueTime,
        Integer difficulty,
        String createdAt
    ) {
        this.assignmentId = assignmentId;
        this.courseId = courseId;
        this.assignmentName = assignmentName;
        this.createdAt = createdAt;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.difficulty = difficulty;
    }

    /**
     * Gets the unique assignment identifier.
     *
     * @return the assignmentId as a string
     */
    public String getAssignmentId() {
        return assignmentId;
    }

    /**
     * Sets the unique assignment identifier.
     *
     * @param assignmentId the assignmentId to set
     */
    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }

    /**
     * Gets the associated course identifier.
     *
     * @return the courseId as a string
     */
    public String getCourseId() {
        return courseId;
    }

    /**
     * Sets the associated course identifier.
     *
     * @param courseId the courseId to set
     */
    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    /**
     * Gets the assignment name/title.
     *
     * @return the assignmentName as a string
     */
    public String getAssignmentName() {
        return assignmentName;
    }

    /**
     * Sets the assignment name/title.
     *
     * @param assignmentName the assignmentName to set
     */
    public void setAssignmentName(String assignmentName) {
        this.assignmentName = assignmentName;
    }

    /**
     * Gets the due date in YYYY-MM-DD format.
     *
     * @return dueDate as string, may be empty
     */
    public String getDueDate() {
        return dueDate;
    }

    /**
     * Sets the due date in YYYY-MM-DD format.
     *
     * @param dueDate the dueDate string in YYYY-MM-DD format
     */
    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * Gets the due time in HH:MM format.
     *
     * @return dueTime as string, may be null or empty
     */
    public String getDueTime() {
        return dueTime;
    }

    /**
     * Sets the due time in HH:MM format.
     *
     * @param dueTime the dueTime string in HH:MM format
     */
    public void setDueTime(String dueTime) {
        this.dueTime = dueTime;
    }

    /**
     * Gets the user-set difficulty level.
     * Critical for merge logic - local difficulty is preserved during API sync.
     *
     * @return difficulty as Integer (1-5), may be null
     */
    public Integer getDifficulty() {
        return difficulty;
    }

    /**
     * Sets the user-set difficulty level.
     * Used by inline difficulty editor in the UI.
     *
     * @param difficulty the difficulty level 1-5, or null if not set
     */
    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * Gets the assignment creation timestamp.
     *
     * @return createdAt as string in ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ), may be null or empty
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the assignment creation timestamp.
     *
     * @param createdAt the creation timestamp in ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ)
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns a string representation of the assignment for debugging.
     * Shows all fields including the critical difficulty value.
     *
     * @return string containing all assignment fields
     */
    @Override
    public String toString() {
        return (
            "Assignment{" +
            "assignmentId='" +
            assignmentId +
            '\'' +
            ", courseId='" +
            courseId +
            '\'' +
            ", assignmentName='" +
            assignmentName +
            '\'' +
            ", createdAt='" +
            createdAt +
            '\'' +
            ", dueDate='" +
            dueDate +
            '\'' +
            ", dueTime='" +
            dueTime +
            '\'' +
            ", difficulty=" +
            difficulty +
            '}'
        );
    }
}
