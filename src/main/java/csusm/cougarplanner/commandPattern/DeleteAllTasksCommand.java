package csusm.cougarplanner.commandPattern;

import csusm.cougarplanner.io.TasksRepository;
import csusm.cougarplanner.models.Task;
import csusm.cougarplanner.services.TaskCache;

import java.util.List;

public class DeleteAllTasksCommand implements Command {

    private final TasksRepository tasksRepository;
    private List<Task> snapshot;

    public DeleteAllTasksCommand(TasksRepository tasksRepository) {
        this.tasksRepository = tasksRepository;
    }

    @Override
    public void execute() throws Exception {
        snapshot = TaskCache.getInstance().getAll();
        TaskCache.getInstance().removeAll();
        tasksRepository.deleteAll();
    }

    @Override
    public void undo() throws Exception {
        if (snapshot == null) return;
        for (Task t : snapshot) {
            TaskCache.getInstance().add(t);
        }
        tasksRepository.upsertAll(snapshot);
    }
}
