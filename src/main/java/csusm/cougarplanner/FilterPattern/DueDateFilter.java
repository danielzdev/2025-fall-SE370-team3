package csusm.cougarplanner.FilterPattern;

import csusm.cougarplanner.models.Task;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

/**
 * Filters tasks by their due date relative to a target date.
 * The comparison is controlled by the operator string passed in the
 * constructor — see the list of accepted values below.
 *
 * Tasks with a missing or unparseable due date are always dropped, regardless
 * of the operator. That way the rest of the UI never has to deal with junk
 * dates leaking through.
 */
public class DueDateFilter implements TaskFilter {
    private LocalDate targetDate;
    private String operator;

    // "eq", "before", "after", "onOrBefore", "onOrAfter"

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyy");
    public DueDateFilter(LocalDate targetDate, String operator) {
        this.targetDate = targetDate;
        this.operator = operator;
    }

    @Override
    public List<Task> filter(List<Task> tasks) {
        return tasks.stream()
                .filter(task -> {
                    // Skip tasks that don't have a due date set at all
                    if (task.getDueDate() == null || task.getDueDate().isBlank())
                        return false;
                    try{
                        LocalDate dueDate = LocalDate.parse(task.getDueDate(), DATE_FORMATTER);
                        switch (operator) {

                            case "eq": return dueDate.equals(targetDate);
                            case "before": return dueDate.isBefore(targetDate);
                            case "after": return dueDate.isAfter(targetDate);
                            case "onOrBefore": return !dueDate.isAfter(targetDate);
                            case "onOrAfter": return !dueDate.isBefore(targetDate);
                            default: return false;
                        }
                    } catch (Exception e) {
                        // Bad date strings get filtered out rather than crashing the view
                        return false;
                    }


                })
                .collect(Collectors.toList());
    }
}
