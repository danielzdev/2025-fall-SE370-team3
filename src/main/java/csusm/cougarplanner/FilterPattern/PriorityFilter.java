package csusm.cougarplanner.FilterPattern;

import csusm.cougarplanner.models.Task;
import java.util.List;
import java.util.stream.Collectors;

public class PriorityFilter implements TaskFilter {
    private String priorityLevel;

    public PriorityFilter(String priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    @Override
    public List<Task> filter(List<Task> tasks) {
        if (priorityLevel == null || priorityLevel.isEmpty() || priorityLevel.equals("All Priorities")) {
            return tasks;
        }
        return tasks.stream()
                .filter(task -> priorityLevel.equals(task.getPriority()))
                .collect(Collectors.toList());
    }
}
