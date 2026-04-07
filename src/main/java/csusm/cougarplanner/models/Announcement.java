package csusm.cougarplanner.models;

/**
 * Represents a course announcement with combined datetime posting information.
 */
public class Announcement
{
    private String announcementId;
    private String courseId;
    private String title;
    private String postedAt;
    private String message;

    /**
     * Default constructor for CSV reading and object creation.
     * Required in repository layer.
     */
    public Announcement() {}

    /**
     * Constructs a complete Announcement with all fields.
     *
     * @param announcementId unique identifier for the announcement
     * @param courseId identifier of the associated course
     * @param title the announcement title
     * @param postedAt combined date and time in YYYY-MM-DD HH:MM format
     * @param message optional announcement content
     */
    public Announcement(String announcementId, String courseId, String title,
                        String postedAt, String message)
    {
        this.announcementId = announcementId;
        this.courseId = courseId;
        this.title = title;
        this.postedAt = postedAt;
        this.message = message;
    }

    /**
     * Gets the unique announcement identifier.
     *
     * @return the announcementId as a string
     */
    public String getAnnouncementId()
    {
        return announcementId;
    }

    /**
     * Sets the unique announcement identifier.
     *
     * @param announcementId the announcementId to set
     */
    public void setAnnouncementId(String announcementId)
    {
        this.announcementId = announcementId;
    }

    /**
     * Gets the associated course identifier.
     *
     * @return the courseId as a string
     */
    public String getCourseId()
    {
        return courseId;
    }

    /**
     * Sets the associated course identifier.
     *
     * @param courseId the courseId to set
     */
    public void setCourseId(String courseId)
    {
        this.courseId = courseId;
    }

    /**
     * Gets the announcement title.
     *
     * @return the title as a string
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Sets the announcement title.
     *
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * Gets the posting date and time in combined format.
     *
     * @return postedAt as YYYY-MM-DD HH:MM string
     */
    public String getPostedAt()
    {
        return postedAt;
    }

    /**
     * Sets the posting date and time in combined format.
     *
     * @param postedAt the postedAt string in YYYY-MM-DD HH:MM format
     */
    public void setPostedAt(String postedAt)
    {
        this.postedAt = postedAt;
    }

    /**
     * Gets the announcement message content.
     *
     * @return the message content, which may be null or empty
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * Sets the announcement message content.
     *
     * @param message the message content to set
     */
    public void setMessage(String message)
    {
        this.message = message;
    }

    /**
     * Returns a string representation of the announcement for debugging.
     *
     * @return string containing all announcement fields
     */
    @Override
    public String toString()
    {
        return "Announcement{" +
                "announcementId='" + announcementId + '\'' +
                ", courseId='" + courseId + '\'' +
                ", title='" + title + '\'' +
                ", postedAt='" + postedAt + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}