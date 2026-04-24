package csusm.cougarplanner.commandPattern;

import csusm.cougarplanner.io.TasksRepository;
import csusm.cougarplanner.models.Task;
import csusm.cougarplanner.services.TaskCache;

import java.util.List;

/**
 * Command that clears every task (the "Delete All" action).
 * <p>
 * Snapshots the full task list at execute() time so undo can restore the
 * entire set via {@link TasksRepository#upsertAll(List)} in one pass, rather
 * than relying on the user to undo each deletion individually.
 */
public class DeleteAllTasksCommand implements Command {

    private final TasksRepository tasksRepository;
    // Snapshot of every task at the time of deletion, used to restore the full list on undo.
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
