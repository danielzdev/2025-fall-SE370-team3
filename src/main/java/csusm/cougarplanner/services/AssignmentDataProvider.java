package csusm.cougarplanner.services;

import csusm.cougarplanner.config.Profile;
import csusm.cougarplanner.config.ProfileReader;
import csusm.cougarplanner.io.AssignmentsRepository;
import csusm.cougarplanner.models.Assignment;
import csusm.cougarplanner.models.AssignmentDisplay;
import csusm.cougarplanner.util.DateTimeUtil;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides assignment data for the user interface.
 * Fetches, filters, and adds course names to assignment details.
 * Accesses data through the AssignmentsRepository.
 */
public class AssignmentDataProvider extends AbstractItemDataProvider<Assignment, AssignmentDisplay>
{

    /**
     * Repository for accessing assignment data from assignments.csv.
     * Provides methods for finding assignments by week and day ranges.
     */
    private final AssignmentsRepository assignmentsRepository;

    /**
     * Constructs a new AssignmentDataProvider with required dependencies.
     * Initializes the assignments repository for data access.
     */
    public AssignmentDataProvider()
    {
        super();
        this.assignmentsRepository = new AssignmentsRepository();
    }

    /**
     * Retrieves assignments for a specific time period and enriches them with course names.
     * This is the primary method used by week view in the main application.
     *
     * @param periodStart The start date of the week (inclusive)
     * @param periodEnd The end date of the week (inclusive)
     * @return List of assignment display objects ready for UI presentation
     * @throws IOException If assignment or course data cannot be accessed
     */
    @Override
    public List<AssignmentDisplay> getDataForPeriod(LocalDate periodStart, LocalDate periodEnd) throws IOException
    {
        // Uses existing repository method for week-based queries
        List<Assignment> assignments = assignmentsRepository.findByWeek(periodStart, periodEnd);

        // Enriches assignments with course names and convert to display objects
        return enrichItemsWithCourseNames(assignments);
    }

    /**
     * Retrieves assignments for a specific day and enriches them with course names.
     * This method supports the day view functionality in the main application.
     *
     * @param day The specific day to retrieve assignments for
     * @return List of assignment display objects for the specified day
     * @throws IOException If assignment or course data cannot be accessed
     */
    public List<AssignmentDisplay> getDataForDay(LocalDate day) throws IOException
    {
        List<Assignment> assignments = assignmentsRepository.findByDay(day);
        return enrichItemsWithCourseNames(assignments);
    }

    /**
     * Enriches assignment objects with course names by looking up course information.
     * Transforms base Assignment objects into AssignmentDisplay objects with full course context.
     *
     * @param assignments List of base assignment objects from repository
     * @return List of enriched assignment display objects
     * @throws IOException If course data cannot be accessed for lookup
     */
    @Override
    protected List<AssignmentDisplay> enrichItemsWithCourseNames(List<Assignment> assignments) throws IOException
    {
        List<AssignmentDisplay> enrichedAssignments = new ArrayList<>();

        for (Assignment assignment : assignments)
        {
            // Looks up course name using the course_id from the assignment
            String courseName = getCourseName(assignment.getCourseId());

            // Creates display object with enriched course information
            AssignmentDisplay displayItem = new AssignmentDisplay(assignment, courseName);
            enrichedAssignments.add(displayItem);
        }

        // Sort the enriched assignments before returning
        sortAssignments(enrichedAssignments);
        return enrichedAssignments;
    }

    /**
     * Provides a user-friendly message for empty assignment states.
     *
     * @return Empty state message for assignments
     */
    @Override
    public String getEmptyMessage()
    {
        return "No assignments due for this class.";
    }

    /**
     * Sorts the assignment display list based on user preferences from Profile.
     * Supports two modes: date_time and difficulty sorting.
     *
     * @param assignments List of AssignmentDisplay objects to sort in-place
     */
    private void sortAssignments(List<AssignmentDisplay> assignments) {
        // Create ProfileReader instance and get profile from ReadResult
        ProfileReader reader = new ProfileReader(java.nio.file.Paths.get(".profile"));
        ProfileReader.ReadResult result = reader.readProfile();
        Profile profile = result.getProfile();
        String sortMode = profile.getDefaultSortMode();

        if ("difficulty".equals(sortMode)) {
            boolean ascending = "ascending".equals(profile.getDefaultSortDifficultyOrder());
            sortByDifficulty(assignments, ascending);
        } else {
            // Default to date_time sorting
            sortByDateTime(assignments);
        }
    }

    /**
     * Sorts assignments by date/time mode: due_date → due_time → course_name → assignment_name
     */
    private void sortByDateTime(List<AssignmentDisplay> assignments) {
        Collections.sort(assignments, (a, b) -> {
            // 1. Compare due dates (empty dates go last)
            int dateCompare = DateTimeUtil.compareDueDate(a.getDueDate(), b.getDueDate());
            if (dateCompare != 0) return dateCompare;

            // 2. Compare due times within same date (empty times go last)
            int timeCompare = DateTimeUtil.compareDueTime(a.getDueTime(), b.getDueTime());
            if (timeCompare != 0) return timeCompare;

            // 3. Compare course names A→Z
            String courseA = a.getCourseName() != null ? a.getCourseName() : "";
            String courseB = b.getCourseName() != null ? b.getCourseName() : "";
            int courseCompare = courseA.compareToIgnoreCase(courseB);
            if (courseCompare != 0) return courseCompare;

            // 4. Compare assignment names A→Z
            String assignmentA = a.getAssignmentName() != null ? a.getAssignmentName() : "";
            String assignmentB = b.getAssignmentName() != null ? b.getAssignmentName() : "";
            return assignmentA.compareToIgnoreCase(assignmentB);
        });
    }

    /**
     * Sorts assignments by difficulty mode: difficulty → due_date → due_time → course_name → assignment_name
     */
    private void sortByDifficulty(List<AssignmentDisplay> assignments, boolean ascending) {
        Collections.sort(assignments, (a, b) -> {
            // 1. Compare difficulty (empty difficulties go last)
            Integer diffA = a.getDifficulty();
            Integer diffB = b.getDifficulty();

            if (diffA != null && diffB != null) {
                int diffCompare = ascending ? diffA.compareTo(diffB) : diffB.compareTo(diffA);
                if (diffCompare != 0) return diffCompare;
            } else if (diffA == null && diffB == null) {
                // Both null, continue to next criteria
            } else if (diffA == null) {
                return 1; // null difficulty goes last
            } else {
                return -1; // diffB is null, so it goes last
            }

            // 2. Compare due dates (empty dates go last)
            int dateCompare = DateTimeUtil.compareDueDate(a.getDueDate(), b.getDueDate());
            if (dateCompare != 0) return dateCompare;

            // 3. Compare due times within same date (empty times go last)
            int timeCompare = DateTimeUtil.compareDueTime(a.getDueTime(), b.getDueTime());
            if (timeCompare != 0) return timeCompare;

            // 4. Compare course names A→Z
            String courseA = a.getCourseName() != null ? a.getCourseName() : "";
            String courseB = b.getCourseName() != null ? b.getCourseName() : "";
            int courseCompare = courseA.compareToIgnoreCase(courseB);
            if (courseCompare != 0) return courseCompare;

            // 5. Compare assignment names A→Z
            String assignmentA = a.getAssignmentName() != null ? a.getAssignmentName() : "";
            String assignmentB = b.getAssignmentName() != null ? b.getAssignmentName() : "";
            return assignmentA.compareToIgnoreCase(assignmentB);
        });
    }
}
