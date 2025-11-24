package csusm.cougarplanner;

import static org.junit.jupiter.api.Assertions.*;

import csusm.cougarplanner.models.Assignment;
import csusm.cougarplanner.models.Course;
import csusm.cougarplanner.services.CanvasService;
import csusm.cougarplanner.util.WeekRange;
import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// To use these tests, in your terminal run the command: ./mvnw test
public class SimpleCanvasTest {

    private API api;
    private CanvasService canvasService;

    @BeforeEach
    void setUp() {
        String token = getToken();
        assertNotNull(token, "Auth token should not be null - check data/profile.properties file");

        api = new API(token);
        canvasService = new CanvasService(api);
    }

    private String getToken() {
        try (Scanner sc = new Scanner(new File("data/profile.properties"))) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.startsWith("authToken=")) {
                    return line.substring("authToken=".length()).trim();
                }
            }
        } catch (Exception e) {
            fail("Error reading token from data/profile.properties: " + e.getMessage());
        }
        return null;
    }

    @Test
    void testFetchCourses() {
        System.out.println("\nCOURSES TEST");
        System.out.println("================");

        List<Course> courses = canvasService.fetchCourses();

        assertNotNull(courses, "Courses should not be null");
        assertFalse(courses.isEmpty(), "Should have at least one course");

        System.out.println("Found " + courses.size() + " courses:");
        for (Course course : courses) {
            System.out.println("  • " + course.getCourseName() + " (ID: " + course.getCourseId() + ")");

            assertNotNull(course.getCourseId(), "Course ID should not be null");
            assertNotNull(course.getCourseName(), "Course name should not be null");
            assertFalse(course.getCourseId().trim().isEmpty(), "Course ID should not be empty");
            assertFalse(course.getCourseName().trim().isEmpty(), "Course name should not be empty");
        }
    }

    @Test
    void testCurrentWeekAssignments() {
        System.out.println("CURRENT WEEK ASSIGNMENTS");
        System.out.println("===========================");

        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.minusDays(now.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(7);
        WeekRange currentWeek = new WeekRange(weekStart, weekEnd);

        List<Assignment> assignments = canvasService.fetchAssignments(currentWeek);
        assertNotNull(assignments, "Current week assignments should not be null");

        System.out.println("Current week (" + weekStart + " to " + weekEnd.minusDays(1) + "):");

        if (assignments.isEmpty()) {
            System.out.println("  No assignments due this week");
        } else {
            System.out.println("  Found " + assignments.size() + " assignments:");
            for (Assignment assignment : assignments) {
                System.out.println(
                    "    • " +
                        assignment.getAssignmentName() +
                        " - Due: " +
                        assignment.getDueDate() +
                        " at " +
                        assignment.getDueTime()
                );

                assertNotNull(assignment.getAssignmentName(), "Assignment name should not be null");
                assertNotNull(assignment.getCourseId(), "Assignment course ID should not be null");
            }
        }
    }

    @Test
    void testNextWeekAssignments() {
        System.out.println("NEXT WEEK ASSIGNMENTS");
        System.out.println("=========================");

        LocalDate now = LocalDate.now();
        LocalDate nextWeekStart = now.plusDays(7 - now.getDayOfWeek().getValue() + 1);
        LocalDate nextWeekEnd = nextWeekStart.plusDays(7);
        WeekRange nextWeek = new WeekRange(nextWeekStart, nextWeekEnd);

        List<Assignment> assignments = canvasService.fetchAssignments(nextWeek);
        assertNotNull(assignments, "Next week assignments should not be null");

        System.out.println("Next week (" + nextWeekStart + " to " + nextWeekEnd.minusDays(1) + "):");

        if (assignments.isEmpty()) {
            System.out.println("  No assignments due next week");
        } else {
            System.out.println("  Found " + assignments.size() + " assignments:");
            for (Assignment assignment : assignments) {
                System.out.println(
                    "    • " +
                        assignment.getAssignmentName() +
                        " - Due: " +
                        assignment.getDueDate() +
                        " at " +
                        assignment.getDueTime()
                );

                assertNotNull(assignment.getAssignmentName(), "Assignment name should not be null");
                assertNotNull(assignment.getCourseId(), "Assignment course ID should not be null");
            }
        }
    }
}
