package csusm.cougarplanner.FilterPattern;

import csusm.cougarplanner.models.Task;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Keeps only the tasks whose priority matches the one this filter was built
 * with (e.g. "High", "Medium", "Low").
 *
 * Mirrors {@link StatusFilter}: the sentinel value "All Priorities" is
 * treated as "no filter active" so the priority dropdown in the UI can be
 * passed straight through.
 */
public class PriorityFilter implements TaskFilter {
    private String priorityLevel;

    public PriorityFilter(String priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    @Override
    public List<Task> filter(List<Task> tasks) {
        // Empty selection or the catch-all label means "show everything"
        if (priorityLevel == null || priorityLevel.isEmpty() || priorityLevel.equals("All Priorities")) {
            return tasks;
        }
        return tasks.stream()
                .filter(task -> priorityLevel.equals(task.getPriority()))
                .collect(Collectors.toList());
    }
}
