package csusm.cougarplanner.FilterPattern;

import csusm.cougarplanner.models.Task;
import java.util.List;
import java.util.stream.Collectors;

public class CourseFilter implements TaskFilter {
    private String courseId; // Now stores course ID directly

    public CourseFilter(String courseId) {
        this.courseId = courseId;
    }

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