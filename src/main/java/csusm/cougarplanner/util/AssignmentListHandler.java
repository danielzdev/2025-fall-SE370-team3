package csusm.cougarplanner.util;

import csusm.cougarplanner.models.Assignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.IntFunction;

public class AssignmentListHandler {
    /**
     * returns a 2d list of assignments separated by course (each row contains a different course)
     * @param assignments list of assignments taken from the api
     * @return returns a 2d list of assignments
     */
    public static List<List<Assignment>> separateAssignmentsByCourse(List<Assignment> assignments) {
        List<List<Assignment>> assignmentsByCourse = new ArrayList<>();
        HashMap<String, Integer> courseIdAndIndexRow = new HashMap<>();

        for (int i = 0; i < assignments.size(); i++) {
            String courseId = assignments.get(i).getCourseId();

            if (!courseIdAndIndexRow.containsKey(courseId)) {
                courseIdAndIndexRow.put(courseId, courseIdAndIndexRow.size());
            }

            int index = courseIdAndIndexRow.get(courseId);

            while (assignmentsByCourse.size() <= index) {
                assignmentsByCourse.add(new ArrayList<>());
            }

            assignmentsByCourse.get(index).add(assignments.get(i));
        }

        return assignmentsByCourse;
    }

    /**
     * <pre>
     * converts a list object to a traditional array
     * @param list the list being converted to an array
     * @param generator stores lambda expression (int) -> new T[int]; which conveys
     *                  the future array at the necessary size to list's
     *                  toArray(T[] a) method.
     *                  The shorthand for this lambda should be used:
     *                              T[]::new
     *                  But replace T with the type of object stored by the list
     * @return returns the traditional array variation of your list
     * @param <T>
     * </pre>
     */
    public static <T> T[] toArray(List<T> list, IntFunction<T[]> generator) {
        return list.toArray(generator);
    }
}
