package csusm.cougarplanner.commandPattern;

import csusm.cougarplanner.io.TasksRepository;
import csusm.cougarplanner.models.Task;

public class UpdateTaskCommand implements Command {

    private final TasksRepository tasksRepository;
    private final Task updatedTask;

    public UpdateTaskCommand(TasksRepository tasksRepository, Task updatedTask) {
        this.tasksRepository = tasksRepository;
        this.updatedTask = updatedTask;
    }

    @Override
    public void execute() throws Exception {
        tasksRepository.upsert(updatedTask);
    }

    @Override
    public void undo() {
        // In-row field edits are excluded from the undo stack (see isUndoable).
    }

    @Override
    public boolean isUndoable() { return false; }
}
