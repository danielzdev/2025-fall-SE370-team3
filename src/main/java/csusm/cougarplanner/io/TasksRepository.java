package csusm.cougarplanner.io;

import csusm.cougarplanner.models.Announcement;
import csusm.cougarplanner.models.Task;
import csusm.cougarplanner.util.DateTimeUtil;
import csusm.cougarplanner.util.WeekUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Repository class for managing Task data persistence in tasks.csv.
 *
 * Part of T03: Implement CSV layer for announcements.csv with upsert by ID functionality.
 */
public class TasksRepository {
    // CSV column headers matching the tasks.csv file specification
    private static final String[] HEADERS = {"taskID", "title", "description", "createdDate", "dueDate", "courseId", "status", "priority", "completed"};
    // Formatter for parsing and formatting the combined datetime in posted_at field
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CsvReader csvReader;
    private final CsvWriter csvWriter;

    /**
     * Constructs a new TasksRepository with default CSV reader/writer.
     */
    public TasksRepository()
    {
        this.csvReader = new CsvReader();
        this.csvWriter = new CsvWriter();
    }

    /**
     * Retrieves all tasks from the tasks.csv file.
     *
     * @return List of all Task objects in the database
     * @throws IOException if the CSV file cannot be read
     */
    public List<Task> findAll() throws IOException
    {
        List<Map<String, String>> records = csvReader.readAll(CsvPaths.getTasksPath());
        return records.stream()
                .map(this::mapToTask)
                .collect(Collectors.toList());
    }


    /**
     * Finds all tasks posted within the specific weeks.
     * Uses the posted_at datetime field to determine posting time.
     *
     * @param weekStart the start date of the week (inclusive)
     * @param weekEnd the end date of the week (inclusive)
     * @return List of announcements posted within the specific week
     * @throws IOException if the CSV file cannot be read
     */
//    public List<Announcement> findByWeek(LocalDate weekStart, LocalDate weekEnd) throws IOException
//    {
//        return findAll().stream()
//                .filter(announcement -> isInWeek(announcement, weekStart, weekEnd))
//                .collect(Collectors.toList());
//    }

    /**
     * Finds all announcements posted on the specific day.
     * Uses the posted_at datetime field and compares only the date.
     *
     * @param day the specific day to filter announcements by
     * @return List of announcements posted on the specific day
     * @throws IOException if the CSV file cannot be read
     */
//    public List<Announcement> findByDay(LocalDate day) throws IOException
//    {
//        return findAll().stream()
//                .filter(announcement -> isOnDay(announcement, day))
//                .collect(Collectors.toList());
//    }

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
//    private boolean isInWeek(Announcement announcement, LocalDate weekStart, LocalDate weekEnd)
//    {
//        LocalDateTime postedAt = DateTimeUtil.parseDateTime(announcement.getPostedAt());
//        if (postedAt == null) return false;
//        LocalDate postedDate = postedAt.toLocalDate();
//        return WeekUtil.isDateInWeek(postedDate, weekStart, weekEnd);
//    }

    /**
     * Checks if an announcement was posted on the specific day.
     *
     * @param announcement the Announcement to check
     * @param day the specific day to check against
     * @return true if the announcement was posted on the specific day, false otherwise
     */
//    private boolean isOnDay(Announcement announcement, LocalDate day)
//    {
//        LocalDateTime postedAt = DateTimeUtil.parseDateTime(announcement.getPostedAt());
//        return postedAt != null && postedAt.toLocalDate().equals(day);
//    }

    /**
     * Converts a CSV record Map to an Announcement object.
     *
     * @param record Map representing a CSV row with snake_case keys
     * @return Announcement object populated from the CSV data
     */
    private Task mapToTask(Map<String, String> record)
    {
        Task task = new Task();
        task.setTaskId(record.get("taskID"));
        task.setTitle(record.get("title"));
        task.setDescription(record.get("description"));
        task.setCreatedDate(record.get("createdDate"));
        task.setDueDate(record.get("dueDate"));
        task.setCourseId(record.get("courseId"));
        task.setStatus(record.get("status"));
        task.setPriority(record.get("priority"));
        task.setCompleted(Boolean.parseBoolean(record.get("completed")));
        return task;
    }

    /**
     * Converts a Task object to a CSV record Map.
     *
     * @param task the Task object to convert
     * @return Map representing a CSV row with snake_case keys
     */
    private Map<String, String> taskToMap(Task task)
    {
        Map<String, String> record = new HashMap<>();
        record.put("taskID", task.getTaskId());
        record.put("title", task.getTitle());
        record.put("description", task.getDescription());
        record.put("createdDate", task.getCreatedDate());
        record.put("dueDate", task.getDueDate());
        record.put("courseId", task.getCourseId());
        record.put("status", task.getStatus());
        record.put("priority", task.getPriority());
        record.put("completed", String.valueOf(task.isCompleted()));
        return record;
    }
}
