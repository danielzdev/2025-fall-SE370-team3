package csusm.cougarplanner.commandPattern;

import csusm.cougarplanner.io.TasksRepository;
import csusm.cougarplanner.models.Task;
import csusm.cougarplanner.services.TaskCache;

public class DeleteTaskCommand implements Command {

    private final TasksRepository tasksRepository;
    private final String taskId;
    private Task snapshot;

    public DeleteTaskCommand(TasksRepository tasksRepository, String taskId) {
        this.tasksRepository = tasksRepository;
        this.taskId = taskId;
    }

    @Override
    public void execute() throws Exception {
        snapshot = TaskCache.getInstance().findById(taskId).orElse(null);
        TaskCache.getInstance().remove(taskId);
        tasksRepository.deleteById(taskId);
    }

    @Override
    public void undo() throws Exception {
        if (snapshot == null) return;
        TaskCache.getInstance().add(snapshot);
        tasksRepository.upsert(snapshot);
    }
}
