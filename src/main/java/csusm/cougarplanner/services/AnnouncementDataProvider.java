package csusm.cougarplanner.services;

import csusm.cougarplanner.io.AnnouncementsRepository;
import csusm.cougarplanner.models.Announcement;
import csusm.cougarplanner.models.AnnouncementDisplay;
import csusm.cougarplanner.util.DateTimeUtil;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concrete data provider for announcement information.
 * Handles retrieval, filtering, and enrichment of announcement data for UI display.
 * Integrates with AnnouncementsRepository for data access and provides course name lookup.
 */
public class AnnouncementDataProvider extends AbstractItemDataProvider<Announcement, AnnouncementDisplay> {

    /**
     * Repository for accessing announcement data from announcements.csv
     * Provides methods for finding announcements by week and day ranges
     */
    private final AnnouncementsRepository announcementsRepository;

    /**
     * Constructs a new AnnouncementDataProvider with required dependencies.
     * Initializes the announcements repository for data access.
     */
    public AnnouncementDataProvider() {
        super();
        this.announcementsRepository = new AnnouncementsRepository();
    }

    /**
     * Retrieves announcements for a specific time period and enriches them with course names.
     * This is the primary method used by week view in the main application.
     *
     * @param periodStart The start date of the week (inclusive)
     * @param periodEnd The end date of the week (inclusive)
     * @return List of announcement display objects ready for UI presentation
     * @throws IOException If announcement or course data cannot be accessed
     */
    @Override
    public List<AnnouncementDisplay> getDataForPeriod(LocalDate periodStart, LocalDate periodEnd) throws IOException {
        List<Announcement> announcements = announcementsRepository.findByWeek(periodStart, periodEnd);
        return enrichItemsWithCourseNames(announcements);
    }

    /**
     * Retrieves announcements for a specific day and enriches them with course names.
     * This method supports the day view functionality in the main application.
     *
     * @param day The specific day to retrieve announcements for
     * @return List of announcement display objects for the specified day
     * @throws IOException If announcement or course data cannot be accessed
     */
    public List<AnnouncementDisplay> getDataForDay(LocalDate day) throws IOException {
        List<Announcement> announcements = announcementsRepository.findByDay(day);
        return enrichItemsWithCourseNames(announcements);
    }

    /**
     * Enriches announcement objects with course names by looking up course information.
     * Transforms base Announcement objects into AnnouncementDisplay objects with full course context.
     *
     * @param announcements List of base announcement objects from repository
     * @return List of enriched announcement display objects
     * @throws IOException If course data cannot be accessed for lookup
     */
    @Override
    protected List<AnnouncementDisplay> enrichItemsWithCourseNames(List<Announcement> announcements) throws IOException {
        List<AnnouncementDisplay> enrichedAnnouncements = new ArrayList<>();

        for (Announcement announcement : announcements) {
            // Look up course name using the course_id from the announcement
            String courseName = getCourseName(announcement.getCourseId());

            // Create display object with enriched course information
            AnnouncementDisplay displayItem = new AnnouncementDisplay(announcement, courseName);
            enrichedAnnouncements.add(displayItem);
        }

        // Sort the enriched announcements before returning
        sortAnnouncements(enrichedAnnouncements);
        return enrichedAnnouncements;
    }

    /**
     * Provides a user-friendly message for empty announcement states.
     * This message is displayed in the UI when no announcements are available.
     *
     * @return Context-aware empty state message for announcements
     */
    @Override
    public String getEmptyMessage()
    {
        return "No announcements for this class.";
    }

    /**
     * Sorts the announcement display list by posted_at datetime then course name.
     *
     * @param announcements List of AnnouncementDisplay objects to sort in-place
     */
    private void sortAnnouncements(List<AnnouncementDisplay> announcements) {
        Collections.sort(announcements, (a, b) -> {
            // 1. Compare posted_at datetime (empty datetimes go last)
            int dateTimeCompare = DateTimeUtil.compareDateTime(a.getPostedAt(), b.getPostedAt());
            if (dateTimeCompare != 0) return dateTimeCompare;

            // 2. Compare course names A→Z
            String courseA = a.getCourseName() != null ? a.getCourseName() : "";
            String courseB = b.getCourseName() != null ? b.getCourseName() : "";
            return courseA.compareToIgnoreCase(courseB);
        });
    }
}
