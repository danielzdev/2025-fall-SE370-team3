package csusm.cougarplanner.FilterPattern;

import csusm.cougarplanner.models.Task;
import java.util.List;
import java.util.stream.Collectors;

public class StatusFilter implements TaskFilter {
    private String status;

    public StatusFilter(String status) {
        this.status = status;
    }

    @Override
    public List<Task> filter(List<Task> tasks) {
        if (status == null || status.isEmpty() || status.equals("All Statuses")) {
            return tasks;
        }
        return tasks.stream()
                .filter(task -> status.equals(task.getStatus()))
                .collect(Collectors.toList());
    }
}