package csusm.cougarplanner.FilterPattern;

import csusm.cougarplanner.models.Task;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Keeps only the tasks that belong to a specific course.
 * The course is identified by its ID rather than its display name so the
 * filter still works after a course gets renamed.
 */
public class CourseFilter implements TaskFilter {
    private String courseId; // Now stores course ID directly

    public CourseFilter(String courseId) {
        this.courseId = courseId;
    }

    /**
     * If no course ID was provided we treat that as "no course filter active"
     * and return the list as-is.
     */
    @Override
    public List<Task> filter(List<Task> tasks) {
        if (courseId == null || courseId.isEmpty()) {
            return tasks;
        }

        return tasks.stream()
                .filter(task -> courseId.equals(task.getCourseId()))
                .collect(Collectors.toList());
    }
}
