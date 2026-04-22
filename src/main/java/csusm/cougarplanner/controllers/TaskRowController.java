package csusm.cougarplanner.controllers;

import csusm.cougarplanner.io.CoursesRepository;
import csusm.cougarplanner.models.Course;
import csusm.cougarplanner.models.Task;
import csusm.cougarplanner.services.TaskCache;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

// Controller for a single task card (TaskRow.fxml).
public class TaskRowController
{

    @FXML private VBox             taskRowRoot;
    @FXML private TextField        titleField;
    @FXML private Label            dateCreatedLabel;
    @FXML private Label            courseLabel;
    @FXML private Button           completedButton;
    @FXML private Button           deleteButton;
    @FXML private TextArea         descriptionArea;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private ComboBox<String> priorityComboBox;
    @FXML private Button           checkIconButton;
    @FXML private Button           deleteIconButton;

    private Task task;
    private Consumer<String> onDelete;
    private Consumer<String> onToggle;
    private Consumer<Task> onUpdate;
    private final Map<String, String> courseIdToName = new LinkedHashMap<>();

    // Same border style used by both dropdowns
    private static final String COMBO_BASE =
            "-fx-border-color: #cccccc; -fx-border-radius: 6; -fx-background-radius: 6;" +
                    "-fx-font-family: 'Arial Rounded MT Bold'; -fx-font-size: 10;";

    public void init(Task task, Consumer<String> onDelete, Consumer<String> onToggle)
    {
        init(task, onDelete, onToggle, null);
    }

    public void init(Task task, Consumer<String> onDelete, Consumer<String> onToggle, Consumer<Task> onUpdate)
    {
        this.task     = task;
        this.onDelete = onDelete;
        this.onToggle = onToggle;
        this.onUpdate = onUpdate;
        loadCourseNames();
        setupDropdowns();
        setupEditListeners();
        populate();
    }

    // Commit title/description edits when the field loses focus.
    // Without this, typing in the row is cache-only and lost on restart.
    private void setupEditListeners()
    {
        titleField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) commitTitleIfChanged();
        });
        descriptionArea.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) commitDescriptionIfChanged();
        });
    }

    private void commitTitleIfChanged()
    {
        String newTitle = titleField.getText();
        String current = task.getTitle() == null ? "" : task.getTitle();
        String next = newTitle == null ? "" : newTitle;
        if (!next.equals(current))
        {
            task.setTitle(next);
            fireUpdate();
        }
    }

    private void commitDescriptionIfChanged()
    {
        String newDesc = descriptionArea.getText();
        String current = task.getDescription() == null ? "" : task.getDescription();
        String next = newDesc == null ? "" : newDesc;
        if (!next.equals(current))
        {
            task.setDescription(next);
            fireUpdate();
        }
    }

    private void fireUpdate()
    {
        TaskCache.getInstance().update(task);
        if (onUpdate != null) onUpdate.accept(task);
    }

    public VBox getRoot() { return taskRowRoot; }

    public void refresh(Task updatedTask)
    {
        this.task = updatedTask;
        populate();
    }

    private void loadCourseNames()
    {
        try {
            for (Course c : new CoursesRepository().findAll())
            {
                courseIdToName.put(c.getCourseId(), c.getCourseName());
            }
        }
        catch (IOException e)
        {
            System.err.println("Could not load courses for row: " + e.getMessage());
        }
    }

    private void setupDropdowns()
    {
        statusComboBox.getItems().addAll(
                Task.STATUS_NOT_STARTED, Task.STATUS_IN_PROGRESS, Task.STATUS_COMPLETED);

        priorityComboBox.getItems().addAll(
                Task.PRIORITY_HIGH, Task.PRIORITY_MEDIUM, Task.PRIORITY_LOW);

        statusComboBox.setOnAction(e -> {
            String selected = statusComboBox.getValue();
            if (selected != null && !selected.equals(task.getStatus()))
            {
                task.setStatus(selected);
                fireUpdate();
            }
        });

        priorityComboBox.setOnAction(e ->
        {
            String selected = priorityComboBox.getValue();
            if (selected != null && !selected.equals(task.getPriority()))
            {
                task.setPriority(selected);
                fireUpdate();
                applyPriorityStyle(selected);
            }
        });
    }

    private void populate()
    {
        titleField.setText(task.getTitle() != null ? task.getTitle() : "");
        descriptionArea.setText(task.getDescription() != null ? task.getDescription() : "");

        // Due Date
        dateCreatedLabel.setText(
                task.getDueDate() != null && !task.getDueDate().isBlank()
                        ? task.getDueDate() : "No due date");

        // Course
        String courseId = task.getCourseId();
        courseLabel.setText(
                (courseId != null && courseIdToName.containsKey(courseId))
                        ? courseIdToName.get(courseId) : "No course");

        // Dropdowns
        statusComboBox.setValue(
                task.getStatus() != null ? task.getStatus() : Task.STATUS_NOT_STARTED);
        priorityComboBox.setValue(
                task.getPriority() != null ? task.getPriority() : Task.PRIORITY_MEDIUM);

        // Apply styles
        applyStatusStyle();
        applyPriorityStyle(priorityComboBox.getValue());
        applyCompletedStyle(task.isCompleted());
    }

    // Status dropdown background color
    private void applyStatusStyle()
    {
        statusComboBox.setStyle("-fx-background-color: white; " + COMBO_BASE);
    }

    /**
     * Priority dropdown: high=red, medium=yellow, low=white.
     */
    private void applyPriorityStyle(String priority)
    {
        if (priority == null) return;
        String bg = switch (priority.toLowerCase())
        {
            case Task.PRIORITY_HIGH   -> "#ffcccc";
            case Task.PRIORITY_MEDIUM -> "#fff5cc";
            default                   -> "white";
        };
        priorityComboBox.setStyle("-fx-background-color: " + bg + "; " + COMBO_BASE);
    }

    private void applyCompletedStyle(boolean completed)
    {
        if (completed)
        {
            taskRowRoot.setStyle(
                    "-fx-background-color: #e8e8e8; -fx-background-radius: 10; -fx-padding: 10;" +
                            "-fx-border-color: #aaaaaa; -fx-border-radius: 10; -fx-border-width: 1;");
            titleField.setStyle(
                    "-fx-background-color: #dddddd; -fx-text-fill: #888888;" +
                            "-fx-background-radius: 6; -fx-border-color: #cccccc; -fx-border-radius: 6;" +
                            "-fx-font-family: 'Arial Rounded MT Bold'; -fx-font-size: 12;");
            checkIconButton.setText("↩");
            completedButton.setText("Undo");
        }
        else
        {
            taskRowRoot.setStyle(
                    "-fx-background-color: #f0f0f0; -fx-background-radius: 10; -fx-padding: 10;" +
                            "-fx-border-color: #cccccc; -fx-border-radius: 10; -fx-border-width: 1;");
            titleField.setStyle(
                    "-fx-background-color: white; -fx-text-fill: black;" +
                            "-fx-background-radius: 6; -fx-border-color: #cccccc; -fx-border-radius: 6;" +
                            "-fx-font-family: 'Arial Rounded MT Bold'; -fx-font-size: 12;");
            checkIconButton.setText("✓");
            completedButton.setText("Completed");
        }
    }

    @FXML private void onCompletedClicked() { onToggle.accept(task.getTaskId()); }
    @FXML private void onDeleteClicked()    { onDelete.accept(task.getTaskId()); }
}
