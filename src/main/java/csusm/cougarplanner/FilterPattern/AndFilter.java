package csusm.cougarplanner.FilterPattern;

import csusm.cougarplanner.models.Task;
import java.util.ArrayList;
import java.util.List;

public class AndFilter implements TaskFilter {
    private List<TaskFilter> filters = new ArrayList<>();

    public void addFilter(TaskFilter filter) {
        filters.add(filter);
    }

    public void removeFilter(TaskFilter filter) {
        filters.remove(filter);
    }

    public void clearFilters() {
        filters.clear();
    }
    public List<TaskFilter> getFilters() {
        return filters;
    }

    @Override
    public List<Task> filter(List<Task> tasks) {
        List<Task> result = tasks;
        for (TaskFilter filter : filters) {
            result = filter.filter(result);
        }
        return result;
    }
}
