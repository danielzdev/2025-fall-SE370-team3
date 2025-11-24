package csusm.cougarplanner.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class DateTimeUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Parses YYYY-MM-DD strings into LocalDate. Returns null if input is empty or parsing fails.
     */
    public static LocalDate parseDate(String dateStr)
    {
        if (dateStr == null || dateStr.trim().isEmpty())
        {
            return null;
        }
        try
        {
            return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
        }
        catch (DateTimeParseException e)
        {
            return null;
        }
    }

    /**
     * Parses HH:MM strings into LocalTime. Returns null if input is empty or parsing fails.
     */
    public static LocalTime parseTime(String timeStr)
    {
        if (timeStr == null || timeStr.trim().isEmpty())
        {
            return null;
        }
        try
        {
            return LocalTime.parse(timeStr.trim(), TIME_FORMATTER);
        }
        catch (DateTimeParseException e)
        {
            return null;
        }
    }

    /**
     * Parses YYYY-MM-DD HH:MM strings into LocalDateTime. Returns null if input is empty or parsing fails.
     */
    public static LocalDateTime parseDateTime(String dateTimeStr)
    {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty())
        {
            return null;
        }
        try
        {
            return LocalDateTime.parse(dateTimeStr.trim(), DATE_TIME_FORMATTER);
        }
        catch (DateTimeParseException e)
        {
            return null;
        }
    }

    /**
     * Compares two due_date strings with "empty goes last" logic.
     * Returns: -1 if dateA < dateB, 0 if equal, 1 if dateA > dateB
     * Empty dates are considered "greater" than valid dates.
     */
    public static int compareDueDate(String dateStrA, String dateStrB)
    {
        LocalDate dateA = parseDate(dateStrA);
        LocalDate dateB = parseDate(dateStrB);

        // Both dates are valid - compares normally
        if (dateA != null && dateB != null)
        {
            return dateA.compareTo(dateB);
        }

        // Empty dates go last logic
        if (dateA == null && dateB == null)
        {
            return 0; // Both empty - consider equal
        } else if (dateA == null)
        {
            return 1; // dateA is empty, so it goes after dateB
        }
        else
        {
            return -1; // dateB is empty, so dateA goes before dateB
        }
    }

    /**
     * Compares two due_time strings with "empty goes last" logic.
     * Returns: -1 if timeA < timeB, 0 if equal, 1 if timeA > timeB
     * Empty times are considered "greater" than valid times.
     */
    public static int compareDueTime(String timeStrA, String timeStrB)
    {
        LocalTime timeA = parseTime(timeStrA);
        LocalTime timeB = parseTime(timeStrB);

        // Both times are valid - compares normally
        if (timeA != null && timeB != null)
        {
            return timeA.compareTo(timeB);
        }

        // Empty times go last logic
        if (timeA == null && timeB == null)
        {
            return 0; // Both empty - consider equal
        }
        else if (timeA == null)
        {
            return 1; // timeA is empty, so it goes after timeB
        }
        else
        {
            return -1; // timeB is empty, so timeA goes before timeB
        }
    }

    /**
     * Helper method to check if a date string represents a valid date
     */
    public static boolean isValidDate(String dateStr) {
        return parseDate(dateStr) != null;
    }

    /**
     * Helper method to check if a time string represents a valid time
     */
    public static boolean isValidTime(String timeStr)
    {
        return parseTime(timeStr) != null;
    }

    /**
     * Helper method to format LocalDate to YYYY-MM-DD string
     */
    public static String formatDate(LocalDate date)
    {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }

    /**
     * Helper method to format LocalTime to HH:MM string
     */
    public static String formatTime(LocalTime time)
    {
        return time != null ? time.format(TIME_FORMATTER) : "";
    }

    /**
     * Helper method to format LocalDateTime to YYYY-MM-DD HH:MM string
     */
    public static String formatDateTime(LocalDateTime dateTime)
    {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : "";
    }

    /**
     * Parses an ISO instant or zoned datetime string and converts to LocalDate in system default zone.
     * Returns empty Optional if input is null, blank, or cannot be parsed.
     */
    public static Optional<LocalDate> parseDateFromDateTime(String iso) 
    {
        if (iso == null || iso.trim().isEmpty()) 
        {
            return Optional.empty();
        }

        try 
        {
            String trimmed = iso.trim();

            if (trimmed.endsWith("Z") || trimmed.matches(".*[+-]\\d{2}:?\\d{2}$")) {
                ZonedDateTime zdt;
                if (trimmed.endsWith("Z")) {
                    Instant instant = Instant.parse(trimmed);
                    zdt = instant.atZone(ZoneId.systemDefault());
                } else {
                    zdt = ZonedDateTime.parse(trimmed);
                }
                return Optional.of(zdt.toLocalDate());
            }

            // Try parsing as LocalDateTime (e.g., "2023-12-25T10:30:00")
            LocalDateTime ldt = LocalDateTime.parse(trimmed);
            return Optional.of(ldt.toLocalDate());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Parses an ISO instant or zoned datetime string and converts to LocalTime in system default zone.
     * Truncates seconds to return only hours and minutes.
     * Returns empty Optional if input is null, blank, or cannot be parsed.
     */
    public static Optional<LocalTime> parseTimeFromDateTime(String iso) {
        if (iso == null || iso.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            String trimmed = iso.trim();

            // Try parsing as instant first (e.g., "2023-12-25T10:30:00Z" or "2023-12-25T10:30:00.123Z")
            if (trimmed.endsWith("Z") || trimmed.matches(".*[+-]\\d{2}:?\\d{2}$")) {
                ZonedDateTime zdt;
                if (trimmed.endsWith("Z")) {
                    Instant instant = Instant.parse(trimmed);
                    zdt = instant.atZone(ZoneId.systemDefault());
                } else {
                    zdt = ZonedDateTime.parse(trimmed);
                }
                LocalTime time = zdt.toLocalTime();
                return Optional.of(time.withSecond(0).withNano(0)); // Truncate to minutes
            }

            // Try parsing as LocalDateTime (e.g., "2023-12-25T10:30:00")
            LocalDateTime ldt = LocalDateTime.parse(trimmed);
            LocalTime time = ldt.toLocalTime();
            return Optional.of(time.withSecond(0).withNano(0)); // Truncate to minutes
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Formats LocalDate as YYYY-MM-DD string.
     */
    public static String formatYMD(LocalDate d) {
        return d != null ? d.format(DATE_FORMATTER) : "";
    }

    /**
     * Formats LocalTime as HH:MM string.
     */
    public static String formatHM(LocalTime t) 
    {
        return t != null ? t.format(TIME_FORMATTER) : "";
    }

    /**
     * Compares two combined datetime strings with "empty goes last" logic.
     * Expected format: "YYYY-MM-DD HH:MM"
     * Returns: -1 if dateTimeA < dateTimeB, 0 if equal, 1 if dateTimeA > dateTimeB
     * Empty datetime strings are considered "greater" than valid datetimes.
     */
    public static int compareDateTime(String dateTimeStrA, String dateTimeStrB) {
        LocalDateTime dateTimeA = parseDateTime(dateTimeStrA);
        LocalDateTime dateTimeB = parseDateTime(dateTimeStrB);

        // Both datetimes are valid - compare normally
        if (dateTimeA != null && dateTimeB != null) {
            return dateTimeA.compareTo(dateTimeB);
        }

        // Empty datetimes go last logic
        if (dateTimeA == null && dateTimeB == null) {
            return 0; // Both empty - consider equal
        } else if (dateTimeA == null) {
            return 1; // dateTimeA is empty, so it goes after dateTimeB
        } else {
            return -1; // dateTimeB is empty, so dateTimeA goes before dateTimeB
        }
    }
}
