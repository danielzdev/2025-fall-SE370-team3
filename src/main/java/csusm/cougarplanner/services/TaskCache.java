package csusm.cougarplanner.services;

import csusm.cougarplanner.models.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Temporary for testing — Kenny replaces this with the real cache implementation.
 * Method signatures must stay the same so TaskPanelController keeps compiling.
 */
public final class TaskCache
{

    private static TaskCache instance;
    private final List<Task> store = new ArrayList<>();

    private TaskCache() {}

    public static TaskCache getInstance()
    {
        if (instance == null) instance = new TaskCache();
        return instance;
    }

    public List<Task> getAll()
    {
        return new ArrayList<>(store);
    }

    public void add(Task task)
    {
        store.add(task);
    }

    public void remove(String taskId)
    {
        store.removeIf(t -> t.getTaskId().equals(taskId));
    }

    public void removeAll()
    {
        store.clear();
    }

    public void toggleCompleted(String taskId)
    {
        store.stream()
                .filter(t -> t.getTaskId().equals(taskId))
                .findFirst()
                .ifPresent(t -> t.setCompleted(!t.isCompleted()));
    }

    public void update(Task updatedTask)
    {
        for (int i = 0; i < store.size(); i++)
        {
            if (store.get(i).getTaskId().equals(updatedTask.getTaskId()))
            {
                store.set(i, updatedTask);
                return;
            }
        }
    }
    public Optional<Task> findById(String taskId)
    {
        return store.stream()
                .filter(t -> t.getTaskId().equals(taskId))
                .findFirst();
    }
}
