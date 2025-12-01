package csusm.cougarplanner.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class WeekUtil
{
    /**
     * Calculates the start of the week for a given reference date and week start setting.
     * @param referenceDate The date to calculate the week for (e.g., today or last viewed date)
     * @param weekStartSetting The week start setting from Profile ("monday" or "sunday")
     * @return The start date of the week
     */
    public static LocalDate getWeekStart(LocalDate referenceDate, String weekStartSetting)
    {
        DayOfWeek targetDayOfWeek;

        if ("sunday".equalsIgnoreCase(weekStartSetting))
        {
            targetDayOfWeek = DayOfWeek.SUNDAY;
        }
        else
        {
            // Default to Monday if invalid setting
            targetDayOfWeek = DayOfWeek.MONDAY;
        }

        // Finds the most recent target day of week that is on or before the reference date
        DayOfWeek currentDayOfWeek = referenceDate.getDayOfWeek();
        int daysToSubtract = currentDayOfWeek.getValue() - targetDayOfWeek.getValue();

        // Adjusts for Sunday (Java's DayOfWeek considers Monday=1, Sunday=7)
        if (daysToSubtract < 0)
        {
            daysToSubtract += 7;
        }

        return referenceDate.minusDays(daysToSubtract);
    }

    /**
     * Calculates the end of the week given the week start date.
     * The week covers exactly 7 days (start date + 6 days).
     * @param weekStart The start date of the week
     * @return The end date of the week
     */
    public static LocalDate getWeekEnd(LocalDate weekStart)
    {
        return weekStart.plusDays(6);
    }

    /**
     * Gets the current week bounds based on system time and week start setting.
     * @param weekStartSetting The week start setting from Profile ("monday" or "sunday")
     * @return Array where [0] is week start and [1] is week end
     */
    public static LocalDate[] getCurrentWeekBounds(String weekStartSetting)
    {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = getWeekStart(today, weekStartSetting);
        LocalDate weekEnd = getWeekEnd(weekStart);
        return new LocalDate[]{weekStart, weekEnd};
    }

    /**
     * Gets the week bounds for navigation (previous/next week).
     * @param currentWeekStart The current week start date
     * @param weekStartSetting The week start setting
     * @param direction "previous" or "next"
     * @return Array where [0] is week start and [1] is week end
     */
    public static LocalDate[] getNavigationWeekBounds(LocalDate currentWeekStart, String weekStartSetting, String direction)
    {
        LocalDate newWeekStart;

        if ("previous".equalsIgnoreCase(direction))
        {
            newWeekStart = currentWeekStart.minusWeeks(1);
        }
        else if ("next".equalsIgnoreCase(direction))
        {
            newWeekStart = currentWeekStart.plusWeeks(1);
        }
        else
        {
            // Default to current week if invalid direction
            newWeekStart = currentWeekStart;
        }

        // Recalculates week start to ensure it aligns with the week start setting
        newWeekStart = getWeekStart(newWeekStart, weekStartSetting);
        LocalDate newWeekEnd = getWeekEnd(newWeekStart);

        return new LocalDate[]{newWeekStart, newWeekEnd};
    }

    /**
     * Checks if a given date falls within a week range.
     * @param date The date to check
     * @param weekStart The week start date
     * @param weekEnd The week end date
     * @return true if the date is within the week range (inclusive)
     */
    public static boolean isDateInWeek(LocalDate date, LocalDate weekStart, LocalDate weekEnd)
    {
        return (date != null && !date.isBefore(weekStart) && !date.isAfter(weekEnd));
    }

    /**
     * Checks if a given date falls within a week range.
     *      This is a specialization of the previous function: only the week start needs to be provided here.
     * @param date The date to check
     * @param weekStart The week start date
     * @param weekStartSetting The week start setting. Weather or not the week starts on sunday or monday
     * @return true if the date is within the week range (inclusive)
     */
    public static boolean isDateInWeek(LocalDate date, LocalDate weekStart, String weekStartSetting) {
        weekStart = getWeekStart(weekStart, weekStartSetting); //double check that week start is actually the start of a week
        LocalDate weekEnd = getWeekEnd(weekStart); //get the last day of the week provided

        return isDateInWeek(date, weekStart, weekEnd);
    }

    /**
     * Gets today's date according to system timezone.
     * @return Current local date
     */
    public static LocalDate getToday()
    {
        return LocalDate.now();
    }
}