package csusm.cougarplanner.commandPattern;

import csusm.cougarplanner.io.TasksRepository;
import csusm.cougarplanner.models.Task;

public class CreateTaskCommand implements Command {

    private final TasksRepository tasksRepository;
    private final Task task;

    public CreateTaskCommand(TasksRepository tasksRepository, Task task) {
        this.tasksRepository = tasksRepository;
        this.task = task;
    }

    @Override
    public void execute() throws Exception {
        tasksRepository.upsert(task);
    }
}
