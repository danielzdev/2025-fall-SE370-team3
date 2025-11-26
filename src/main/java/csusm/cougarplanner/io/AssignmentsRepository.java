package csusm.cougarplanner.io;

import csusm.cougarplanner.models.Assignment;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository class for managing Assignment data persistence in assignments.csv.
 * Implements upsert logic to preserve local difficulty settings during sync.
 */
public class AssignmentsRepository {

    private static final String[] HEADERS = {
        "assignment_id",
        "course_id",
        "assignment_name",
        "due_date",
        "due_time",
        "difficulty",
        "created_at",
    };
    private final CsvReader csvReader;
    private final CsvWriter csvWriter;

    /**
     * Constructs a new AssignmentsRepository with CSV reader/writer.
     */
    public AssignmentsRepository() {
        this.csvReader = new CsvReader();
        this.csvWriter = new CsvWriter();
    }

    /**
     * Reads entire assignments.csv file
     * Each CSV row becomes a Map with keys assignment_id and so forth.
     * Converts each Map to an Assignment object.
     *
     * @return List of all assignments
     * @throws IOException if the CSV file cannot be read
     */
    public List<Assignment> findAll() throws IOException {
        List<Map<String, String>> records = csvReader.readAll(CsvPaths.getAssignmentsPath());
        return records.stream().map(this::mapToAssignment).collect(Collectors.toList());
    }

    /**
     * Finds all assignments due within the specific week.
     *
     * @param weekStart the start date of the week
     * @param weekEnd the end date of the week
     * @return List of assignments due within the specific week
     * @throws IOException if the CSV file cannot be read
     */
    public List<Assignment> findByWeek(LocalDate weekStart, LocalDate weekEnd) throws IOException {
        return findAll()
            .stream()
            .filter(assignment -> isInWeek(assignment, weekStart, weekEnd))
            .collect(Collectors.toList());
    }

    /**
     * Finds all assignments due on the specific day.
     *
     * @param day the specific day to filter assignments by
     * @return List of assignments due on the specific day
     * @throws IOException if the CSV file cannot be read
     */
    public List<Assignment> findByDay(LocalDate day) throws IOException {
        return findAll()
            .stream()
            .filter(assignment -> isOnDay(assignment, day))
            .collect(Collectors.toList());
    }

    /**
     * Updates an existing assignment or inserts a new one.
     *
     * @param assignment the Assignment object to update or insert
     * @throws IOException if the CSV file cannot be written
     */
    public void upsert(Assignment assignment) throws IOException {
        List<Assignment> allAssignments = findAll();

        // Removes existing assignment with same ID
        allAssignments.removeIf(a -> a.getAssignmentId().equals(assignment.getAssignmentId()));

        // Adds updated assignment
        allAssignments.add(assignment);

        // Writes back to file
        List<Map<String, String>> records = allAssignments
            .stream()
            .map(this::assignmentToMap)
            .collect(Collectors.toList());

        csvWriter.writeAll(CsvPaths.getAssignmentsPath(), records, HEADERS);
    }

    /**
     * Performs upsert operation on multiple assignments and preserves difficulty level.
     * This is used during Canvas API sync.
     *
     * @param assignments List of assignments to upsert
     * @throws IOException if the CSV file cannot be written
     */
    public void upsertAll(List<Assignment> assignments) throws IOException {
        Map<String, Assignment> assignmentMap = new HashMap<>();

        // Loads existing assignments
        for (Assignment existing : findAll()) {
            assignmentMap.put(existing.getAssignmentId(), existing);
        }

        // Updates with new assignments
        for (Assignment assignment : assignments) {
            Assignment existing = assignmentMap.get(assignment.getAssignmentId());
            if (existing != null && existing.getDifficulty() != null) {
                assignment.setDifficulty(existing.getDifficulty());
            }
            assignmentMap.put(assignment.getAssignmentId(), assignment);
        }

        // Writes back
        List<Map<String, String>> records = assignmentMap
            .values()
            .stream()
            .map(this::assignmentToMap)
            .collect(Collectors.toList());

        csvWriter.writeAll(CsvPaths.getAssignmentsPath(), records, HEADERS);
    }

    /**
     * Checks if an assignment's due date is within the specific week range.
     *
     * @param assignment the Assignment to check
     * @param weekStart the start date of the week
     * @param weekEnd the end date of the week
     * @return true if the assignment is due within the week range, false otherwise
     */
    private boolean isInWeek(Assignment assignment, LocalDate weekStart, LocalDate weekEnd) {
        if (assignment.getDueDate() == null || assignment.getDueDate().isEmpty()) return false;
        try {
            LocalDate dueDate = LocalDate.parse(assignment.getDueDate());
            return !dueDate.isBefore(weekStart) && !dueDate.isAfter(weekEnd);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if an assignment is due on the specific day.
     *
     * @param assignment the Assignment to check
     * @param day the specific day to check against
     * @return true if the assignment is due on the specified day, false otherwise
     */
    private boolean isOnDay(Assignment assignment, LocalDate day) {
        if (assignment.getDueDate() == null || assignment.getDueDate().isEmpty()) return false;
        try {
            LocalDate dueDate = LocalDate.parse(assignment.getDueDate());
            return dueDate.equals(day);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Converts a CSV record Map to an Assignment object.
     * Handles parsing of difficulty field and empty value conversion.
     *
     * @param record Map representing a CSV row with snake_case keys
     * @return Assignment object populated from the CSV data
     */
    private Assignment mapToAssignment(Map<String, String> record) {
        Assignment assignment = new Assignment();
        assignment.setAssignmentId(record.get("assignment_id"));
        assignment.setCourseId(record.get("course_id"));
        assignment.setAssignmentName(record.get("assignment_name"));
        assignment.setDueDate(record.get("due_date"));
        assignment.setDueTime(record.get("due_time"));
        assignment.setCreatedAt(record.get("created_at"));

        String difficultyStr = record.get("difficulty");
        if (difficultyStr != null && !difficultyStr.isEmpty()) {
            try {
                assignment.setDifficulty(Integer.parseInt(difficultyStr));
            } catch (NumberFormatException e) {
                assignment.setDifficulty(null);
            }
        }
        return assignment;
    }

    /**
     * Converts an Assignment object to a CSV record Map.
     * Handles null difficulty values by converting to empty strings.
     *
     * @param assignment the Assignment object to convert
     * @return Map representing a CSV row with snake_case keys
     */
    private Map<String, String> assignmentToMap(Assignment assignment) {
        Map<String, String> record = new HashMap<>();
        record.put("assignment_id", assignment.getAssignmentId());
        record.put("course_id", assignment.getCourseId());
        record.put("assignment_name", assignment.getAssignmentName());
        record.put("due_date", assignment.getDueDate());
        record.put("due_time", assignment.getDueTime());
        record.put("difficulty", assignment.getDifficulty() != null ? assignment.getDifficulty().toString() : "");
        record.put("created_at", assignment.getCreatedAt() != null ? assignment.getCreatedAt() : "");
        return record;
    }
}
