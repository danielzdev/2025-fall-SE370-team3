package csusm.cougarplanner.FilterPattern;

import csusm.cougarplanner.models.Task;
import java.util.ArrayList;
import java.util.List;

/**
 * Composite filter that runs every registered filter in sequence and only
 * keeps tasks that pass all of them (logical AND).
 *
 * The order filters are added in does not change the final result, but it can
 * affect performance — cheaper filters added first will trim the list down
 * before more expensive ones run.
 */
public class AndFilter implements TaskFilter {
    private List<TaskFilter> filters = new ArrayList<>();

    /**
     * Adds a filter to the chain.
     */
    public void addFilter(TaskFilter filter) {
        filters.add(filter);
    }

    /**
     * Removes a previously added filter. No-op if it isn't in the chain.
     */
    public void removeFilter(TaskFilter filter) {
        filters.remove(filter);
    }

    /**
     * Drops every filter in the chain. After this call the AndFilter behaves
     * as a pass-through.
     */
    public void clearFilters() {
        filters.clear();
    }

    public List<TaskFilter> getFilters() {
        return filters;
    }

    /**
     * Applies each filter to the result of the previous one. With no filters
     * registered the input list is returned unchanged.
     */
    @Override
    public List<Task> filter(List<Task> tasks) {
        List<Task> result = tasks;
        for (TaskFilter filter : filters) {
            result = filter.filter(result);
        }
        return result;
    }
}
