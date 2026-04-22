package csusm.cougarplanner.controllers;

import csusm.cougarplanner.commandPattern.*;
import csusm.cougarplanner.io.TasksRepository;
import csusm.cougarplanner.models.Task;
import csusm.cougarplanner.services.TaskCache;
import csusm.cougarplanner.theme.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

// Controller for TaskPanel.fxml — the Tasks tab.
public class TaskPanelController implements Initializable
{
    // attributes linking to command manager and task repository
    private final CommandManager cmdManager = new CommandManager();
    private final TasksRepository tasksRepo = new TasksRepository();

    @FXML private HBox filterBar;         // Manal's filter controller adds ComboBoxes here
    @FXML private VBox taskListContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        hydrateCacheFromCsv();
        refreshTaskList();
    }

    // Loads tasks.csv into the in-memory cache so tasks persist across app restarts.
    // Cache starts empty on each launch; without this, saved tasks would never appear.
    private void hydrateCacheFromCsv()
    {
        TaskCache cache = TaskCache.getInstance();
        cache.removeAll();
        try
        {
            List<Task> fromDisk = tasksRepo.findAll();
            for (Task t : fromDisk)
            {
                cache.add(t);
            }
        }
        catch (IOException e)
        {
            System.err.println("Could not load tasks from CSV: " + e.getMessage());
        }
    }

    /**
     * Clears and rebuilds the task list from the cache.
     * Called after any add, delete, or toggle, and by Manal's filter bar on change.
     */
    public void refreshTaskList()
    {
        taskListContainer.getChildren().clear();

        List<Task> tasks = TaskCache.getInstance().getAll(); // Kenny's cache

        for (Task task : tasks)
        {
            try
            {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/csusm/cougarplanner/TaskRow.fxml"));
                VBox row = loader.load();
                TaskRowController rowCtrl = loader.getController();
                rowCtrl.init(task, this::handleDelete, this::handleToggle, this::handleUpdate);
                taskListContainer.getChildren().add(row);
            }
            catch (IOException e)
            {
                System.err.println("Could not load TaskRow.fxml: " + e.getMessage());
            }
        }
    }

    // Manal's filter controller calls this to get the HBox to add ComboBoxes into.
    public HBox getFilterBar()
    {
        return filterBar;
    }

    @FXML
    private void onAddTaskClicked()
    {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/csusm/cougarplanner/AddTaskDialog.fxml"));
            VBox dialogRoot = loader.load();
            AddTaskDialogController dialogCtrl = loader.getController();

            dialogCtrl.setOnTaskCreated(newTask ->
            {
                // CreateTaskCommand
                Command cmd = new CreateTaskCommand(tasksRepo, newTask);
                cmdManager.execute(cmd);

                TaskCache.getInstance().add(newTask); // connect to Maria's AddTaskCommand
                refreshTaskList();
            });

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            Scene dialogScene = new Scene(dialogRoot);
            ThemeManager.apply(dialogScene, ThemeManager.getCurrentTheme());
            stage.setScene(dialogScene);
            stage.showAndWait();

        }
        catch (IOException e)
        {
            System.err.println("Could not open AddTaskDialog: " + e.getMessage());
        }
    }

    @FXML
    private void onDeleteAllClicked()
    {
        // DeleteAllTasksCommand
        Command cmd = new DeleteAllTasksCommand(tasksRepo);
        cmdManager.execute(cmd);

        TaskCache.getInstance().removeAll(); // connect to Maria's DeleteAllTasksCommand
        refreshTaskList();
    }

    private void handleDelete(String taskId)
    {
        // DeleteTaskCommand
        Command cmd = new DeleteTaskCommand(tasksRepo, taskId);
        cmdManager.execute(cmd);

        TaskCache.getInstance().remove(taskId); // connect to Maria's DeleteTaskCommand
        refreshTaskList();
    }

    private void handleToggle(String taskId)
    {
        Command cmd = new CompletedTaskCommand(tasksRepo, taskId);
        cmdManager.execute(cmd);

        TaskCache.getInstance().toggleCompleted(taskId);
        refreshTaskList();
    }

    // Persists in-row edits (title, description, status, priority) to CSV.
    // Cache is already updated by the row controller; we only write through here.
    // No refreshTaskList() — rebuilding the list would steal focus mid-edit.
    private void handleUpdate(Task updated)
    {
        Command cmd = new UpdateTaskCommand(tasksRepo, updated);
        cmdManager.execute(cmd);
    }

    // handleStatusChange - update status
    // handlePriorityChange - update priority
    // handleDueDateChange - update due date
    // handleCourseChange - update course
    // handleEditTask - universal update task
        // this could be for a single button similar
        // to the delete button but just to save/update
        // a task. EX: you just change the status and then click a
        // button that will take the whole state of the task and
        // save what is there

}
