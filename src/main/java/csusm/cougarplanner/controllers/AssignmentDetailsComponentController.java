package csusm.cougarplanner.controllers;

import static csusm.cougarplanner.util.ColorUtil.getColorBurn;

import csusm.cougarplanner.models.Assignment;
import csusm.cougarplanner.models.AssignmentDisplay;
import csusm.cougarplanner.util.ColorUtil;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class AssignmentDetailsComponentController implements Initializable {

    private Assignment assignment;
    private AssignmentDisplay assignmentDisplay;
    private Color backgroundColor;

    @FXML
    private AnchorPane hoverAssignmentWindow;

    @FXML
    private Label hoverAssignmentLabel;

    @FXML
    private Pane springGreenMask, chartreuseMask, yellowMask, orangeMask, redMask;

    private Pane[] difficultyMasks = new Pane[5];
    private String[] difficultyColors;
    private final Color topColor = Color.rgb(54, 54, 54); //hex color for the top value of a color effect

    public AssignmentDetailsComponentController(AssignmentDisplay assignment, Color color) {
        this.assignmentDisplay = assignment;
        this.assignment = new Assignment(
            assignmentDisplay.getAssignmentId(),
            assignmentDisplay.getCourseId(),
            assignmentDisplay.getAssignmentName(),
            assignmentDisplay.getDueDate(),
            assignmentDisplay.getDueTime(),
            assignmentDisplay.getDifficulty(),
            assignmentDisplay.getCreatedAt()
        );
        difficultyColors = new String[] { "springgreen", "chartreuse", "yellow", "orange", "red" };

        this.backgroundColor = color;

        hoverAssignmentLabel.setText(assignment.getAssignmentName());

        setColor(color);
    }

    public AssignmentDetailsComponentController(Assignment assignment, String courseName, Color color) {
        this.assignment = assignment;
        this.assignmentDisplay = new AssignmentDisplay(assignment, courseName);

        this.backgroundColor = color;

        hoverAssignmentLabel.setText(assignment.getAssignmentName());

        setColor(color);
    }

    public void setColor(String hexColor) {
        if (ColorUtil.validHexColor(hexColor)) {
            return;
        }

        changeColor(Color.web(hexColor));
    }

    public void setColor(Color color) {
        changeColor(color);
    }

    private void changeColor(Color bottomColor) {
        this.backgroundColor = bottomColor;

        Color colorBurn = getColorBurn(bottomColor, topColor);

        hoverAssignmentWindow.setStyle("-fx-background-color: " + bottomColor);
        hoverAssignmentWindow.setStyle("-fx-border-color: " + "transparent" + colorBurn + colorBurn + colorBurn);

        setDifficulty(assignment.getDifficulty());
    }

    public void setDifficulty(Integer difficulty) {
        if (difficulty == null) {
            return;
        }
        if (difficulty < 1 || difficulty > 5) {
            return;
        }
        if (assignment.getDifficulty() < 1 || assignment.getDifficulty() > 5) {
            assignment.setDifficulty(null);
            return;
        }

        for (Pane difficultyMask : difficultyMasks) {
            difficultyMask.setStyle("-fx-background-color: " + backgroundColor);
        }

        difficultyMasks[difficulty].setStyle("-fx-background-color: " + difficultyColors[difficulty]);
    }

    public void setText(String text) {
        hoverAssignmentLabel.setText(text);

        if (text.length() > 10) {
            updateWidth();
        }
    }

    private void updateWidth() {
        //set the new preferred width to the default (150) + average width of a size 16 arial letter (7.5) * number of letters over 10
        hoverAssignmentWindow.setPrefWidth(150 + 7.5 * (hoverAssignmentLabel.getText().length() - 10));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        difficultyMasks = new Pane[] { springGreenMask, chartreuseMask, yellowMask, orangeMask, redMask };
    }
}
