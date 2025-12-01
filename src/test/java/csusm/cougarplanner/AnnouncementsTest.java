package csusm.cougarplanner;

import csusm.cougarplanner.models.Announcement;
import csusm.cougarplanner.services.CanvasService;
import csusm.cougarplanner.util.WeekRange;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

public class AnnouncementsTest {

    @Test
    public void testAnnouncementsHistory() {
        API api = new API(); // Loads token automatically
        CanvasService service = new CanvasService(api);

        LocalDate today = LocalDate.now();
        // Calculate the Monday of the current week
        LocalDate currentWeekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);

        System.out.println("\n=== ANNOUNCEMENT HISTORY (Past 2 Weeks + Current) ===");

        // Loop 3 times:
        // i=0 (Current Week)
        // i=1 (Last Week)
        // i=2 (2 Weeks Ago)
        for (int i = 0; i <= 2; i++) {
            LocalDate start = currentWeekStart.minusWeeks(i);
            WeekRange range = new WeekRange(start, start.plusDays(7));

            String label = (i == 0) ? "CURRENT WEEK" : (i + " WEEK(S) AGO");

            checkRange(service, range, label);
        }

        System.out.println("=====================================================\n");
    }

    private void checkRange(CanvasService service, WeekRange range, String label) {
        System.out.println(
            "\n--- " + label + " (" + range.startIncl() + " to " + range.endExcl().minusDays(1) + ") ---"
        );

        // This method inside CanvasService iterates all courses for us now
        List<Announcement> list = service.fetchAnnouncements(range);

        if (list.isEmpty()) {
            System.out.println("  (No announcements found)");
        } else {
            for (Announcement a : list) {
                // Formatting: [Course ID] Date : Title
                System.out.println("  [Course " + a.getCourseId() + "] " + a.getPostedAt() + " : " + a.getTitle());
            }
        }
    }
}
