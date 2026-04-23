package csusm.cougarplanner.commandPattern;

import csusm.cougarplanner.io.TasksRepository;
import csusm.cougarplanner.services.TaskCache;

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
