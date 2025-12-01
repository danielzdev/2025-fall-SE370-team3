package csusm.cougarplanner.io;

import csusm.cougarplanner.models.Announcement;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import csusm.cougarplanner.util.DateTimeUtil;
import csusm.cougarplanner.util.WeekUtil;

/**
 * Repository class for managing Announcement data persistence in announcements.csv.
 *
 * Part of T03: Implement CSV layer for announcements.csv with upsert by ID functionality.
 */
public class AnnouncementsRepository
{
    // CSV column headers matching the announcements.csv file specification
    private static final String[] HEADERS = {"announcement_id", "course_id", "title", "posted_at", "body"};
    // Formatter for parsing and formatting the combined datetime in posted_at field
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CsvReader csvReader;
    private final CsvWriter csvWriter;

    /**
     * Constructs a new AnnouncementsRepository with default CSV reader/writer.
     */
    public AnnouncementsRepository()
    {
        this.csvReader = new CsvReader();
        this.csvWriter = new CsvWriter();
    }

    /**
     * Retrieves all announcements from the announcements.csv file.
     *
     * @return List of all Announcement objects in the database
     * @throws IOException if the CSV file cannot be read
     */
    public List<Announcement> findAll() throws IOException
    {
        List<Map<String, String>> records = csvReader.readAll(CsvPaths.getAnnouncementsPath());
        return records.stream()
                .map(this::mapToAnnouncement)
                .collect(Collectors.toList());
    }


    /**
     * Finds all announcements posted within the specific weeks.
     * Uses the posted_at datetime field to determine posting time.
     *
     * @param weekStart the start date of the week (inclusive)
     * @param weekEnd the end date of the week (inclusive)
     * @return List of announcements posted within the specific week
     * @throws IOException if the CSV file cannot be read
     */
    public List<Announcement> findByWeek(LocalDate weekStart, LocalDate weekEnd) throws IOException
    {
        return findAll().stream()
                .filter(announcement -> isInWeek(announcement, weekStart, weekEnd))
                .collect(Collectors.toList());
    }

    /**
     * Finds all announcements posted on the specific day.
     * Uses the posted_at datetime field and compares only the date.
     *
     * @param day the specific day to filter announcements by
     * @return List of announcements posted on the specific day
     * @throws IOException if the CSV file cannot be read
     */
    public List<Announcement> findByDay(LocalDate day) throws IOException
    {
        return findAll().stream()
                .filter(announcement -> isOnDay(announcement, day))
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing announcement or inserts a new one.
     *
     * @param announcement the Announcement object to update or insert
     * @throws IOException if the CSV file cannot be written
     */
    public void upsert(Announcement announcement) throws IOException
    {
        List<Announcement> allAnnouncements = findAll();

        // Removes existing announcement with same ID
        allAnnouncements.removeIf(a -> a.getAnnouncementId().equals(announcement.getAnnouncementId()));

        // Adds updated announcement
        allAnnouncements.add(announcement);

        // Writes back to file
        List<Map<String, String>> records = allAnnouncements.stream()
                .map(this::announcementToMap)
                .collect(Collectors.toList());

        csvWriter.writeAll(CsvPaths.getAnnouncementsPath(), records, HEADERS);
    }

    /**
     * Upsert operation for multiple announcements.
     *
     * @param announcements List of announcements to upsert
     * @throws IOException if the CSV file cannot be written
     */
    public void upsertAll(List<Announcement> announcements) throws IOException
    {
        Map<String, Announcement> announcementMap = new HashMap<>();

        // Loads existing announcements
        for (Announcement existing : findAll())
        {
            announcementMap.put(existing.getAnnouncementId(), existing);
        }

        // Updates with new announcements
        for (Announcement announcement : announcements)
        {
            announcementMap.put(announcement.getAnnouncementId(), announcement);
        }

        // Writes back
        List<Map<String, String>> records = announcementMap.values().stream()
                .map(this::announcementToMap)
                .collect(Collectors.toList());

        csvWriter.writeAll(CsvPaths.getAnnouncementsPath(), records, HEADERS);
    }

    /**
     * Checks if an announcement was posted within specific week range.
     *
     * @param announcement the Announcement to check
     * @param weekStart the start date of the week
     * @param weekEnd the end date of the week
     * @return true if the announcement was posted within the week range, false otherwise
     */
    private boolean isInWeek(Announcement announcement, LocalDate weekStart, LocalDate weekEnd)
    {
        LocalDateTime postedAt = DateTimeUtil.parseDateTime(announcement.getPostedAt());
        if (postedAt == null) return false;
        LocalDate postedDate = postedAt.toLocalDate();
        return WeekUtil.isDateInWeek(postedDate, weekStart, weekEnd);
    }

    /**
     * Checks if an announcement was posted on the specific day.
     *
     * @param announcement the Announcement to check
     * @param day the specific day to check against
     * @return true if the announcement was posted on the specific day, false otherwise
     */
    private boolean isOnDay(Announcement announcement, LocalDate day)
    {
        LocalDateTime postedAt = DateTimeUtil.parseDateTime(announcement.getPostedAt());
        return postedAt != null && postedAt.toLocalDate().equals(day);
    }

    /**
     * Converts a CSV record Map to an Announcement object.
     *
     * @param record Map representing a CSV row with snake_case keys
     * @return Announcement object populated from the CSV data
     */
    private Announcement mapToAnnouncement(Map<String, String> record)
    {
        Announcement announcement = new Announcement();
        announcement.setAnnouncementId(record.get("announcement_id"));
        announcement.setCourseId(record.get("course_id"));
        announcement.setTitle(record.get("title"));
        announcement.setPostedAt(record.get("posted_at"));
        announcement.setMessage(record.get("body"));
        return announcement;
    }

    /**
     * Converts an Announcement object to a CSV record Map.
     *
     * @param announcement the Announcement object to convert
     * @return Map representing a CSV row with snake_case keys
     */
    private Map<String, String> announcementToMap(Announcement announcement)
    {
        Map<String, String> record = new HashMap<>();
        record.put("announcement_id", announcement.getAnnouncementId());
        record.put("course_id", announcement.getCourseId());
        record.put("title", announcement.getTitle());
        record.put("posted_at", announcement.getPostedAt());
        record.put("body", announcement.getMessage() != null ? announcement.getMessage() : "");
        return record;
    }
}
