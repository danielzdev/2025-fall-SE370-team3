package csusm.cougarplanner.commandPattern;

import csusm.cougarplanner.io.TasksRepository;
import csusm.cougarplanner.models.Task;
import csusm.cougarplanner.services.TaskCache;

/**
 * Command that creates a new task. Adds the task to both the in-memory
 * {@link TaskCache} (so the UI updates immediately) and the persistent
 * CSV-backed {@link TasksRepository}. Undo removes it from both.
 */
public class CreateTaskCommand implements Command {

    private final TasksRepository tasksRepository;
    private final Task task;

    public CreateTaskCommand(TasksRepository tasksRepository, Task task) {
        this.tasksRepository = tasksRepository;
        this.task = task;
    }

    @Override
    public void execute() throws Exception {
        TaskCache.getInstance().add(task);
        tasksRepository.upsert(task);
    }

    @Override
    public void undo() throws Exception {
        TaskCache.getInstance().remove(task.getTaskId());
        tasksRepository.deleteById(task.getTaskId());
    }
}
