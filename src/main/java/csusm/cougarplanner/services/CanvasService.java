package csusm.cougarplanner.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import csusm.cougarplanner.API;
import csusm.cougarplanner.models.Announcement;
import csusm.cougarplanner.models.Assignment;
import csusm.cougarplanner.models.Course;
import csusm.cougarplanner.util.CourseCodeUtil;
import csusm.cougarplanner.util.DateTimeUtil;
import csusm.cougarplanner.util.WeekRange;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class CanvasService {

    private final API api;
    private final Gson gson;

    public CanvasService(API api) {
        this.api = api;
        this.gson = new Gson();
    }

    /**
     * Fetches all active courses from Canvas API.
     * Parses JSON response and converts to Course objects.
     * Returns empty list on API errors or parsing failures.
     */
    public List<Course> fetchCourses() {
        String json = api.getCoursesJson();
        if (json == null) {
            return Collections.emptyList();
        }

        try {
            Type listType = new TypeToken<List<CourseDto>>() {}.getType();
            List<CourseDto> dtos = gson.fromJson(json, listType);

            if (dtos == null) {
                return Collections.emptyList();
            }

            List<Course> courses = new ArrayList<>();
            for (CourseDto dto : dtos) {
                if (dto.id != null) {
                    courses.add(mapToCourse(dto));
                }
            }
            return courses;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Fetches assignments from Canvas API and filters by week range.
     * Only includes assignments with due dates within the specified range.
     * Returns empty list on API errors or parsing failures.
     */
    public List<Assignment> fetchAssignments(WeekRange range) {
        // First get all courses
        List<Course> courses = fetchCourses();
        if (courses.isEmpty()) {
            return Collections.emptyList();
        }

        List<Assignment> allAssignments = new ArrayList<>();

        // Get assignments from each course
        for (Course course : courses) {
            try {
                int courseId = Integer.parseInt(course.getCourseId());
                String json = api.getAssignmentsJson(courseId);
                if (json == null) {
                    continue; // Skip this course if API call failed
                }

                Type listType = new TypeToken<List<AssignmentDto>>() {}.getType();
                List<AssignmentDto> dtos = gson.fromJson(json, listType);

                if (dtos == null) {
                    continue; // Skip this course if parsing failed
                }

                for (AssignmentDto dto : dtos) {
                    if (dto.id != null && dto.course_id != null) {
                        Optional<LocalDate> dueDate = DateTimeUtil.parseDateFromDateTime(dto.due_at);
                        if (dueDate.isPresent() && isDateInRange(dueDate.get(), range)) {
                            allAssignments.add(mapToAssignment(dto));
                        }
                    }
                }
            } catch (Exception e) {
                // Skip this course if any error occurs
                continue;
            }
        }

        return allAssignments;
    }

    /**
     * Fetches announcements from Canvas API and filters by week range.
     * Only includes announcements with posted dates within the specified range.
     * Returns empty list on API errors or parsing failures.
     */
    public List<Announcement> fetchAnnouncements(WeekRange range) {
        String json = api.getAnnouncementsJson();
        if (json == null) {
            return Collections.emptyList();
        }

        try {
            Type listType = new TypeToken<List<AnnouncementDto>>() {}.getType();
            List<AnnouncementDto> dtos = gson.fromJson(json, listType);

            if (dtos == null) {
                return Collections.emptyList();
            }

            List<Announcement> announcements = new ArrayList<>();
            for (AnnouncementDto dto : dtos) {
                if (dto.id != null && dto.course_id != null) {
                    Optional<LocalDate> postedDate = DateTimeUtil.parseDateFromDateTime(dto.posted_at);
                    if (postedDate.isPresent() && isDateInRange(postedDate.get(), range)) {
                        announcements.add(mapToAnnouncement(dto));
                    }
                }
            }
            return announcements;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Checks if a date falls within the week range using half-open semantics.
     * A date D is included if startIncl <= D < endExcl.
     */
    private boolean isDateInRange(LocalDate date, WeekRange range) {
        return !date.isBefore(range.startIncl()) && date.isBefore(range.endExcl());
    }

    /**
     * Maps CourseDto to Course domain object.
     */
    private Course mapToCourse(CourseDto dto) {
        String courseId = String.valueOf(dto.id);
        String courseName = CourseCodeUtil.extract(dto.name != null ? dto.name.trim() : "");
        return new Course(courseId, courseName);
    }

    /**
     * Maps AssignmentDto to Assignment domain object.
     */
    private Assignment mapToAssignment(AssignmentDto dto) {
        String assignmentId = String.valueOf(dto.id);
        String courseId = String.valueOf(dto.course_id);
        String assignmentName = dto.name != null ? dto.name.trim() : "";

        Optional<LocalDate> dueDateOpt = DateTimeUtil.parseDateFromDateTime(dto.due_at);
        Optional<LocalTime> dueTimeOpt = DateTimeUtil.parseTimeFromDateTime(dto.due_at);

        String dueDate = dueDateOpt.map(DateTimeUtil::formatYMD).orElse("");
        String dueTime = dueTimeOpt.map(DateTimeUtil::formatHM).orElse("");

        String createdAt = dto.created_at != null ? dto.created_at.trim() : "";

        return new Assignment(assignmentId, courseId, assignmentName, dueDate, dueTime, null, createdAt);
    }

    /**
     * Maps AnnouncementDto to Announcement domain object.
     */
    private Announcement mapToAnnouncement(AnnouncementDto dto) {
        String announcementId = String.valueOf(dto.id);
        String courseId = String.valueOf(dto.course_id);
        String title = dto.title != null ? dto.title.trim() : "";

        Optional<LocalDate> postedDateOpt = DateTimeUtil.parseDateFromDateTime(dto.posted_at);
        Optional<LocalTime> postedTimeOpt = DateTimeUtil.parseTimeFromDateTime(dto.posted_at);

        String postedAt;
        if (postedDateOpt.isPresent() && postedTimeOpt.isPresent()) {
            postedAt = DateTimeUtil.formatYMD(postedDateOpt.get()) + " " + DateTimeUtil.formatHM(postedTimeOpt.get());
        } else {
            postedAt = "";
        }

        String body = "";
        if (dto.message != null && !dto.message.trim().isEmpty()) {
            body = dto.message.trim();
        } else if (dto.body != null && !dto.body.trim().isEmpty()) {
            body = dto.body.trim();
        }

        return new Announcement(announcementId, courseId, title, postedAt, body);
    }

    private static class CourseDto {

        public Long id;
        public String name;
    }

    private static class AssignmentDto {

        public Long id;
        public Long course_id;
        public String name;
        public String due_at;
        public String created_at;
    }

    private static class AnnouncementDto {

        public Long id;
        public Long course_id;
        public String title;
        public String posted_at;
        public String message;
        public String body;
    }
}
