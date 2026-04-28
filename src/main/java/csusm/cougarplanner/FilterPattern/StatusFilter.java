package csusm.cougarplanner.FilterPattern;

import csusm.cougarplanner.models.Task;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Keeps only the tasks whose status matches the one this filter was built with
 * (e.g. "Not Started", "In Progress", "Complete").
 *
 * The sentinel "All Statuses" is treated the same as no status being set, so
 * the dropdown in the UI can hand its raw selection straight to this filter
 * without special-casing the "show everything" choice.
 */
public class StatusFilter implements TaskFilter {
    private String status;

    public StatusFilter(String status) {
        this.status = status;
    }

    @Override
    public List<Task> filter(List<Task> tasks) {
        // No status, empty status, or "All Statuses" all mean "don't filter"
        if (status == null || status.isEmpty() || status.equals("All Statuses")) {
            return tasks;
        }
        return tasks.stream()
                .filter(task -> status.equals(task.getStatus()))
                .collect(Collectors.toList());
    }
}
