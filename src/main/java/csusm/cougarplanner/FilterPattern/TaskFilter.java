package csusm.cougarplanner.FilterPattern;

import csusm.cougarplanner.models.Task;
import java.util.List;

public interface TaskFilter {
    List<Task> filter(List<Task> tasks);
}
