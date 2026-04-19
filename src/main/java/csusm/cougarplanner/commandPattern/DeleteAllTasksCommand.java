package csusm.cougarplanner.commandPattern;

import csusm.cougarplanner.io.TasksRepository;
import csusm.cougarplanner.models.Task;

public class DeleteAllTasksCommand implements Command {

    private final TasksRepository tasksRepository;

    public DeleteAllTasksCommand(TasksRepository tasksRepository) {
        this.tasksRepository = tasksRepository;
    }

    @Override
    public void execute() throws Exception {
        tasksRepository.deleteAll();
    }
}
