package csusm.cougarplanner.util;

import csusm.cougarplanner.models.Assignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
     * returns a traditional array of assignment objects assembled from assignments
     *      in list format.
     * @param assignments a list of assignments
     * @return returns a traditional array of assignments
     */
    public static Assignment[] convertListToAssignmentArray(List<Assignment> assignments) {
        Assignment[] assignmentArray = new Assignment[assignments.size()];

        for (int i = 0; i < assignments.size(); i++) {
            assignmentArray[i] = assignments.get(i);
        }

        return assignmentArray;
    }
}
