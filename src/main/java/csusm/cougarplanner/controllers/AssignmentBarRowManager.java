package csusm.cougarplanner.controllers;

import csusm.cougarplanner.Launcher;
import csusm.cougarplanner.models.AssignmentModuleManager;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import static csusm.cougarplanner.util.ColorUtil.getAssignmentColor;

public class AssignmentBarRowManager {
    private final int DAYSINWEEK = 8;

    private BarComponentController[] barComponents = new BarComponentController[DAYSINWEEK]; //an array that contains all the bars in a given row
    private List<AssignmentModuleManager> assignments; //an array of all the assignments present in this bar
    private Integer row; //the bar that the assignment belongs to, starts at 0 and increases the lower the bar is
    private final LocalDate weekDisplayed; //the first day of the week of the displayed information, this will be changed to sunday if it isn't already
    private boolean weekStart;

    private int numberOfFilledSpaces = 0;
    private int numberOfAssignments;

    public AssignmentBarRowManager(List<AssignmentModuleManager> assignments, LocalDate weekDisplayed, int row) {
        this.assignments = assignments;
        this.row = row;
        this.numberOfAssignments = assignments.size();

        //if the week starts on a monday, change it to sunday so the BarRowController will function properly
        if (weekDisplayed.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
            this.weekDisplayed = weekDisplayed.minusDays(1);
            this.weekStart = false;
        } else {
            this.weekDisplayed = weekDisplayed;
            this.weekStart = true;
        }
        System.out.println("AssignmentBarRowManager object created");
    }

    /**
     * Takes an array of assignments and conveys their information to the barComponents
     */
    private void configureAssignments(List<AssignmentModuleManager> assignments) {
        System.out.println("Entered configure assignments");
        //iterate through all the assignments included in this bar
        for (int i = 0; i < assignments.size(); i++) {
            //the assignment and due dates of the assignment specified by the above for loop (i)
            LocalDate assignmentBeginning = assignments.get(i).getAssignmentBeginning();
            LocalDate assignmentEnding = assignments.get(i).getAssignmentEnding();

            //The bar starts at a default color specified by its row, this is the color of the first assignment in the bar.
            //The next assignments are given the default color of subsequent rows, which staggers the colors used
            Color assignmentColor = getAssignmentColor(assignments.get(i).getAssignmentDisplay().getCourseName().charAt(0) + row + i); //The color of assignment i in the bar.

            //iterate through every day of the assignment, from assignment date (beginning) to due date (ending)
            for (int j = 0; assignmentBeginning.plusDays(j).isBefore(assignmentEnding.plusDays(1)); j++) {

                LocalDate dayViewed = assignmentBeginning.plusDays(j); // the date currently being viewed by this iteration of the above for loop (j)

                //if the day viewed is included in the 8-day week.
                if (dayViewed.isAfter(weekDisplayed.minusDays(1)) && dayViewed.isBefore(weekDisplayed.plusWeeks(1).plusDays(1))) {
                    //get the week day of the day viewed
                    int weekDay = dayViewed.getDayOfWeek().getValue();

                    //fix the index (getValue() indexes the days of the week like: [Mon, Tue, Wed, Thu, Fri, Sat, Sun]
                    weekDay += (weekDay == 7) ? -7 : 0; //if the day of the week is 7 (Sun), subtract 7 to return index order to [Sun, Mon, Tue, Wed, Thu, Fri, Sat]

                    //if the day viewed is after the end of the traditional 7-day week bounds (after saturday w/ week starting on sunday)
                    if (assignmentBeginning.plusDays(j).isAfter(weekDisplayed.plusDays(6))) {
                        weekDay = 7; //the day of the assignment is Sun in the second week
                    }

                    //if we're viewing the assignment beginning
                    if (j == 0) {
                        String temp = "< " + assignments.get(i).getAssignment().getAssignmentName() + " >";
                        String result = temp;
                        if (temp.length() > 13) { result = temp.substring(0, 13) + "... >"; }
                        barComponents[weekDay].setText(result); //add the assignment name to the bar
                        barComponents[weekDay].setVisibleLabel(true); //make the text visible
                        barComponents[weekDay].setVisibleSideBlocks(false, true); //bevel the left corners, square the right ones
                    }

                    //if the assignment begins before the beginning of the week
                    if (dayViewed.isEqual(weekDisplayed.plusDays(weekStart ? 0 : 1)) && j > 0) {
                        String temp = "| " + assignments.get(i).getAssignment().getAssignmentName() + " >";
                        String result = temp;
                        if (temp.length() > 13) { result = temp.substring(0, 13) + "... >"; }
                        barComponents[weekDay].setText(result); //ad the assignment name to the bar
                        System.out.println(barComponents[weekDay].getLabel().getWidth());
                        barComponents[weekDay].setVisibleLabel(true); //make the text visible
                    }

                    //if we're looking at the assignment due date
                    if (j == assignments.get(i).getAssignmentDuration()) {
                        barComponents[weekDay].setVisibleSideBlocks(true, false); //square the left corners, bevel the right ones
                    }

                    //if the assignment is due on the same day it is assigned
                    if (assignments.get(i).getAssignmentDuration() == 0) {
                        barComponents[weekDay].setVisibleSideBlocks(false, false); //bevel every corner
                    }

                    barComponents[weekDay].setColor(assignmentColor); //set the color of the component
                    barComponents[weekDay].setVisibleParent(true); //make the bar component visible
                    System.out.println("Temp");
                }
            }
        }
    }

    /**
     * Makes the necessary assignment bars visible inside the provided
     *      container list.
     * @param courseContainers the containers for the assignment bars
     */
    public void renderBar(VBox[] courseContainers, boolean weekStart) {
        System.out.println("entered render bar");
        System.out.println("length of barComponents = " + barComponents.length);
        if (courseContainers.length != 7) { throw new InvalidParameterException(); }

        //fill the barComponents array with default bar components
        for (int i = 0; i < barComponents.length; i++) {
            try {
                FXMLLoader loader = new FXMLLoader(Launcher.class.getResource("BarComponent.fxml"));
                Parent temp = loader.load();

                barComponents[i] = loader.getController();

                if (weekStart) { //if the week starts on Sunday
                    if (i != 7) {
                        courseContainers[i].getChildren().add(temp);
                    }
                } else { //if the week starts on Monday
                    if (i != 0) {
                        courseContainers[i - 1].getChildren().add(temp);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("before configure assignments");
        configureAssignments(assignments); //fill the barComponents array with the assignments provided
    }

    public BarComponentController getBarComponent(int index) {
        if (index < 0 || index > DAYSINWEEK) { throw new InvalidParameterException(); }
        return barComponents[index];
    }

    public BarComponentController getBarComponent(String dayOfWeek) {
        dayOfWeek = dayOfWeek.toUpperCase().strip();
        String[] daysOfWeek = {"SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
        for (int i = 0; i < daysOfWeek.length; i++) {
            if (dayOfWeek.equals(daysOfWeek[i])) {
                return barComponents[i];
            }
        }

        throw new InvalidParameterException(); //invalid day of week
    }

    public BarComponentController[] getBarComponents() {
        return barComponents;
    }

    public int getNumberOfFilledSpaces() {
        return numberOfFilledSpaces;
    }

    public int getRow() {
        return row;
    }

    public int getNumberOfAssignments() {
        return numberOfAssignments;
    }
}
