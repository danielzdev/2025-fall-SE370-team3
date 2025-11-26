package csusm.cougarplanner.models;

import csusm.cougarplanner.Launcher;
import csusm.cougarplanner.controllers.AssignmentBarRowManager;
import csusm.cougarplanner.controllers.AssignmentsCourseHeaderComponentController;
import csusm.cougarplanner.util.DateTimeUtil;
import csusm.cougarplanner.util.WeekUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <pre>
 * There is one course manager per course, per week.
 *
 * It is the responsibility of the CourseManager object to
 *      process, assemble, and render the assignments from
 *      a given course into a 7-node wide VBox, provided
 *      externally.
 * CourseManager does not have a default constructor
 *      and needs to be constructed using either
 *      a traditional array of AssignmentDisplay objects
 *      or two traditional arrays, one containing
 *      Assignment objects and one containing String
 *      objects (courseName strings). Additionally, the
 *      start of the week (Sunday or Monday) and the
 *      week displayed (LocalDate containing the date of
 *      the first day of the week) must be passed into
 *      the constructor.
 * After the CourseManager object has been constructed
 *      all you need to do is call renderHeaders(VBox)
 *      and renderBars(VBox) (in that order) on the
 *      course whose assignments you want present in
 *      the display of the planner, and they will populate
 *      the week view.
 * Def:
 *      Bar: A bar is a visual representation of an
 *      assignment. It is made up of nodes (bar components),
 *      and it spans the entire duration of the assignment
 *      (qualified by the boundaries of the week being
 *      viewed). There is one bar component per day the
 *      assignment takes up. Here's a visual:
 * Assignment:  [NULL,  NULL, NULL, NULL, START,  occ,  occ, occ, occ, END]
 * Week:        [ Sun,   Mon,  Tue,  Wed,   Thu,  Fri,  Sat]
 * Bar:         [NULL,  NULL, NULL, NULL, <================]
 * |
 * With an additional assignment:
 * Assignment:  [NULL, START,  END, NULL, NULL, NULL, NULL]
 * Bar:         [NULL, <=========>, NULL, <===============]
 * </pre>
 */
public class CourseManager {
    private final int DAYSINWEEK = 8; //this value is 8 to allow for "first day of the week" toggling
    //the week includes a range of days from sunday - sunday (inclusive) and maps the behavior and location
    //of the assignments within this range as if it were a regular week. When rendering assignments, only
    //the necessary parts of the assignment mapping are used.
    //Multiple weeks of the same course looks like this:
    //Week 1:      [Sun, Mon, Tue, Wed, Thu, Fri, Sat, Sun]
    //Week 2:                                         [Sun, Mon, Tue, Wed, Thu, Fri, Sat, Sun]
    //There is exactly one day of overlap between these objects

    //array of all assignments present inside weekDisplayed, contains assignments due this week and assignments
    //        assigned this week (may include assignments that are due in following weeks or assignments that
    //        were assigned in a previous week)
    private AssignmentModuleManager[] assignmentsInCourseThisWeek;
    private int numberOfAssignments;         //number of assignments included for this week, length of assignmentsInCourseThisWeek
    private int numberOfAssignmentsRendered = 0; //the number of assignments displayed on the 8-day week
    private boolean weekStart;               //identifies which day the week starts on. true - sunday; false - monday
    private LocalDate weekDisplayed;         //stores the first day of the week. Changes based on weekStart

    //generated attributes
    private int[] assignmentsDueThisWeek;                  //indices of the assignments that are due this week, refers to assignments in assignmentsInCourseThisWeek
    private int[] assignmentsNotDueThisWeek;               //indices of the assignments that are not due this week, refers to assignments in assignmentsInCourseThisWeek
    private List<List<AssignmentModuleManager>> placementArray = new LinkedList<>(); //array that maps out the placement of each assignment for this course for this week
    private boolean[] assignmentsEntered;                  //indices of the assignments that have been entered into the placement array. Maps to assignmentsInCourseThisWeek

    private final AssignmentsCourseHeaderComponentController[] courseHeaderComponents = new AssignmentsCourseHeaderComponentController[7];
    private AssignmentBarRowManager[] assignmentBars;

    /**
     * Constructor that takes an array of AssignmentDisplay objects.
     *      This will fill the assignmentModuleManager array that's
     *      necessary for most of the display features of each assignment.
     * assignmentsInCourseThisWeek is sorted in chronological order with
     *      respect to assignment due dates
     * @param assignments a list of AssignmentDisplay objects
     */
    public CourseManager(AssignmentDisplay[] assignments, boolean weekStart, LocalDate weekDisplayed) {
        assignmentsInCourseThisWeek = new AssignmentModuleManager[assignments.length];

        for (int i = 0; i < assignments.length; i++) {
            assignmentsInCourseThisWeek[i] = new AssignmentModuleManager(assignments[i]);
        }

        numberOfAssignments = assignments.length;

        for (int i = 0; i < numberOfAssignments; i++) {
            System.out.println(assignmentsInCourseThisWeek[i].getAssignment().getAssignmentName());
        }

        System.out.println("before configure");
        configure(weekStart, weekDisplayed);
        System.out.println("after configure");
    }

    /**
     * Constructor that takes an array of Assignment and String objects.
     *      This will fill the assignmentModuleManager array that's
     *      necessary for most of the display features of each assignment.
     * assignmentsInCourseThisWeek is sorted in chronological order with
     *      respect to assignment due dates
     * This is an alternate constructor that accepts assignment objects
     *      and course names instead of AssignmentDisplay objects
     *
     * @param assignments      assignment objects
     * @param courseNames      course objects
     */
    public CourseManager(Assignment[] assignments, String[] courseNames, boolean weekStart, LocalDate weekDisplayed) {
        if (assignments.length != courseNames.length) { throw new IllegalArgumentException(); }

        assignmentsInCourseThisWeek = new AssignmentModuleManager[assignments.length];
        for (int i = 0; i < assignments.length; i++) {
            assignmentsInCourseThisWeek[i] = new AssignmentModuleManager(assignments[i], courseNames[i]);
        }

        numberOfAssignments = assignments.length;

        configure(weekStart, weekDisplayed);
    }

    /**
     * This function is called by the constructors. It will:
     *      - sort the array of assignment modules in chronological order
     *      - initialize the values for weekStart and weekDisplayed
     *      - fill the assignmentsDueThisWeek and the assignmentsNotDueThisWeek arrays
     *      - generate the placementArray
     * @param weekStart identifies which day the week starts on. true - sunday; false - monday
     * @param weekDisplayed stores the first day of the week. Changes based on weekStart
     */
    private void configure(boolean weekStart, LocalDate weekDisplayed) {
        quickSort(assignmentsInCourseThisWeek, 0, assignmentsInCourseThisWeek.length - 1);

        System.out.println(" course manager - configure - assignment durations:");
        for (int i = 0; i < assignmentsInCourseThisWeek.length; i++) {
            System.out.println("                          " + assignmentsInCourseThisWeek[i].getAssignment().getAssignmentName() + ": " + DateTimeUtil.formatDate(assignmentsInCourseThisWeek[i].getAssignmentBeginning()));
            System.out.println("                          Duration: " + assignmentsInCourseThisWeek[i].getAssignmentDuration());
        }

        this.weekStart = weekStart;
        this.weekDisplayed = weekDisplayed;

        System.out.println(" course manager - configure - weekStart = " + ((weekStart) ? "Sunday" : "Monday"));
        System.out.println(" course manager - configure - weekDisplayed = " + DateTimeUtil.formatDate(weekDisplayed));

        //I don't know how large either of these arrays are going to be and checking for that beforehand is unnecessarily intensive (probably)
        int[] temporaryAssignmentsDueArray = new int[assignmentsInCourseThisWeek.length];
        int tracker1 = 0;
        int[] temporaryAssignmentsNotDueArray = new int[assignmentsInCourseThisWeek.length];
        int tracker2 = 0;

        for (int i = 0; i < assignmentsInCourseThisWeek.length; i++) {
            Assignment assignment = assignmentsInCourseThisWeek[i].getAssignment();
            //convert the due date and time to a LocalDateTime that can be compared to another
            LocalDate dueDate = DateTimeUtil.parseDate(assignment.getDueDate());

            if (WeekUtil.isDateInWeek(dueDate, weekDisplayed, (weekStart) ? "sunday" : "monday")) {
                temporaryAssignmentsDueArray[tracker1++] = i;
            } else {
                temporaryAssignmentsNotDueArray[tracker2++] = i;
            }
        }

        assignmentsDueThisWeek = new int[tracker1];
        assignmentsNotDueThisWeek = new int[tracker2];

        System.arraycopy(temporaryAssignmentsDueArray, 0, assignmentsDueThisWeek, 0, tracker1);
        System.arraycopy(temporaryAssignmentsNotDueArray, 0, assignmentsNotDueThisWeek, 0, tracker2);

        System.out.println("assignmentsDueThisWeek = " + assignmentsDueThisWeek.length);
        System.out.println("assignmentsNotDueThisWeek = " + assignmentsNotDueThisWeek.length);

        assignmentsEntered = new boolean[assignmentsInCourseThisWeek.length];
        Arrays.fill(assignmentsEntered, false);

        System.out.println(" course manager - configure - before generatePlacementArray");
        generatePlacementArray();
        System.out.println(" course manager - configure - after generatePlacementArray");

        System.out.println("---------------------------------PLACEMENT ARRAY--------------------------------------");
        for (List<AssignmentModuleManager> bar : placementArray) {
            for (AssignmentModuleManager element : bar) {
                System.out.print(element.getAssignment().getAssignmentName() + " ");
            }
            System.out.println(); // Move to the next line after each row
        }
        System.out.println("---------------------------------PLACEMENT ARRAY--------------------------------------");
    }

    /**
     * Partition function for quick sort implementation. This function will establish a pivot, then
     *      it will search, from the left, for a value greater than the pivot. When it finds this
     *      larger value, the function will continue to search right-ward for something smaller than
     *      the pivot to swap with. If nothing smaller than the pivot is found, the function is
     *      done, but otherwise, these values will swap. When the function is done, the pivot swaps
     *      with i + 1, which splits the data in two halves (roughly), with the left-ward values
     *      being less than the pivot, and the right-ward values being greater than the pivot.
     * This standard partition function is adapted to compare the due dates of each of the
     *      assignments provided.
     * @param assignments an array of the assignments found between the low and high indices
     * @param low lower index of the given partition
     * @param high upper index of the given partition
     * @return the location that the pivot changed to int the end is returned, this is the new partition
     */
    private static int partition(AssignmentModuleManager[] assignments, int low, int high) {
        Assignment assignment = assignments[high].getAssignment(); //get the assignment we're using as the pivot

        //convert the due date and time to a LocalDateTime that can be compared to another
        LocalDateTime pivot = DateTimeUtil.parseDateTime(assignment.getDueDate() + " " + assignment.getDueTime());
        int i = low - 1;

        //sort items lower than pivot left
        for (int j = low; j <= high - 1; j++) {
            if (DateTimeUtil.parseDateTime(assignments[j].getAssignment().getDueDate() + " " + assignments[j].getAssignment().getDueTime()).isBefore(pivot)) {
                i++;
                swap(assignments, i, j);
            }
        }

        //center the pivot
        swap(assignments, i + 1, high);

        return i + 1; //return the newly created partition
    }

    /**
     * Standard swap function
     * @param assignments the array of assignments relevant to the course
     * @param i the left-most assignment to be swapped
     * @param j the right-most assignment to be swapped
     */
    private static void swap(AssignmentModuleManager[] assignments, int i, int j) {
        AssignmentModuleManager temp = assignments[i];
        assignments[i] = assignments[j];
        assignments[j] = temp;
    }

    /**
     * The quick sort function will partition the given array recursively
     *      until the array is sorted.
     * This function will sort the array of assignments included in the
     *      course manager in chronological order.
     * @param assignments teh array of assignments relevant to the course
     * @param low the lower index of the partition being sorted. This value
     *            is zero for external use of the quickSort function.
     * @param high the upper index of the partition being sorted. This value
     *             is (arraySize - 1) for external use of the quickSort
     *             function.
     */
    private static void quickSort(AssignmentModuleManager[] assignments, int low, int high) {
        if (low < high) { //only do a quick sort if the bounds contain more than one date

            int part = partition(assignments, low, high); //store the partition created by this function call

            //split into smaller arrays on the left and right of partition and quick sort again
            quickSort(assignments, low, part - 1);
            quickSort(assignments, part + 1, high);
        }
    }

    /**
     * Generates a placement array of AssignmentModuleManagers that defines the exact locations and positions
     *      of each assignment inside of and across rows.
     *      The placementArray logs the location of every assignment with a 2d LinkedList object (I chose
     *      LinkedList objects because placementArray is going to be read from more than it is going to be
     *      written to)
     */
    private void generatePlacementArray() {
        //cycle to the next row (bar) after editing each column (day)
        for (int row = 0; numberOfUnenteredAssignments() > 0; row++) {
            //the number of open spaces remaining in a given bar, this value should initially be equal to DAYSINWEEK, as no spaces are open at first

            //each iteration of this do-while loop will attempt to enter an assignment into the current row of the placementArray
            for (int iterations = DAYSINWEEK; iterations >= 0; iterations--) {
                //search for assignments beginning after previously entered assignment's
                //      due date. Search specifically in assignmentsDueThisWeek first
                AssignmentModuleManager assignment = findViableAssignment(assignmentsDueThisWeek, row); //finds an assignment less than or equal to the given size

                if (assignment != null) { //if an assignment was found
                    addAssignmentToBar(row, assignment);
                } else { //if no assignment due this week is found, search through assignmentsNotDueThisWeek
                    assignment = findViableAssignment(assignmentsNotDueThisWeek, row);

                    if (assignment != null) {
                        addAssignmentToBar(row, assignment); //adding this assignment will always fill the rest of this row of the placementArray
                    }
                    //if no viable assignments were found in both possible arrays:
                    break; //when the loop reaches this point, no more assignments can be entered into this row
                }
            }
        }
    }

    /**
     * calculates the number of assignments from
     *      assignmentsInCourseThisWeek that haven't
     *      been entered into placementArray yet.
     * @return returns the number of unentered assignments
     */
    private int numberOfUnenteredAssignments() {
        int count = 0;
        for (int i = 0; i < assignmentsEntered.length; i++) {
            if (!assignmentsEntered[i]) {
                count++;
            }
        }

        return count;
    }

    /**
     * Returns the first assignment in assignmentsInCourseThisWeek that fits within the specified spaceAvailable.
     * @param assignmentIndices an array of indices that refer to select assignments in assignmentsInCourseThisWeek
     * @return returns the assignment that fits, null if nothing is found
     */
    private AssignmentModuleManager findViableAssignment(int[] assignmentIndices, int row) {
        LocalDate dueDateOfLastEnteredAssignment;
        AssignmentModuleManager assignment;

        if (numberOfAssignmentsInBar(row) == 0) { //when this is true, the first unentered assignment will be returned
            for (int assignmentIndex : assignmentIndices) {
                //get the next assignment given by the assignment indices array
                assignment = assignmentsInCourseThisWeek[assignmentIndex];

                if (!assignmentsEntered[assignmentIndex]) {
                    assignmentsEntered[assignmentIndex] = true;
                    return assignment;
                }
            }
        } else { //search for an assignment that begins after the due date of the last assignment
            //get the due date of the last assignment in placementArray
            dueDateOfLastEnteredAssignment = DateTimeUtil.parseDate(placementArray.get(row).get(placementArray.get(row).size() - 1).getAssignment().getDueDate());

            for (int assignmentIndex : assignmentIndices) {
                //get the next assignment given by the assignment indices array
                assignment = assignmentsInCourseThisWeek[assignmentIndex];

                if (assignment.getAssignmentBeginning().isAfter(dueDateOfLastEnteredAssignment) && !assignmentsEntered[assignmentIndex]) {
                        assignmentsEntered[assignmentIndex] = true;
                        return assignment;
                }
            }
        }

        return null; //if there was no assignment that fit in the available space
    }

    /**
     * finds the number of assignments in the given bar (row)
     * @param row the row whose assignments we're looking for
     * @return returns the number of assignments in the provided row
     */
    private int numberOfAssignmentsInBar(int row) {
        if (placementArray.size() < row + 1) {
            return 0;
        }
        return placementArray.get(row).size();
    }

    /**
     * Adds an assignment into the placementArray
     * @param row the row of the placementArray that this function will enter an assignment into
     * @param assignment the assignment that is going to be entered
     */
    private void addAssignmentToBar(int row, AssignmentModuleManager assignment) {
        assignment.setBarPosition(row);

        //if the assignment lies outside  the boundaries of the 8-day week, don't add it to the placementArray
        if (!assignment.getAssignmentBeginning().isAfter(weekDisplayed.plusDays((weekStart) ? 7 : 6))) {
            if (placementArray.size() < row + 1) {
                placementArray.add(new LinkedList<>());
            }
            placementArray.get(row).add(assignment);

            numberOfAssignmentsRendered++;
        }
    }

    /**
     * Will fill the courseHeadersComponents objects with
     *      the title of the course these assignments belong to
     * |
     * !!! this function is only to be called by renderHeaders() !!!
     *      that's because modifications can only be made to
     *      fxml objects after they have been instantiated
     *      and placed into the MainPageController.
     *      renderHeaders() does this prior to calling this
     *      function.
     */
    private void fillCourseHeaders() {
        System.out.println(" course manager - enter fill course headers");
        if (assignmentsInCourseThisWeek.length < 1) { return; }

        //insert empty text in each course header except for the first and last ones
        for (int i = 0; i < courseHeaderComponents.length; i++) {
            courseHeaderComponents[i].setText("");
        }

        //get the name of the course from an arbitrary assignment (all the assignments in this array belong to the same course)
        String courseName = assignmentsInCourseThisWeek[0].getAssignmentDisplay().getCourseName();

        //if the course name doesn't exist, then use the course id
        if (courseName == null || courseName.isEmpty()) {
            courseName = assignmentsInCourseThisWeek[0].getAssignment().getCourseId();
        }

        courseHeaderComponents[0].setText(courseName);
        courseHeaderComponents[6].setText(courseName);

        System.out.println(" course manager - exit fill course headers");
    }

    /**
     * Will fill the AssignmentBarRowManager objects with
     *      the information generated in the placementArray
     */
    private void fillAssignmentBars() {
        System.out.println(" course manager - enter fill assignment bars");
        assignmentBars = new AssignmentBarRowManager[placementArray.size()];

        for (int i = 0; i < assignmentBars.length; i++) {
            assignmentBars[i] = new AssignmentBarRowManager(placementArray.get(i), weekDisplayed, i);
        }
        System.out.println(" course manager - exit fill assignment bars");
    }


        /// private methods for constructor
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        /// public getter, setter, and bar modification methods


    public int getNumberOfAssignmentsRendered() {
        return numberOfAssignmentsRendered;
    }

    public int getNumberOfBars() {
        return placementArray.size();
    }

    public List<AssignmentModuleManager> getBar(int row) {
        if (row > placementArray.size() - 1) {
            return null;
        }

        return placementArray.get(row);
    }

    public AssignmentModuleManager[] getAllAssignmentModules() {
        return assignmentsInCourseThisWeek;
    }

    /**
     * Returns the number of assignments that are due for this course for this week.
     * @param weekStart weekStart parameter does not need to be the same as CourseManager's weekStart member
     * @return a traditional array of AssignmentModuleManagers.
     */
    public AssignmentModuleManager[] getAssignmentsDueThisWeek(boolean weekStart) {
        List<AssignmentModuleManager> assignmentsDueThisWeek = new LinkedList<>();
        LocalDate referenceWeek;

        //if the CourseManager's week starts on Sunday
        if (weekDisplayed.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            referenceWeek = weekDisplayed.plusDays((weekStart) ? 0 : 1); //get the correct first day of the week based on weekStart parameter
        } else {
            referenceWeek = weekDisplayed.minusDays((weekStart) ? 1 : 0); //get the correct first day of the week based on weekStart parameter
        }

        for (int i = 0; i < assignmentsInCourseThisWeek.length; i++) {
            if (WeekUtil.isDateInWeek(assignmentsInCourseThisWeek[i].getAssignmentEnding(), referenceWeek, (weekStart) ? "sunday" : "monday")) {

            }
        }
        return null;
    }

    public AssignmentModuleManager[] getAssignmentsNodDueThisWeek(boolean weekStart) {
return null;
    }

    public void addAssignment(Assignment assignment, String courseName) {
        addAssignmentHelper(new AssignmentDisplay(assignment, courseName));
    }

    public void addAssignment(AssignmentDisplay assignment) {
        addAssignmentHelper(assignment);
    }

    private void addAssignmentHelper(AssignmentDisplay assignment) {

    }

    public void addAssignments(Assignment[] assignmentsParam, String[] courseNames) {
        if (assignmentsParam.length != courseNames.length) { throw new IllegalArgumentException(); }

        AssignmentDisplay[] assignments = new AssignmentDisplay[assignmentsParam.length];

        for (int i = 0; i < assignmentsParam.length; i++) {
            assignments[i] = new AssignmentDisplay(assignmentsParam[i], courseNames[i]);
        }

        addAssignmentsHelper(assignments);
    }

    public void addAssignments(AssignmentDisplay[] assignments, String[] courseName) {
        addAssignmentsHelper(assignments);
    }

    private void addAssignmentsHelper(AssignmentDisplay[] assignments) {

    }


    public void updateWeekStart(VBox[] courseContainers, boolean weekStart) {
        this.weekStart = weekStart;
        if (weekStart) { //the week displayed is changed to Sunday
            this.weekDisplayed = this.weekDisplayed.minusDays(1);
        } else { //the week displayed is changed to Monday
            this.weekDisplayed = this.weekDisplayed.plusDays(1);
        }

        renderHeaders(courseContainers);
        renderBars(courseContainers);
    }

    public void renderHeaders(VBox[] courseContainers) {
        System.out.println(" course manager - enter render headers");
        for (int i = 0; i < courseContainers.length; i++) {
            try { //instantiate AssignmentCourseHeaderComponent objects and place their controllers into the VBox provided from MainPageController
                FXMLLoader loader = new FXMLLoader(Launcher.class.getResource("AssignmentsCourseHeaderComponent.fxml"));
                Parent temp = loader.load();

                courseHeaderComponents[i] = loader.getController();

                courseContainers[i].getChildren().add(temp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        fillCourseHeaders();
        System.out.println(" course manager - exit render headers");
    }

    public void renderBars(VBox[] courseContainers) {
        System.out.println(" course manager - enter render bars");
        fillAssignmentBars();
        for (int i = 0; i < assignmentBars.length; i++) {
            assignmentBars[i].renderBar(courseContainers, weekStart);
        }
        System.out.println(" course manager - exit render bars");
    }
}
