package csusm.cougarplanner.models;

import csusm.cougarplanner.controllers.AssignmentDetailsComponentController;
import csusm.cougarplanner.util.DateTimeUtil;
import java.net.URL;
import java.time.LocalDate;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.scene.paint.Color;

public class AssignmentModuleManager implements Initializable {

    private final Assignment assignment;
    private final AssignmentDisplay assignmentDisplay;
    private final int assignmentDuration = (new Random()).nextInt(14) + 1; //number of days between the date assigned and the due date of the assignment
    //!!! assignment duration counts one less day than the complete range of days the assignment takes up;
    //    if the assignment goes from the 17th to the 20th, it takes up 4 total days. However, the assignment
    //    duration value stores 3 for this example. !!!
    private Integer bar = null; //the bar that the assignment belongs to, starts at 0 and increases the lower the bar is
    private Color assignmentColor = null; //this is where the color for this assignment is stored
    private boolean empty;

    private AssignmentDetailsComponentController assignmentDetailsComponent;

    /**
     * Default constructor, fills each value with blank or null
     */
    public AssignmentModuleManager() {
        this.assignment = new Assignment("", "", "", "", "", null, "");

        this.assignmentDisplay = new AssignmentDisplay(assignment, "");

        empty = true;
    }

    /**
     * Alternate constructor that accepts AssignmentDisplay classes.
     * @param assignment AssignmentDisplay class which contains its own course name
     */
    public AssignmentModuleManager(AssignmentDisplay assignment) {
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

        empty = false;
    }

    /**
     * Alternate constructor that accepts Assignment classes.
     * @param assignment Assignment class which contains most necessary information
     */
    public AssignmentModuleManager(Assignment assignment, String courseName) {
        this.assignment = assignment;
        this.assignmentDisplay = new AssignmentDisplay(assignment, courseName);

        empty = false;
    }

    /**
     * Alternate constructor that builds an assignment
     * @param assignment_id the identification code for the given assignment
     * @param course_id the identification code for the course hosting the assignment
     * @param course_name the name of the course hosting the assignment
     * @param assignment_name the name of the assignment
     * @param due_date the due date of the assignment
     * @param due_time the due time of the assignment
     * @param difficulty the difficulty of the assignment, a range from 1 to 5, null if empty
     */
    public AssignmentModuleManager(
        String assignment_id,
        String course_id,
        String course_name,
        String assignment_name,
        String due_date,
        String due_time,
        Integer difficulty
    ) {
        this.assignment = new Assignment(
            assignment_id,
            course_id,
            assignment_name,
            due_date,
            due_time,
            difficulty,
            "" // created_at not available in this constructor
        );

        this.assignmentDisplay = new AssignmentDisplay(assignment, course_name);

        empty = false;
    }

    public boolean isEmpty() {
        return empty;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public AssignmentDisplay getAssignmentDisplay() {
        return assignmentDisplay;
    }

    public int getAssignmentDuration() {
        return assignmentDuration;
    }

    /**
     * Gets the number of days that the assignment falls on within the given week
     * @param date the date of the start of the week, could be Sunday or Monday depending on weekStart
     * @param weekStart decides the starting date of the week; true - Sunday; false - Monday
     * @return returns a number of days
     */
    public int getAssignmentDurationWithin8DayWeek(LocalDate date, boolean weekStart) {
        int counter = 0;

        LocalDate weekBeginning = date;
        weekBeginning = weekBeginning.minusDays((weekStart) ? 0 : 1); //if the week starts on Monday, change it back to Sunday
        LocalDate weekEnd = weekBeginning.plusDays(7); //adding 7 days gets you back to Sunday of the next week

        //iterate through each day of the week, change week beginning to be the iterating variable
        for (; weekBeginning.isBefore(weekEnd.plusDays(1)); weekBeginning = weekBeginning.plusDays(1)) {
            //if the day is within the range of the assignment, add to the counter
            if (
                weekBeginning.isAfter(getAssignmentBeginning().minusDays(1)) &&
                weekBeginning.isBefore(DateTimeUtil.parseDate(assignment.getDueDate()).plusDays(1))
            ) {
                counter++;
            }
        }

        return counter;
    }

    public LocalDate getAssignmentBeginning() {
        return (DateTimeUtil.parseDate(assignment.getDueDate()).minusDays(assignmentDuration));
    }

    public LocalDate getAssignmentEnding() {
        return (DateTimeUtil.parseDate(assignment.getDueDate()));
    }

    public Integer getBarPosition() {
        return bar;
    }

    public void setBarPosition(Integer bar) {
        if (bar >= 0) {
            this.bar = bar;
        } else {
            this.bar = null;
        }
    }

    public void setColor(Color color) {
        assignmentColor = color;
    }

    public Color getColor() {
        return assignmentColor;
    }

    public AssignmentDetailsComponentController getAssignmentDetailsComponent() {
        return assignmentDetailsComponent;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {}
}
