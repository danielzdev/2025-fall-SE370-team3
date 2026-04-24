package csusm.cougarplanner.controllers;

import csusm.cougarplanner.FilterPattern.TaskFilter;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;

import csusm.cougarplanner.FilterPattern.AndFilter;


/**
 * Controller for TaskPanel.fxml — the Tasks tab.
 * <p>
 * Wires together the three subsystems that drive this tab:
 * <ul>
 *   <li>The {@link TaskCache} singleton, which mirrors tasks.csv in memory</li>
 *   <li>The {@link TasksRepository}, which reads/writes the CSV file</li>
 *   <li>The {@link CommandManager}, which routes every mutation through
 *       the Command pattern so add/delete/toggle are undoable</li>
 * </ul>
 * Every user action (add, delete, toggle, undo, redo) goes through the
 * command manager, then calls {@link #refreshTaskList()} to rebuild the
 * visible rows and {@link #updateUndoRedoButtons()} to re-enable/disable
 * the undo/redo buttons. The only exception is in-row field edits, which
 * intentionally skip the undo stack and the list refresh (see {@link #handleUpdate}).
 */
public class TaskPanelController implements Initializable
{
    private final CommandManager cmdManager = new CommandManager();
    private final TasksRepository tasksRepo = new TasksRepository();

    @FXML private HBox   filterBar;
    @FXML private VBox   taskListContainer;
    @FXML private Button undoButton;
    @FXML private Button redoButton;
    private FilterBarController filterBarController;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyy");


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        hydrateCacheFromCsv();
        filterBarController = new FilterBarController(
                filterBar,
                this::refreshTaskList
        );

        // Add "Clear Filters" button
        Button clearFiltersBtn = new Button("Clear Filters");
        clearFiltersBtn.getStyleClass().add("clear-filters-button");
        clearFiltersBtn.setOnAction(e -> {
            filterBarController.resetFilters();
            refreshTaskList();
        });
        filterBar.getChildren().add(clearFiltersBtn);

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

        List<Task> allTasks = TaskCache.getInstance().getAll();// Kenny's cache
        // Apply the active filter (course/status/priority/due-date combo) if the
        // filter bar has one; otherwise fall through with the full task list.
        List<Task> tasksToDisplay;
        TaskFilter activeFilter = filterBarController.getCurrentFilter();


        if (activeFilter != null) {
            tasksToDisplay = activeFilter.filter(allTasks);
        } else {
            tasksToDisplay = allTasks;
        }

        // Apply sorting
        String sortOption = filterBarController.getCurrentSortOption();
        tasksToDisplay = sortTasksByDueDate(tasksToDisplay, sortOption);

        for (Task task : tasksToDisplay)
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
    /**
     * Sorts tasks by due date based on the selected option
     */
    private List<Task> sortTasksByDueDate(List<Task> tasks, String sortOption) {
        if (sortOption == null || "Sort: Default".equals(sortOption)) {
            return tasks;
        }

        List<Task> sorted = new ArrayList<>(tasks);

        if ("Sort: Due Date (Earliest First)".equals(sortOption)) {
            sorted.sort((t1, t2) -> {
                LocalDate date1 = parseDate(t1.getDueDate());
                LocalDate date2 = parseDate(t2.getDueDate());

                // Tasks with no due date go to the bottom
                if (date1 == null && date2 == null) return 0;
                if (date1 == null) return 1;
                if (date2 == null) return -1;

                return date1.compareTo(date2); // Earliest first
            });
        } else if ("Sort: Due Date (Latest First)".equals(sortOption)) {
            sorted.sort((t1, t2) -> {
                LocalDate date1 = parseDate(t1.getDueDate());
                LocalDate date2 = parseDate(t2.getDueDate());

                // Tasks with no due date go to the bottom
                if (date1 == null && date2 == null) return 0;
                if (date1 == null) return 1;
                if (date2 == null) return -1;

                return date2.compareTo(date1); // Latest first
            });
        }

        return sorted;
    }

    /**
     * Parses a date string to LocalDate
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
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
