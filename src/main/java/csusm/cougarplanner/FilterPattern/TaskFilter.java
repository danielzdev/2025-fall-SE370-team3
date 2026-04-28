package csusm.cougarplanner.FilterPattern;

import csusm.cougarplanner.models.Task;
import java.util.List;

/**
 * Common interface for any object that knows how to narrow down a list of tasks.
 * Implementations are meant to be small and single-purpose (one filter per concern)
 * so that they can be combined together through {@link AndFilter}.
 */
public interface TaskFilter {

    /**
     * Returns the subset of the given tasks that match this filter's criteria.
     * Implementations should not mutate the input list.
     *
     * @param tasks the tasks to filter
     * @return a new list containing only the tasks that pass
     */
    List<Task> filter(List<Task> tasks);
}
