package csusm.cougarplanner.models;

/**
 * Represents a course with basic identification information.
 * Maps directly to the courses.csv file structure.
 * Serves as reference data for assignments and announcements.
 */
public class Course
{
    private String courseId;
    private String courseName;

    /**
     * Default constructor for CSV reading and object creation.
     * Required in repository layer.
     */
    public Course() {}

    /**
     * Constructs a complete Course with identifier and name.
     *
     * @param courseId unique identifier for the course
     * @param courseName the display name of the course
     */
    public Course(String courseId, String courseName)
    {
        this.courseId = courseId;
        this.courseName = courseName;
    }

    /**
     * Gets the unique course identifier.
     * Used as foreign key in assignments and announcements.
     *
     * @return the courseId as a string
     */
    public String getCourseId()
    {
        return courseId;
    }

    /**
     * Sets the unique course identifier.
     *
     * @param courseId the courseId to set
     */
    public void setCourseId(String courseId)
    {
        this.courseId = courseId;
    }

    /**
     * Gets the course display name.
     * Used for UI presentation and joining with assignment/announcement data.
     *
     * @return the courseName as a string
     */
    public String getCourseName()
    {
        return courseName;
    }

    /**
     * Sets the course display name.
     *
     * @param courseName the courseName to set
     */
    public void setCourseName(String courseName)
    {
        this.courseName = courseName;
    }

    /**
     * Returns a string representation of the course for debugging.
     *
     * @return string containing course identifier and name
     */
    @Override
    public String toString()
    {
        return "Course{" +
                "courseId='" + courseId + '\'' +
                ", courseName='" + courseName + '\'' +
                '}';
    }
}
