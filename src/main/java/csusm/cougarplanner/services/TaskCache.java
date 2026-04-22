package csusm.cougarplanner.services;

import csusm.cougarplanner.models.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Session-scoped in-memory mirror of tasks.csv that the UI binds to.
 * Hydrated from CSV in TaskPanelController.initialize(); kept in sync on each command.
 * Not part of the LRU cache family used for remote data (assignments/announcements/courses) —
 * tasks are local user data with no API tier, so the three-tier pattern doesn't apply.
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
