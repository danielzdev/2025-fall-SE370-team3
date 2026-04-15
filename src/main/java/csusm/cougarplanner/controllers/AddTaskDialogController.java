package csusm.cougarplanner.controllers;

import csusm.cougarplanner.io.CoursesRepository;
import csusm.cougarplanner.models.Course;
import csusm.cougarplanner.models.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Controller for AddTaskDialog.fxml.
 * Builds a Task from the form and passes it back to TaskPanelController via callback.
 */
public class AddTaskDialogController implements Initializable
{

    @FXML private TextField            titleField;
    @FXML private DatePicker           dueDatePicker;
    @FXML private TextArea             descriptionArea;
    @FXML private ComboBox<String>     courseComboBox;
    @FXML private ComboBox<String>     statusComboBox;
    @FXML private ComboBox<String>     priorityComboBox;
    @FXML private Label                errorLabel;
    @FXML private Button               saveButton;

    private Consumer<Task> onTaskCreated;
    private final Map<String, String> courseNameToId = new LinkedHashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        statusComboBox.getItems().addAll(
                Task.STATUS_NOT_STARTED, Task.STATUS_IN_PROGRESS, Task.STATUS_COMPLETED);
        statusComboBox.setValue(Task.STATUS_NOT_STARTED);

        priorityComboBox.getItems().addAll(
                Task.PRIORITY_HIGH, Task.PRIORITY_MEDIUM, Task.PRIORITY_LOW);
        priorityComboBox.setValue(Task.PRIORITY_MEDIUM);

        loadCourses();
    }

    public void setOnTaskCreated(Consumer<Task> onTaskCreated)
    {
        this.onTaskCreated = onTaskCreated;
    }

    @FXML
    private void onSaveClicked()
    {
        String title = titleField.getText();
        if (title == null || title.isBlank())
        {
            errorLabel.setText("Title is required.");
            errorLabel.setVisible(true);
            return;
        }

        LocalDate picked = dueDatePicker.getValue();
        String dueDate = (picked != null)
                ? String.format("%02d-%02d-%04d", picked.getMonthValue(), picked.getDayOfMonth(), picked.getYear())
                : null;

        String selectedCourse = courseComboBox.getValue();
        String courseId       = courseNameToId.getOrDefault(selectedCourse, null);

        Task newTask = new Task(
                title,
                descriptionArea.getText(),
                dueDate,
                courseId,
                statusComboBox.getValue(),
                priorityComboBox.getValue()
        );

        if (onTaskCreated != null) onTaskCreated.accept(newTask);
        closeDialog();
    }

    @FXML
    private void onCancelClicked()
    {
        closeDialog();
    }

    private void loadCourses()
    {
        courseComboBox.getItems().add("None");
        courseNameToId.put("None", null);
        try
        {
            for (Course c : new CoursesRepository().findAll())
            {
                courseComboBox.getItems().add(c.getCourseName());
                courseNameToId.put(c.getCourseName(), c.getCourseId());
            }
        }
        catch (IOException e)
        {
            System.err.println("Could not load courses: " + e.getMessage());
        }
        courseComboBox.setValue("None");
    }

    private void closeDialog()
    {
        ((Stage) saveButton.getScene().getWindow()).close();
    }
}
