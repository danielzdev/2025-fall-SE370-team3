package csusm.cougarplanner.models;

/**
 * Data Transfer Object (DTO) for displaying announcement information in the UI.
 * Enriches the base Announcement model with course name for direct display.
 * Provides all necessary information for rendering announcements without additional lookups.
 */
public class AnnouncementDisplay
{
    private final String announcementId;
    private final String courseId;
    private final String courseName;
    private final String title;
    private final String postedAt;   // YYYY-MM-DD HH:MM format
    private final String body;

    /**
     * Constructs a new AnnouncementDisplay by combining Announcement data with course information.
     *
     * @param announcement The base announcement data from announcements.csv
     * @param courseName The course name looked up from courses.csv
     */
    public AnnouncementDisplay(Announcement announcement, String courseName)
    {
        this.announcementId = announcement.getAnnouncementId();
        this.courseId = announcement.getCourseId();
        this.courseName = courseName;
        this.title = announcement.getTitle();
        this.postedAt = announcement.getPostedAt();
        this.body = announcement.getMessage();
    }

    public String getAnnouncementId() { return announcementId; }
    public String getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public String getTitle() { return title; }
    public String getPostedAt() { return postedAt; }
    public String getBody() { return body; }
}