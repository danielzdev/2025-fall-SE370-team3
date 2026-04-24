package csusm.cougarplanner.commandPattern;

import csusm.cougarplanner.io.TasksRepository;
import csusm.cougarplanner.services.TaskCache;

/**
 * Command that toggles a task's "completed" flag (checking or unchecking
 * the checkbox in the task row). Because toggling is its own inverse,
 * {@link #undo()} simply calls the same toggle again.
 */
public class CompletedTaskCommand implements Command {
    private final TasksRepository tasksRepository;
    private final String taskId;

    public CompletedTaskCommand(TasksRepository tasksRepository, String taskId) {
        this.tasksRepository = tasksRepository;
        this.taskId = taskId;
    }

    @Override
    public void execute() throws Exception {
        TaskCache.getInstance().toggleCompleted(taskId);
        tasksRepository.toggleCompleted(taskId);
    }

    @Override
    public void undo() throws Exception {
        // Toggle is its own inverse.
        TaskCache.getInstance().toggleCompleted(taskId);
        tasksRepository.toggleCompleted(taskId);
    }
}
