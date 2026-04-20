package csusm.cougarplanner.commandPattern;

import csusm.cougarplanner.io.TasksRepository;
import csusm.cougarplanner.models.Task;

public class CompletedTaskCommand implements Command{
    private final TasksRepository tasksRepository;
    private final String taskId;

    public CompletedTaskCommand(TasksRepository tasksRepository, String taskId) {
        this.tasksRepository = tasksRepository;
        this.taskId = taskId;
    }

    @Override
    public void execute() throws Exception {
        tasksRepository.toggleCompleted(taskId);
    }
}
