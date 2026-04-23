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
import javafx.scene.control.Button;
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
    private final CommandManager cmdManager = new CommandManager();
    private final TasksRepository tasksRepo = new TasksRepository();

    @FXML private HBox   filterBar;
    @FXML private VBox   taskListContainer;
    @FXML private Button undoButton;
    @FXML private Button redoButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        hydrateCacheFromCsv();
        refreshTaskList();
        updateUndoRedoButtons();
    }

    // Loads tasks.csv into the in-memory cache so tasks persist across app restarts.
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
     * Called after any add, delete, toggle, undo, or redo.
     */
    public void refreshTaskList()
    {
        taskListContainer.getChildren().clear();

        List<Task> tasks = TaskCache.getInstance().getAll();

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
                cmdManager.execute(new CreateTaskCommand(tasksRepo, newTask));
                refreshTaskList();
                updateUndoRedoButtons();
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
        cmdManager.execute(new DeleteAllTasksCommand(tasksRepo));
        refreshTaskList();
        updateUndoRedoButtons();
    }

    @FXML
    private void onUndoClicked()
    {
        cmdManager.undo();
        refreshTaskList();
        updateUndoRedoButtons();
    }

    @FXML
    private void onRedoClicked()
    {
        cmdManager.redo();
        refreshTaskList();
        updateUndoRedoButtons();
    }

    private void handleDelete(String taskId)
    {
        cmdManager.execute(new DeleteTaskCommand(tasksRepo, taskId));
        refreshTaskList();
        updateUndoRedoButtons();
    }

    private void handleToggle(String taskId)
    {
        cmdManager.execute(new CompletedTaskCommand(tasksRepo, taskId));
        refreshTaskList();
        updateUndoRedoButtons();
    }

    // Persists in-row edits (title, description, status, priority) to CSV.
    // Cache is already updated by the row controller; this only writes through.
    // Not added to the undo stack — see UpdateTaskCommand.isUndoable().
    // No refreshTaskList() — rebuilding the list would steal focus mid-edit.
    private void handleUpdate(Task updated)
    {
        cmdManager.execute(new UpdateTaskCommand(tasksRepo, updated));
    }

    private void updateUndoRedoButtons()
    {
        if (undoButton != null) undoButton.setDisable(!cmdManager.canUndo());
        if (redoButton != null) redoButton.setDisable(!cmdManager.canRedo());
    }
}
