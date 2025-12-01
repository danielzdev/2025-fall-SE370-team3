package csusm.cougarplanner.models;

/**
 * Represents a course announcement with combined datetime posting information.
 */
public class Announcement
{
    private String announcement_id;
    private String course_id;
    private String title;
    private String posted_at;
    private String message;

    /**
     * Default constructor for CSV reading and object creation.
     * Required in repository layer.
     */
    public Announcement() {}

    /**
     * Constructs a complete Announcement with all fields.
     *
     * @param announcement_id unique identifier for the announcement
     * @param course_id identifier of the associated course
     * @param title the announcement title
     * @param posted_at combined date and time in YYYY-MM-DD HH:MM format
     * @param message optional announcement content
     */
    public Announcement(String announcement_id, String course_id, String title,
                        String posted_at, String message)
    {
        this.announcement_id = announcement_id;
        this.course_id = course_id;
        this.title = title;
        this.posted_at = posted_at;
        this.message = message;
    }

    /**
     * Gets the unique announcement identifier.
     *
     * @return the announcement_id as a string
     */
    public String getAnnouncementId()
    {
        return announcement_id;
    }

    /**
     * Sets the unique announcement identifier.
     *
     * @param announcement_id the announcement_id to set
     */
    public void setAnnouncementId(String announcement_id)
    {
        this.announcement_id = announcement_id;
    }

    /**
     * Gets the associated course identifier.
     *
     * @return the course_id as a string
     */
    public String getCourseId()
    {
        return course_id;
    }

    /**
     * Sets the associated course identifier.
     *
     * @param course_id the course_id to set
     */
    public void setCourseId(String course_id)
    {
        this.course_id = course_id;
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
     * @return posted_at as YYYY-MM-DD HH:MM string
     */
    public String getPostedAt()
    {
        return posted_at;
    }

    /**
     * Sets the posting date and time in combined format.
     *
     * @param posted_at the posted_at string in YYYY-MM-DD HH:MM format
     */
    public void setPostedAt(String posted_at)
    {
        this.posted_at = posted_at;
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
                "announcement_id='" + announcement_id + '\'' +
                ", course_id='" + course_id + '\'' +
                ", title='" + title + '\'' +
                ", posted_at='" + posted_at + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}