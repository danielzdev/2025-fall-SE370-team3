package csusm.cougarplanner.io;

import csusm.cougarplanner.models.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Repository class for managing Task data persistence in tasks.csv.
 *
 * Part of T03: Implement CSV layer for task.csv with upsert by ID functionality.
 */
public class TasksRepository {
    // CSV column headers matching the tasks.csv file specification
    private static final String[] HEADERS = {
            "taskID", "title", "description", "createdDate",
            "dueDate", "courseId", "status", "priority", "completed"
    };

    private final CsvReader csvReader;
    private final CsvWriter csvWriter;

    /**
     * Constructs a new TasksRepository with default CSV reader/writer.
     */
    public TasksRepository()
    {
        this.csvReader = new CsvReader();
        this.csvWriter = new CsvWriter();
    }

    /**
     * Retrieves all tasks from the tasks.csv file.
     *
     * @return List of all Task objects in the database
     * @throws IOException if the CSV file cannot be read
     */
    public List<Task> findAll() throws IOException
    {
        List<Map<String, String>> records = csvReader.readAll(CsvPaths.getTasksPath());
        return records.stream()
                .map(this::mapToTask)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing task or inserts a new one.
     *
     * @param task the Task object to update or insert
     * @throws IOException if the CSV file cannot be written
     */
    public void upsert(Task task) throws IOException
    {
        List<Task> allTasks = findAll();

        // Removes existing task with same ID
        allTasks.removeIf(t -> t.getTaskId().equals(task.getTaskId()));

        // Adds updated task
        allTasks.add(task);

        // Writes back to file
        List<Map<String, String>> records = allTasks.stream()
                .map(this::taskToMap)
                .collect(Collectors.toList());

        csvWriter.writeAll(CsvPaths.getTasksPath(), records, HEADERS);
    }

    /**
     * Upsert operation for multiple tasks.
     *
     * @param tasks List of tasks to upsert
     * @throws IOException if the CSV file cannot be written
     */
    public void upsertAll(List<Task> tasks) throws IOException
    {
        Map<String, Task> taskMap = new HashMap<>();

        // Loads existing tasks
        for (Task existing : findAll())
        {
            taskMap.put(existing.getTaskId(), existing);
        }

        // Updates with new tasks
        for (Task task : tasks)
        {
            taskMap.put(task.getTaskId(), task);
        }

        // Writes back
        List<Map<String, String>> records = taskMap.values().stream()
                .map(this::taskToMap)
                .collect(Collectors.toList());

        csvWriter.writeAll(CsvPaths.getTasksPath(), records, HEADERS);
    }

    /**
     * update task as completed
     */
    public void toggleCompleted(String taskId) throws IOException {
        // list of all tasks
        List<Task> allTasks = findAll();
        // look through all tasks
        for (Task t : allTasks) {
            // find tasks with taskId from parameter
            if (t.getTaskId().equals(taskId)) {
                // flip the boolean from false to true
                t.setCompleted(!t.isCompleted());
                // stop
                break;
            }
        }
        // write back to csv
        List<Map<String, String>> records = allTasks.stream()
                .map(this::taskToMap)
                .collect(Collectors.toList());

        csvWriter.writeAll(CsvPaths.getTasksPath(), records, HEADERS);
    }


    /**
     * Delete task by id
     */
    public void deleteById(String taskId) throws IOException {
        // get list of all tasks
        List<Task> allTasks = findAll();
        // remove task with the id == taskId in parameter
        allTasks.removeIf(t -> t.getTaskId().equals(taskId));
        // write back to csv
        List<Map<String, String>> records = allTasks.stream()
                .map(this::taskToMap)
                .collect(Collectors.toList());

        csvWriter.writeAll(CsvPaths.getTasksPath(), records, HEADERS);
    }

    /**
     * Delete all tasks
     */
    public void deleteAll() throws IOException {
        // create an empty list of tasks
        List<Task> emptyList = new ArrayList<>();
        // convert empty list to CSV rows
        List<Map<String, String>> records = emptyList.stream()
                .map(this::taskToMap)
                .collect(Collectors.toList());
        // write empty CSV back to file
        csvWriter.writeAll(CsvPaths.getTasksPath(), records, HEADERS);
    }


    /**
     * Converts a CSV record Map to a Task object.
     *
     * @param record Map representing a CSV row with snake_case keys
     * @return Task object populated from the CSV data
     */
    private Task mapToTask(Map<String, String> record)
    {
        // CsvReader lowercases all header names, so lookup keys must be lowercase too.
        // Mismatched case (e.g. "taskID") silently returns null and triggers the UUID
        // fallback below on every load — which rotates IDs and breaks deleteById.
        String taskId = record.get("taskid");
        if (taskId == null || taskId.isBlank()) {
            taskId = java.util.UUID.randomUUID().toString();
        }

        String created = record.get("createddate");
        if (created == null || created.isBlank()) {
            created = java.time.LocalDate.now().toString();
        }

        Task task = new Task();
        task.setTaskId(taskId);
        task.setTitle(record.get("title"));
        task.setDescription(record.get("description"));
        task.setCreatedDate(created);
        task.setDueDate(record.get("duedate"));
        task.setCourseId(record.get("courseid"));
        task.setStatus(record.get("status"));
        task.setPriority(record.get("priority"));
        task.setCompleted(Boolean.parseBoolean(record.get("completed")));
        return task;
    }

    /**
     * Converts a Task object to a CSV record Map.
     *
     * @param task the Task object to convert
     * @return Map representing a CSV row with snake_case keys
     */
    private Map<String, String> taskToMap(Task task)
    {
        Map<String, String> record = new HashMap<>();
        record.put("taskID", task.getTaskId());
        record.put("title", task.getTitle());
        record.put("description", task.getDescription());
        record.put("createdDate", task.getCreatedDate());
        record.put("dueDate", task.getDueDate());
        record.put("courseId", task.getCourseId());
        record.put("status", task.getStatus());
        record.put("priority", task.getPriority());
        record.put("completed", String.valueOf(task.isCompleted()));
        return record;
    }
}
