package csusm.cougarplanner.controllers;

import csusm.cougarplanner.FilterPattern.*;
import csusm.cougarplanner.io.CoursesRepository;
import csusm.cougarplanner.models.Course;
import csusm.cougarplanner.models.Task;
import csusm.cougarplanner.services.TaskCache;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Builds and owns the filter bar above the task list (course, priority,
 * status, due-date, and sort dropdowns). On any change it rebuilds an
 * {@link AndFilter} out of whichever dropdowns are not at their "All …"
 * defaults and notifies the host controller via the {@code onFilterChange}
 * callback so it can refresh the visible rows.
 * <p>
 * This class is not a JavaFX FXML controller — it's driven programmatically
 * by {@link TaskPanelController}, which hands it the HBox to populate.
 */
public class FilterBarController {
    private HBox filterBar;
    private ComboBox<String> courseFilter;
    private ComboBox<String> priorityFilter;
    private ComboBox<String> statusFilter;
    private ComboBox<String> dateFilter;
    private Runnable onFilterChange;
    private TaskFilter currentFilter;
    private Map<String, String> courseNameToIdMap;
    private ComboBox<String> sortFilter;
    private String currentSortOption = "Sort: Default";

    public FilterBarController(HBox filterBar, Runnable onFilterChange) {
        this.filterBar = filterBar;
        this.onFilterChange = onFilterChange;
        this.courseNameToIdMap = new HashMap<>();
        setupFilters();
    }

    /**
     * Instantiates every ComboBox, seeds it with its options (including the
     * "All …" default), wires up its change handler, and places the whole
     * row into the HBox provided at construction time.
     */
    private void setupFilters() {
        // load course from CourseRepo

        List<String> courses = loadCourses();
        // Course filter
        courseFilter = new ComboBox<>();
        courseFilter.setItems(FXCollections.observableArrayList("All Courses"));
        courseFilter.getItems().addAll(courses);
        courseFilter.setValue("All Courses");
        courseFilter.getStyleClass().add("filter-combobox");
        courseFilter.setOnAction(e -> applyFilters());

        // Priority filter
        priorityFilter = new ComboBox<>();
        priorityFilter.setItems(FXCollections.observableArrayList(
                "All Priorities", Task.PRIORITY_HIGH, Task.PRIORITY_MEDIUM, Task.PRIORITY_LOW));
        priorityFilter.setValue("All Priorities");
        priorityFilter.getStyleClass().add("filter-combobox");
        priorityFilter.setOnAction(e -> applyFilters());

        // Status filter
        statusFilter = new ComboBox<>();
        statusFilter.setItems(FXCollections.observableArrayList(
                "All Statuses", Task.STATUS_NOT_STARTED, Task.STATUS_IN_PROGRESS, Task.STATUS_COMPLETED));
        statusFilter.setValue("All Statuses");
        statusFilter.getStyleClass().add("filter-combobox");
        statusFilter.setOnAction(e -> applyFilters());

        // Date filter
        dateFilter = new ComboBox<>();
        dateFilter.setItems(FXCollections.observableArrayList(
                "All Dates", "Today", "This Week", "Overdue", "No Due Date"));
        dateFilter.setValue("All Dates");
        dateFilter.getStyleClass().add("filter-combobox");
        dateFilter.setOnAction(e -> applyFilters());

        sortFilter = new ComboBox<>();
        sortFilter.setItems(FXCollections.observableArrayList(
                "Sort: Default",
                "Sort: Due Date (Earliest First)",
                "Sort: Due Date (Latest First)"));
        sortFilter.setValue("Sort: Default");
        sortFilter.getStyleClass().add("filter-combobox");
        sortFilter.setOnAction(e -> applyFilters());


        // Add all filters to the HBox
        filterBar.getChildren().addAll(
                createStyledLabel("Course:"), courseFilter,
                createStyledLabel("Priority:"), priorityFilter,
                createStyledLabel("Status:"), statusFilter,
                createStyledLabel("Due:"), dateFilter,
                createStyledLabel("Sort"),sortFilter
        );
    }
    private List<String> loadCourses() {
        List<String> courseNames = new ArrayList<>();
        try {
            CoursesRepository coursesRepo = new CoursesRepository();
            List<Course> courses = coursesRepo.findAll();
            for (Course course : courses) {
                String courseName = course.getCourseName();
                String courseId = course.getCourseId();

                courseNames.add(course.getCourseName());
                courseNameToIdMap.put(courseName, courseId);
            }
        } catch (IOException e) {
            System.err.println("Could not load courses for filter: " + e.getMessage());
        }
        return courseNames;
    }

    private Label createStyledLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("filter-label");
        return label;
    }

    /**
     * Rebuilds the composite filter from the current dropdown values and
     * fires the change callback so the host can refresh its task list.
     * <p>
     * Each dropdown only contributes to the filter if it's not on its
     * default "All …" value, so the composite shrinks to zero children
     * when the user has cleared everything (in which case
     * {@link #getCurrentFilter()} returns null).
     */
    private void applyFilters() {
        AndFilter andFilter = new AndFilter();

        // Add course filter
        if (!"All Courses".equals(courseFilter.getValue())) {
            String selectedCourseName = courseFilter.getValue();
            String courseId = courseNameToIdMap.get(selectedCourseName);
            if (courseId != null) {
                andFilter.addFilter(new CourseFilter(courseId)); // Pass course ID
            }
        }

        // Add priority filter
        if (!"All Priorities".equals(priorityFilter.getValue())) {
            andFilter.addFilter(new PriorityFilter(priorityFilter.getValue()));
        }

        // Add status filter
        if (!"All Statuses".equals(statusFilter.getValue())) {
            andFilter.addFilter(new StatusFilter(statusFilter.getValue()));
        }

        // Add date filter
        String dateChoice = dateFilter.getValue();
        if (dateChoice != null && !"All Dates".equals(dateChoice)) {
            switch (dateChoice) {
                case "Today":
                    andFilter.addFilter(new DueDateFilter(LocalDate.now(), "eq"));
                    break;
                case "This Week":
                    // getDayOfWeek().getValue() returns 1 (Mon) to 7 (Sun), so this rolls
                    // the target date forward to the upcoming Sunday (end of the current week).
                    LocalDate endOfWeek = LocalDate.now().plusDays(7 - LocalDate.now().getDayOfWeek().getValue());
                    andFilter.addFilter(new DueDateFilter(endOfWeek, "onOrBefore"));
                    break;
                case "Overdue":
                    andFilter.addFilter(new DueDateFilter(LocalDate.now(), "before"));
                    break;
                case "No Due Date":
                    andFilter.addFilter(tasks -> tasks.stream()
                            .filter(t -> t.getDueDate() == null || t.getDueDate().isBlank())
                            .collect(Collectors.toList()));
                    break;
            }
        }

        currentSortOption = sortFilter.getValue();

        // Store current filter (null if no active filters)
        currentFilter = andFilter.getFilters().isEmpty() ? null : andFilter;

        // Notify that filter changed
        if (onFilterChange != null) {
            onFilterChange.run();
        }
    }

    public TaskFilter getCurrentFilter() {
        return currentFilter;
    }

    public String getCurrentSortOption() {
        return currentSortOption;
    }

    public void resetFilters() {
        courseFilter.setValue("All Courses");
        priorityFilter.setValue("All Priorities");
        statusFilter.setValue("All Statuses");
        dateFilter.setValue("All Dates");
        sortFilter.setValue("Sort: Default");
        applyFilters();
    }
}