package csusm.cougarplanner.models;

/**
 * Represents a course assignment with due dates, times, and user-set difficulty.
 */
public class Assignment {

    private String assignment_id;
    private String course_id;
    private String assignment_name;
    private String created_at;
    private String due_date;
    private String due_time;
    private Integer difficulty;

    /**
     * Default constructor for CSV reading and object creation.
     * Required in repository layer.
     */
    public Assignment() {}

    /**
     * Constructs a complete Assignment with all fields.
     *
     * @param assignment_id unique identifier for the assignment
     * @param course_id identifier of the associated course
     * @param assignment_name the assignment name/title
     * @param created_at creation timestamp in ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ)
     * @param due_date due date in YYYY-MM-DD format
     * @param due_time due time in HH:MM format
     * @param difficulty user-set difficulty level 1-5
     */
    public Assignment(
        String assignment_id,
        String course_id,
        String assignment_name,
        String due_date,
        String due_time,
        Integer difficulty,
        String created_at
    ) {
        this.assignment_id = assignment_id;
        this.course_id = course_id;
        this.assignment_name = assignment_name;
        this.created_at = created_at;
        this.due_date = due_date;
        this.due_time = due_time;
        this.difficulty = difficulty;
    }

    /**
     * Gets the unique assignment identifier.
     *
     * @return the assignment_id as a string
     */
    public String getAssignmentId() {
        return assignment_id;
    }

    /**
     * Sets the unique assignment identifier.
     *
     * @param assignment_id the assignment_id to set
     */
    public void setAssignmentId(String assignment_id) {
        this.assignment_id = assignment_id;
    }

    /**
     * Gets the associated course identifier.
     *
     * @return the course_id as a string
     */
    public String getCourseId() {
        return course_id;
    }

    /**
     * Sets the associated course identifier.
     *
     * @param course_id the course_id to set
     */
    public void setCourseId(String course_id) {
        this.course_id = course_id;
    }

    /**
     * Gets the assignment name/title.
     *
     * @return the assignment_name as a string
     */
    public String getAssignmentName() {
        return assignment_name;
    }

    /**
     * Sets the assignment name/title.
     *
     * @param assignment_name the assignment_name to set
     */
    public void setAssignmentName(String assignment_name) {
        this.assignment_name = assignment_name;
    }

    /**
     * Gets the due date in YYYY-MM-DD format.
     *
     * @return due_date as string, may be empty
     */
    public String getDueDate() {
        return due_date;
    }

    /**
     * Sets the due date in YYYY-MM-DD format.
     *
     * @param due_date the due_date string in YYYY-MM-DD format
     */
    public void setDueDate(String due_date) {
        this.due_date = due_date;
    }

    /**
     * Gets the due time in HH:MM format.
     *
     * @return due_time as string, may be null or empty
     */
    public String getDueTime() {
        return due_time;
    }

    /**
     * Sets the due time in HH:MM format.
     *
     * @param due_time the due_time string in HH:MM format
     */
    public void setDueTime(String due_time) {
        this.due_time = due_time;
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
     * @return created_at as string in ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ), may be null or empty
     */
    public String getCreatedAt() {
        return created_at;
    }

    /**
     * Sets the assignment creation timestamp.
     *
     * @param created_at the creation timestamp in ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ)
     */
    public void setCreatedAt(String created_at) {
        this.created_at = created_at;
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
            "assignment_id='" +
            assignment_id +
            '\'' +
            ", course_id='" +
            course_id +
            '\'' +
            ", assignment_name='" +
            assignment_name +
            '\'' +
            ", created_at='" +
            created_at +
            '\'' +
            ", due_date='" +
            due_date +
            '\'' +
            ", due_time='" +
            due_time +
            '\'' +
            ", difficulty=" +
            difficulty +
            '}'
        );
    }
}
