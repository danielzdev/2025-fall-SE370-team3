package csusm.cougarplanner.controllers;

import csusm.cougarplanner.API;
import csusm.cougarplanner.Launcher;
import csusm.cougarplanner.config.Profile;
import csusm.cougarplanner.config.ProfileReader;
import csusm.cougarplanner.io.AnnouncementsRepository;
import csusm.cougarplanner.io.AssignmentsRepository;
import csusm.cougarplanner.io.CoursesRepository;
import csusm.cougarplanner.models.*;
import csusm.cougarplanner.services.CanvasService;
import csusm.cougarplanner.transitions.ExponentialTransitionScale;
import csusm.cougarplanner.transitions.ExponentialTransitionTranslation;
import csusm.cougarplanner.util.DateTimeUtil;
import csusm.cougarplanner.util.WeekRange;
import csusm.cougarplanner.util.WeekUtil;
import java.io.File;
import java.io.IOException;
import java.io.IOException;
import java.net.URL;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDate;
import java.time.Month;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.format.TextStyle;
import java.util.*;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class MainPageController implements Initializable {

    ProfileReader reader = new ProfileReader(Path.of("data/profile.properties"));
    Profile profile = reader.readProfile().getProfile();

    private Node currentlySelectedObject = null;

    private void selectNewObject(Node node) {
        if (currentlySelectedObject != null) {
            currentlySelectedObject.setStyle(null);
        }
        currentlySelectedObject = node;
    }

    @FXML
    private AnchorPane viewingMenu;

    @FXML
    private Pane viewingHitbox;

    @FXML
    private AnchorPane plannerBody;

    boolean viewingMenuIsOpen = false;

    @FXML
    private void toggleViewingMenu(MouseEvent event) {
        viewingMenu.setVisible(!viewingMenuIsOpen);
        viewingHitbox.setVisible(!viewingMenuIsOpen);

        viewingMenuIsOpen = !viewingMenuIsOpen;

        plannerBody.setEffect(viewingMenuIsOpen ? new BoxBlur() : null);
        viewingMenu.setOpacity(1.0);

        selectNewObject(viewingHitbox);
    }

    @FXML
    private void fuzzPlannerBody(MouseEvent event) {
        if (viewingMenuIsOpen) {
            plannerBody.setEffect((event.getEventType() == MouseEvent.MOUSE_ENTERED) ? null : new BoxBlur());
            viewingMenu.setOpacity((event.getEventType() == MouseEvent.MOUSE_ENTERED) ? 0.25 : 1.0);
        } else {
            plannerBody.setEffect(null);
            viewingMenu.setOpacity(1.0);
        }
    }

    @FXML
    private void highlightFromText(MouseEvent event) {
        if (event.getSource() instanceof Label label) {
            label.setStyle(
                "-fx-text-fill: " + ((event.getEventType() == MouseEvent.MOUSE_ENTERED) ? "#ffe777" : "#ffffff")
            );
        }
    }

    @FXML
    private Label viewingMenuLabel, viewingMenuLabelMutable;

    @FXML
    private Rectangle announcementsRectangle, assignmentsRectangle;

    boolean showAnnouncements = true; //false - show assignments

    @FXML
    private void toggleContentsType(MouseEvent event) {
        if (event.getSource() instanceof Label label) {
            boolean userClickedAnnouncements = label.getText().equals("Announcements");

            if (showAnnouncements != userClickedAnnouncements) {
                viewingMenuLabelMutable.setText(userClickedAnnouncements ? "Announcements" : "Assignments");
                announcementsRectangle.setVisible(userClickedAnnouncements);
                assignmentsRectangle.setVisible(!userClickedAnnouncements);

                showAnnouncements = userClickedAnnouncements;

                dateDisplayed = LocalDate.now(); // reset to current week
                WeekRange currentWeek = getWeekRange(dateDisplayed);

                if (showAnnouncements) {
                    clearAssignmentDisplay();
                    populateAnnouncements(currentWeek);
                } else {
                    clearAnnouncementDisplay();
                    dateDisplayed = LocalDate.now().plusWeeks(1);
                    currentWeek = getWeekRange(dateDisplayed);
                    // Ensure weekStart and weekDisplayed are set correctly
                    weekStart = true;               // or false depending on your week logic
                    weekDisplayed = currentWeek.startIncl(); // mimic navigateWeek behavior

                    populateCoursesAndAssignments(currentWeek);
                }
            }
        }
    }

    private void clearAnnouncementDisplay() {
        //announcementContainer.clear(); // replace
    }

    private void clearAssignmentDisplay() {
        for (VBox vbox : courseContainers) vbox.getChildren().clear();
    }

    @FXML
    private Pane viewingButtonDecoration1, viewingButtonDecoration2, viewingButtonDecoration3, viewingButtonDecoration4, viewingButtonDecoration5;

    private Pane[] viewingButtonDecorations;
    private final Double[] viewingButtonDecorationInitLocations = new Double[5];

    @FXML
    private void highlightViewingLabelFromPane(MouseEvent event) {
        if (event.getSource() instanceof Pane pane) {
            ExponentialTransitionTranslation[] transition = new ExponentialTransitionTranslation[5];

            if (event.getEventType() == MouseEvent.MOUSE_ENTERED) {
                viewingMenuLabel.setStyle("-fx-text-fill: #ffffff");
                viewingMenuLabelMutable.setStyle("-fx-text-fill: #ffffff");

                for (int i = 0; i < viewingButtonDecorations.length; i++) {
                    transition[i] = new ExponentialTransitionTranslation(
                        viewingButtonDecorations[i],
                        true,
                        viewingButtonDecorations[i].getTranslateX(),
                        viewingButtonDecorationInitLocations[i] + 5 * (i + 1),
                        Duration.millis(500)
                    );
                }

                for (int i = 0; i < viewingButtonDecorations.length; i++) {
                    transition[i].play();
                }
            } else {
                viewingMenuLabel.setStyle("-fx-text-fill: #D5D5D5");
                viewingMenuLabelMutable.setStyle("-fx-text-fill: #D5D5D5");

                for (int i = 0; i < viewingButtonDecorations.length; i++) {
                    transition[i] = new ExponentialTransitionTranslation(
                        viewingButtonDecorations[i],
                        true,
                        viewingButtonDecorations[i].getTranslateX(),
                        viewingButtonDecorationInitLocations[i],
                        Duration.millis(400)
                    );
                }

                for (int i = 0; i < viewingButtonDecorations.length; i++) {
                    transition[i].play();
                }
            }
        }
    }

    @FXML
    private Label dateLabel;

    private LocalDate dateMemory; //stores the previous date visited
    private LocalDate dateDisplayed; //stores the current date viewing
    private LocalDate weekDisplayed; //stores the first day of the week displayed
    private final LocalDate currentDate = LocalDate.now();

    private void updateDate(String action, Optional<MouseEvent> day) {
        MouseEvent mouseEvent = day.orElse(null);
        String weekStartSetting = (weekStart) ? "sunday" : "monday";
        dateMemory = dateDisplayed;

        switch (action) {
            case "today":
                dateDisplayed = LocalDate.now();
                weekDisplayed = WeekUtil.getWeekStart(dateDisplayed, weekStartSetting);
                break;
            case "previousWeek":
                dateDisplayed = weekDisplayed = WeekUtil.getNavigationWeekBounds(
                    weekDisplayed,
                    weekStartSetting,
                    "previous"
                )[0]; //display the first day of the next week
                break;
            case "previousDay":
                dateDisplayed = dateDisplayed.minusDays(1);
                //if the new date is outside the bounds of the unchanged week
                if (!WeekUtil.isDateInWeek(dateDisplayed, weekDisplayed, weekStartSetting)) {
                    weekDisplayed = weekDisplayed.minusWeeks(1);
                }
                break;
            case "nextWeek":
                dateDisplayed = weekDisplayed = WeekUtil.getNavigationWeekBounds(
                    weekDisplayed,
                    weekStartSetting,
                    "next"
                )[0]; //display the first day of the next week
                break;
            case "nextDay":
                dateDisplayed = dateDisplayed.plusDays(1);
                //if the new date is outside the bounds of the unchanged week
                if (!WeekUtil.isDateInWeek(dateDisplayed, weekDisplayed, weekStartSetting)) {
                    weekDisplayed = weekDisplayed.plusWeeks(1);
                }
                break;
            case "changeWeekStart":
                if (weekStart) {
                    //if the start of the week is changed to sunday
                    weekDisplayed = weekDisplayed.minusDays(1);

                    //if the date that the user last had selected is now outside the bounds of the changed week
                    if (!WeekUtil.isDateInWeek(dateDisplayed, weekDisplayed, weekStartSetting)) {
                        //the date that the user previously had selected was sunday of the given week (on the very right) which was removed from the planner page (which shifted left)
                        dateDisplayed = dateDisplayed.minusDays(1); //change the date to sunday of that week
                    } //otherwise don't change the date displayed
                } else {
                    //if the start of the week is changed to monday
                    weekDisplayed = weekDisplayed.plusDays(1);

                    //if the date that the user last had selected is now outside the bounds of the changed week
                    if (!WeekUtil.isDateInWeek(dateDisplayed, weekDisplayed, weekStartSetting)) {
                        //the date that the user previously had selected was sunday  of the given week (on the very left) which was removed from the planner page (which shifted right)
                        dateDisplayed = dateDisplayed.plusDays(1); //change the date to monday of that week
                    } //otherwise don't change the date displayed
                }
                break;
            case "clickInput":
                assert mouseEvent != null;

                if (mouseEvent.getSource() instanceof Pane pane) {
                    int dayOfWeek = Integer.parseInt((String) pane.getUserData()) - 1;

                    dateDisplayed = weekDisplayed.plusDays(dayOfWeek /*- getCorrectionTerm(dayOfWeek)*/);
                    //the user can not change the week displayed by clicking on a header, so the week displayed stays the same.
                }

                break;
        }

        fillDate();
    }

    @FXML
    private Pane displayDateParent;

    private double displayDateParentPaneCenter;

    //formats dateDisplayed into preferred appearance and inserts it into dateLabel
    private void fillDate() {
        String[] components = DateTimeUtil.formatDate(dateDisplayed).split("-");

        components[1] = Month.of(Integer.parseInt(components[1])).getDisplayName(TextStyle.SHORT, Locale.ENGLISH); //month string, e.g. Jan, Feb, etc...
        components[2] = addNumberSuffix(components[2]); //day of month string, e.g. 1st, 2nd, 3rd, etc...

        //sets the text in the date label to the date selected with the format: Mon Nov 10th
        dateLabel.setText(
            dateDisplayed.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) +
                ", " +
                components[1] +
                " " +
                components[2]
        );

        dateLabel
            .widthProperty()
            .addListener((observable, oldValue, newValue) -> {
                dateLabel.setLayoutX(displayDateParentPaneCenter - (dateLabel.getWidth() / 2));
            });
    }

    //adds the number suffix onto the end of the day. E.g. 1st, 2nd, 3rd, etc...
    private String addNumberSuffix(String i) {
        String suffix;
        int integer = Integer.parseInt(i);

        int lastTwoDigits = integer % 100;
        int lastDigit = integer % 10;

        if (lastTwoDigits >= 11 && lastTwoDigits <= 13) {
            suffix = "th";
        } else {
            suffix = switch (lastDigit) {
                case 1 -> "st";
                case 2 -> "nd";
                case 3 -> "rd";
                default -> "th";
            };
        }

        return integer + suffix;
    }

    @FXML
    private Rectangle weekRectangle, dayRectangle;

    @FXML
    private AnchorPane weekPlanner, dayPlanner;

    boolean defaultView = profile.getDefaultView().equals("week"); //true - week ; false - day

    @FXML
    private void toggleViewByWeek(MouseEvent event) {
        if (event.getSource() instanceof Label label) {
            boolean userClickedViewByWeek = label.getText().equals("Week");

            weekDayViewed = dateDisplayed.getDayOfWeek().getValue();
            weekDayViewed += (weekDayViewed == 7) ? -7 : 0;

            if (defaultView != userClickedViewByWeek) {
                performToggleViewByWeek(userClickedViewByWeek);
            }
        }
    }

    @FXML
    private void toggleViewByWeek() {
        boolean userClickedViewByWeek = !defaultView;

        performToggleViewByWeek(userClickedViewByWeek);
    }

    /**
     * This program will perform the operations necessary to toggle between the week and
     *      the day view menus in the assignments tab of the planner.
     *
     *      !!! When switching from week view to day view, the part of the program that
     *      calls this function needs to update the weekDayViewed variable beforehand.
     *      This function only calls changeDayViewed(weekDayViewed).!!!
     *
     *This is because when this function is called from a header double click, this
     *      function does not have the mouse event needed to identify the day clicked on.
     * @param userClickedViewByWeek true if the user changed the planner to week view
     *                              instead of day view.
     */
    private void performToggleViewByWeek(boolean userClickedViewByWeek) {
        weekRectangle.setVisible(userClickedViewByWeek);
        dayRectangle.setVisible(!userClickedViewByWeek);

        weekPlanner.setVisible(userClickedViewByWeek);
        dayPlanner.setVisible(!userClickedViewByWeek);

        defaultView = userClickedViewByWeek;
        profile.setDefaultView(defaultView ? "week" : "day");

        organizePlannerByWeekStart(); //the first day of the week isn't changed on the day-week view that's hidden, so it needs to be updated
        if (!defaultView) {
            //the user changed the planner to see the day view
            changeDayViewed(weekDayViewed); //planner defaults to viewing the first day; change this to the day selected. weekDayViewed is updated before toggleViewByWeek is called
        }
    }

    @FXML
    private HBox hboxWeekContentsContainer;

    @FXML
    private AnchorPane sundayContentsPane, mondayContentsPane, tuesdayContentsPane, wednesdayContentsPane, thursdayContentsPane, fridayContentsPane, saturdayContentsPane;

    @FXML
    private VBox sundayContentsVBox, mondayContentsVBox, tuesdayContentsVBox, wednesdayContentsVBox, thursdayContentsVBox, fridayContentsVBox, saturdayContentsVBox;

    private VBox[] courseContainers;

    private VBox[] announcementContainer;

    @FXML
    private AnchorPane sundayDayHeaderPane, mondayDayHeaderPane, tuesdayDayHeaderPane, wednesdayDayHeaderPane, thursdayDayHeaderPane, fridayDayHeaderPane, saturdayDayHeaderPane;

    @FXML
    private Label weekSundayHeaderLabel, weekMondayHeaderLabel, weekTuesdayHeaderLabel, weekWednesdayHeaderLabel, weekThursdayHeaderLabel, weekFridayHeaderLabel, weekSaturdayHeaderLabel;

    @FXML
    private Label daySundayHeaderLabel, dayMondayHeaderLabel, dayTuesdayHeaderLabel, dayWednesdayHeaderLabel, dayThursdayHeaderLabel, dayFridayHeaderLabel, daySaturdayHeaderLabel;

    private Label[] weekHeaderLabels;
    private Label[] dayHeaderLabels;

    //these variables track the first day of the week in the week view and day view (the week beginning isn't updated on the part of the planner that isn't being viewed)
    private boolean firstDayOfWeekWeekTracker = true; //true - sunday, false - monday
    private boolean firstDayOfWeekDayTracker = true; //true - sunday, false - monday

    private final String[] weekOrganizedStartingSunday = {
        "SUNDAY",
        "MONDAY",
        "TUESDAY",
        "WEDNESDAY",
        "THURSDAY",
        "FRIDAY",
        "SATURDAY",
    };
    private final String[] weekOrganizedStartingMonday = {
        "MONDAY",
        "TUESDAY",
        "WEDNESDAY",
        "THURSDAY",
        "FRIDAY",
        "SATURDAY",
        "SUNDAY",
    };

    private void organizePlannerByWeekStart() {
        if (defaultView) {
            //if the user is currently seeing planner week view
            if (weekStart != firstDayOfWeekWeekTracker) {
                performWeekOrganization(weekHeaderLabels);
            }
        } else {
            //if the user is currently seeing the planner day view
            if (weekStart != firstDayOfWeekDayTracker) {
                performWeekOrganization(dayHeaderLabels);
            }
        }
    }

    private void performWeekOrganization(Label[] arrayOfLabels) {
        if (weekStart) {
            //change the week beginning to sunday
            for (int i = 0; i < arrayOfLabels.length; i++) {
                arrayOfLabels[i].setText(weekOrganizedStartingSunday[i]);
            }

            if (defaultView) {
                firstDayOfWeekWeekTracker = true;
            } else {
                firstDayOfWeekDayTracker = true;
            }
        } else {
            //change the week beginning to monday
            for (int i = 0; i < arrayOfLabels.length; i++) {
                arrayOfLabels[i].setText(weekOrganizedStartingMonday[i]);
            }

            if (defaultView) {
                firstDayOfWeekWeekTracker = false;
            } else {
                firstDayOfWeekDayTracker = false;
            }
        }
    }

    @FXML
    private Pane headerPaneDecoration1, headerPaneDecoration2, headerPaneDecoration3, headerPaneDecoration4, headerPaneDecoration5, headerPaneDecoration6, headerPaneDecoration7;

    private Pane[] headerPaneDecorations;

    @FXML
    private void weekViewHeaderAnimation(MouseEvent event) {
        if (event.getSource() instanceof Pane pane) {
            int index = Integer.parseInt((String) pane.getUserData()) - 1; //get the index of the day of the week the user clicked on
            boolean mouseEntered = event.getEventType() == MouseEvent.MOUSE_ENTERED; //true if the mouse entered the header hitbox, false if it exited

            ExponentialTransitionScale transition;
            //an exponential transition that scales the size of the header decoration. The details of the transition change based on mouse behavior.
            transition = new ExponentialTransitionScale(
                headerPaneDecorations[index],
                headerPaneDecorations[index].getScaleX(),
                (mouseEntered) ? 2 : 0,
                Duration.millis(200.0)
            );
            transition.play();
        }
    }

    //a pause transition that defines the amount of time needed to distinguish between a single click and a double click
    PauseTransition singleClick = new PauseTransition(Duration.millis(100.0));

    @FXML
    private void selectHeaderFromPane(MouseEvent event) {
        //if the event was called from a Pane, assign it to the object titled pane
        if (event.getSource() instanceof Pane pane) {
            //if the transition is allowed to complete (not stopped by a double click) then it will execute this code:
            singleClick.setOnFinished(e -> {
                //establish the event behavior on a single click
                if (pane != currentlySelectedObject) {
                    //if the user clicked the already selected object, do nothing.
                    selectNewObject(pane);
                    pane.setStyle("-fx-background-color: #BDBDBD");
                    updateDate("clickInput", Optional.of(event)); //update the date based on what was clicked
                }
            });

            if (event.getClickCount() == 1) {
                singleClick.play(); //begin to wait for a second click
            }

            //user double-clicked and wishes to open up the day selected in the 'day view' menu
            if (event.getClickCount() == 2) {
                singleClick.stop(); //upon a second click, stop the countdown for a single click
                weekDayViewed = Integer.parseInt((String) pane.getUserData()) - 1; //get the index for the week day currently displayed
                weekDayViewed += (weekDayViewed == 7) ? -7 : 0;
                toggleViewByWeek(); //change the planner view
            }
        }
    }

    @FXML
    private Rectangle sundayRectangle, mondayRectangle;

    boolean weekStart = profile.getWeekStart().equalsIgnoreCase("sunday"); //true - sunday ; false - monday

    @FXML
    private void toggleWeekStart(MouseEvent event) {
        if (event.getSource() instanceof Label label) {
            boolean userClickedSunday = label.getText().equals("Sunday");

            if (weekStart != userClickedSunday) {
                //if the user clicks the other unselected option
                sundayRectangle.setVisible(userClickedSunday);
                mondayRectangle.setVisible(!userClickedSunday);

                weekStart = userClickedSunday;
                profile.setWeekStart(weekStart ? "sunday" : "monday");

                organizePlannerByWeekStart();
                updateDate("changeWeekStart", Optional.empty());
                if (!defaultView) {
                    //if the user is currently seeing the day view
                    weekDayViewed = dateDisplayed.getDayOfWeek().getValue();
                    if (weekStart) {
                        //if the user changed the start of the week to be sunday
                        weekDayViewed += (weekDayViewed == 7) ? -7 : 0;
                    } else {
                        weekDayViewed--;
                    }
                    changeDayViewed(weekDayViewed);
                }
            }
        }
    }

    private AnchorPane[] listOfDayHeaders;

    private int weekDayViewed = 0; //0 - sunday, 6 - saturday

    @FXML
    private void viewPreviousBlock(MouseEvent event) {
        selectNewObject(viewingHitbox); //filler object to allow new selection
        updateDate("previousWeek", Optional.empty());
        navigateWeek(-1); // go to previous week
    }

    @FXML
    private void viewNextBlock(MouseEvent event) {
        selectNewObject(viewingHitbox); //filler object to allow new selection
        updateDate("nextWeek", Optional.empty());
        navigateWeek(1); // go to next week
    }

    private void changeWeekViewed(String dateViewed) {
        return;
    }

    private void changeDayViewed(int dayViewed) {
        for (AnchorPane listOfDayHeader : listOfDayHeaders) {
            listOfDayHeader.setVisible(false);
        }
        listOfDayHeaders[dayViewed].setVisible(true);
    }

    @FXML
    private VBox root;

    private double offsetX = 0;
    private double offsetY = 0;

    @FXML
    private void recordWindowPosition(MouseEvent event) {
        offsetX = event.getSceneX();
        offsetY = event.getSceneY();
    }

    @FXML
    private void updateWindowPosition(MouseEvent event) {
        Launcher.getPrimaryStage().setX(event.getScreenX() - offsetX);
        Launcher.getPrimaryStage().setY(event.getScreenY() - offsetY);
    }

    @FXML
    private void minimizeWindow(MouseEvent event) {
        Launcher.getPrimaryStage().setIconified(true);
    }

    @FXML
    private void closeApplication(MouseEvent event) {
        Platform.exit();
    }

    private CanvasService canvasService;
    private final CoursesRepository coursesRepository = new CoursesRepository();
    private final AssignmentsRepository assignmentsRepository = new AssignmentsRepository();
    private final AnnouncementsRepository announcementsRepository = new AnnouncementsRepository();

    private void populateCoursesAndAssignments(WeekRange week) {
        // Get courses from cache (CanvasService handles fetch on first call only)
        List<Course> courses = canvasService.fetchCourses();

        List<Assignment> assignments = new ArrayList<>();
        boolean apiSuccess = false;

        // Attempt API fetch for assignments only
        try {
            assignments = canvasService.fetchAssignments(week);

            if (!assignments.isEmpty()) {
                apiSuccess = true;

                // Save assignments to local CSV
                try {
                    assignmentsRepository.upsertAll(assignments);
                } catch (IOException e) {
                    System.err.println("Error saving assignments to local files: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            apiSuccess = false;
        }

        if (!apiSuccess) {
            // Fallback to local CSV if API failed
            try {
                assignments = assignmentsRepository.findByWeek(week.startIncl(), week.endExcl());
            } catch (IOException e) {
                assignments = new ArrayList<>();
                System.err.println("Error reading local assignment data: " + e.getMessage());
            }
        }

        for (Course course : courses) {
            List<Assignment> courseAssignments = assignments.stream()
                    .filter(a -> a.getCourseId().equals(course.getCourseId()))
                    .toList();

            if (!courseAssignments.isEmpty()) {
                AssignmentDisplay[] displayAssignments = courseAssignments.stream()
                        .map(a -> new AssignmentDisplay(a, course.getCourseName()))
                        .toArray(AssignmentDisplay[]::new);

                CourseManager manager = new CourseManager(displayAssignments, weekStart, weekDisplayed);
                manager.renderHeaders(courseContainers);
                manager.renderBars(courseContainers);
            }
        }
    }

    private void populateAnnouncements(WeekRange week) {
        List<Announcement> announcements = new ArrayList<>();
        boolean apiSuccess = false;

        // Try API first
        try {
            announcements = canvasService.fetchAnnouncements(week);

            if (!announcements.isEmpty()) {
                apiSuccess = true;

                // Save announcements to CSV
                try {
                    announcementsRepository.upsertAll(announcements);
                } catch (IOException e) {
                    System.err.println("Error saving announcements to CSV: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            apiSuccess = false;
        }

        // Fallback to CSV if API fails or returns empty
        if (!apiSuccess) {
            try {
                announcements = announcementsRepository.findByWeek(week.startIncl(), week.endExcl());
            } catch (IOException e) {
                System.err.println("Error reading announcements from CSV: " + e.getMessage());
            }
        }

        // Print out announcements instead of GUI
        System.out.println("\n=== ANNOUNCEMENTS FOR WEEK ===");
        System.out.println(week.startIncl() + " → " + week.endExcl().minusDays(1));
        System.out.println("-----------------------------------------");

        if (announcements.isEmpty()) {
            System.out.println("No announcements for this week.");
            return;
        }

        for (Announcement ann : announcements) {
            System.out.println("• " + ann.getTitle());
            System.out.println("  Posted: " + ann.getPostedAt());
            System.out.println("  Course ID: " + ann.getCourseId());
            System.out.println("  Message: " + (ann.getMessage() != null ? ann.getMessage() : "(empty)"));
            System.out.println();
        }
    }

    private WeekRange getWeekRange(LocalDate date) {
        LocalDate weekStart = date.minusDays(date.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(7);
        return new WeekRange(weekStart, weekEnd);
    }

    private void navigateWeek(int offsetWeeks) {
        // Clear previous content
        for (VBox vbox : courseContainers) vbox.getChildren().clear();

        if (offsetWeeks == 1) {
            dateDisplayed = dateDisplayed.plusWeeks(offsetWeeks);
        } else {
            dateDisplayed = dateDisplayed.minusWeeks(offsetWeeks);
        }
        WeekRange newWeek = getWeekRange(dateDisplayed);
        if (showAnnouncements) {
            populateAnnouncements(newWeek);
        } else {
            populateCoursesAndAssignments(newWeek);
        }
    }

    private String getAuthToken() {
        try (Scanner sc = new Scanner(new File("data/profile.properties"))) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.startsWith("authToken=")) {
                    return line.substring("authToken=".length()).trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        listOfDayHeaders = new AnchorPane[] {
            sundayDayHeaderPane,
            mondayDayHeaderPane,
            tuesdayDayHeaderPane,
            wednesdayDayHeaderPane,
            thursdayDayHeaderPane,
            fridayDayHeaderPane,
            saturdayDayHeaderPane,
        };

        weekHeaderLabels = new Label[] {
            weekSundayHeaderLabel,
            weekMondayHeaderLabel,
            weekTuesdayHeaderLabel,
            weekWednesdayHeaderLabel,
            weekThursdayHeaderLabel,
            weekFridayHeaderLabel,
            weekSaturdayHeaderLabel,
        };

        dayHeaderLabels = new Label[] {
            daySundayHeaderLabel,
            dayMondayHeaderLabel,
            dayTuesdayHeaderLabel,
            dayWednesdayHeaderLabel,
            dayThursdayHeaderLabel,
            dayFridayHeaderLabel,
            daySaturdayHeaderLabel,
        };

        viewingButtonDecorations = new Pane[] {
            viewingButtonDecoration1,
            viewingButtonDecoration2,
            viewingButtonDecoration3,
            viewingButtonDecoration4,
            viewingButtonDecoration5,
        };

        headerPaneDecorations = new Pane[] {
            headerPaneDecoration1,
            headerPaneDecoration2,
            headerPaneDecoration3,
            headerPaneDecoration4,
            headerPaneDecoration5,
            headerPaneDecoration6,
            headerPaneDecoration7,
        };

        courseContainers = new VBox[] {
            sundayContentsVBox,
            mondayContentsVBox,
            tuesdayContentsVBox,
            wednesdayContentsVBox,
            thursdayContentsVBox,
            fridayContentsVBox,
            saturdayContentsVBox,
        };

        String token = getAuthToken(); // implement this to read from profile.properties or config
        API api = new API(token);

        // Initialize canvasService with the API instance
        canvasService = new CanvasService(api);

        for (int i = 0; i < viewingButtonDecorations.length; i++) {
            viewingButtonDecorationInitLocations[i] = viewingButtonDecorations[i].getTranslateX();
        }

        viewingMenu.setVisible(false);
        viewingHitbox.setVisible(false);

        weekRectangle.setVisible(defaultView);
        dayRectangle.setVisible(!defaultView);

        sundayRectangle.setVisible(weekStart);
        mondayRectangle.setVisible(!weekStart);

        Platform.runLater(() -> {
            dateDisplayed = (dateDisplayed == null) ? LocalDate.now() : dateDisplayed;
            dateMemory = dateDisplayed;
            displayDateParentPaneCenter = displayDateParent.getWidth() / 2;

            updateDate("today", Optional.empty());

            // Populate GUI with Canvas data
            WeekRange currentWeek = getWeekRange(dateDisplayed);
            populateAnnouncements(currentWeek);
        });
    }
}
