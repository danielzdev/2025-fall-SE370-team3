package csusm.cougarplanner.commandPattern;

import csusm.cougarplanner.io.TasksRepository;
import csusm.cougarplanner.models.Task;
import csusm.cougarplanner.services.TaskCache;

/**
 * Command that deletes a single task by id.
 * <p>
 * Before deleting, it snapshots the task from the cache so {@link #undo()} can
 * restore the exact same object. If the id wasn't found (snapshot is null),
 * undo becomes a no-op rather than throwing.
 */
public class DeleteTaskCommand implements Command {

    private final TasksRepository tasksRepository;
    private final String taskId;
    // Snapshot of the task taken at execute() time, used to restore it on undo.
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
