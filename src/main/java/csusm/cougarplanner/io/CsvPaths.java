package csusm.cougarplanner.io;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Centralizes all file system paths for CSV data storage and JSON snapshots.
 *
 * Part of T03: Implement CSV layer with centralized file location management.
 */
public class CsvPaths
{
    // Root directory for all application data files
    private static final String DATA_DIR = "data";

    /**
     * Gets the file system path for the courses.csv file.
     * Contains course reference data (course_id, course_name).
     *
     * @return Path to courses.csv in the data directory
     */
    public static Path getCoursesPath()
    {
        return Paths.get(DATA_DIR, "courses.csv");
    }

    /**
     * Gets the file system path for the assignments.csv file.
     * Contains assignment data with due dates, times, and user-set difficulty levels.
     *
     * @return Path to assignments.csv in the data directory
     */
    public static Path getAssignmentsPath()
    {
        return Paths.get(DATA_DIR, "assignments.csv");
    }

    /**
     * Gets the file system path for the announcements.csv file.
     *
     * @return Path to announcements.csv in the data directory
     */
    public static Path getAnnouncementsPath()
    {
        return Paths.get(DATA_DIR, "announcements.csv");
    }

    /**
     * Gets the file system path for the announcements.csv file.
     *
     * @return Path to announcements.csv in the data directory
     */
    public static Path getTasksPath()
    {
        return Paths.get(DATA_DIR, "tasks.csv");
    }

    /**
     * Gets the file system path for the snapshots.json file.
     *
     * @return Path to snapshots.json in the data directory
     */
    public static Path getSnapshotsPath()
    {
        return Paths.get(DATA_DIR, "snapshots.json");
    }

    /**
     * Ensures the data directory exists or creates it if necessary.
     */
    public static void ensureDataDirectory()
    {
        Path dataDir = Paths.get(DATA_DIR);
        if (!dataDir.toFile().exists())
        {
            dataDir.toFile().mkdirs();
        }
    }
}