package csusm.cougarplanner.controllers;

import csusm.cougarplanner.models.Task;
import csusm.cougarplanner.services.TaskCache;
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

    @FXML private HBox filterBar;         // Manal's filter controller adds ComboBoxes here
    @FXML private VBox taskListContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        refreshTaskList();
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
                rowCtrl.init(task, this::handleDelete, this::handleToggle);
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
                TaskCache.getInstance().add(newTask); // connect to Maria's AddTaskCommand
                refreshTaskList();
            });

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(dialogRoot));
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
        TaskCache.getInstance().removeAll(); // connect to Maria's DeleteAllTasksCommand
        refreshTaskList();
    }

    private void handleDelete(String taskId)
    {
        TaskCache.getInstance().remove(taskId); // connect to Maria's DeleteTaskCommand
        refreshTaskList();
    }

    private void handleToggle(String taskId)
    {
        TaskCache.getInstance().toggleCompleted(taskId); // connect to Maria's ToggleCompletedCommand
        refreshTaskList();
    }
}
