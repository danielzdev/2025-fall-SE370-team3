package csusm.cougarplanner.FilterPattern;

import csusm.cougarplanner.models.Task;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

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
                        return false;
                    }


                })
                .collect(Collectors.toList());
    }
}