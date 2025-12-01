package csusm.cougarplanner.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import csusm.cougarplanner.API;
import csusm.cougarplanner.io.AnnouncementsRepository;
import csusm.cougarplanner.io.AssignmentsRepository;
import csusm.cougarplanner.io.CoursesRepository;
import csusm.cougarplanner.models.Announcement;
import csusm.cougarplanner.models.Assignment;
import csusm.cougarplanner.models.Course;
import csusm.cougarplanner.util.CourseCodeUtil;
import csusm.cougarplanner.util.DateTimeUtil;
import csusm.cougarplanner.util.WeekRange;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public final class CanvasService {

    private final API api;
    private final Gson gson;
    private final CoursesRepository coursesRepository;
    private final AssignmentsRepository assignmentsRepository;
    private final AnnouncementsRepository announcementsRepository;
    private final ExecutorService executor;

    public CanvasService(API api) {
        this.api = api;
        this.gson = new Gson();
        this.coursesRepository = new CoursesRepository();
        this.assignmentsRepository = new AssignmentsRepository();
        this.announcementsRepository = new AnnouncementsRepository();
        // Thread pool for async API calls - one thread per course for parallel fetching
        this.executor = Executors.newFixedThreadPool(10);
    }

    /**
     * Gets all active courses, using cached data if available.
     * Fetches from API only if cache is empty, then saves to cache.
     * Returns empty list on errors.
     */
    public List<Course> fetchCourses() {
        try {
            // Try to load from cache first
            List<Course> cachedCourses = coursesRepository.findAll();
            if (!cachedCourses.isEmpty()) {
                return cachedCourses;
            }

            // If cache is empty, fetch from API and save
            List<Course> fetchedCourses = fetchCoursesFromApi();
            if (!fetchedCourses.isEmpty()) {
                coursesRepository.upsertAll(fetchedCourses);
            }
            return fetchedCourses;
        } catch (IOException e) {
            // If cache read fails, try API as fallback
            return fetchCoursesFromApi();
        }
    }

    /**
     * Fetches all active courses from Canvas API.
     * Parses JSON response and converts to Course objects.
     * Returns empty list on API errors or parsing failures.
     */
    private List<Course> fetchCoursesFromApi() {
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
            System.err.println("Error parsing courses from API: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Gets assignments for a specific week, using cached data if available.
     * Fetches from API only if cache is empty or needs refresh.
     * Uses async/parallel fetching for all courses simultaneously.
     * Returns empty list on errors.
     */
    public List<Assignment> fetchAssignments(WeekRange range) {
        try {
            // Try to load from cache first
            List<Assignment> cachedAssignments = assignmentsRepository.findByWeek(range.startIncl(), range.endExcl());
            if (!cachedAssignments.isEmpty()) {
                return cachedAssignments;
            }

            // If cache is empty, fetch from API async and save
            List<Assignment> fetchedAssignments = fetchAssignmentsFromApiAsync(range);
            if (!fetchedAssignments.isEmpty()) {
                assignmentsRepository.upsertAll(fetchedAssignments);
            }
            return fetchedAssignments;
        } catch (IOException e) {
            // If cache read fails, try API as fallback
            return fetchAssignmentsFromApiAsync(range);
        }
    }

    /**
     * Fetches assignments from Canvas API asynchronously for all courses in parallel.
     * Dramatically improves performance when there are many courses.
     * Only includes assignments with due dates within the specified range.
     * Returns empty list on API errors or parsing failures.
     */
    private List<Assignment> fetchAssignmentsFromApiAsync(WeekRange range) {
        List<Course> courses = fetchCourses();
        if (courses.isEmpty()) {
            return Collections.emptyList();
        }

        // Create async tasks for each course
        List<CompletableFuture<List<Assignment>>> futures = courses
            .stream()
            .map(course -> CompletableFuture.supplyAsync(() -> fetchAssignmentsForCourse(course, range), executor))
            .collect(Collectors.toList());

        // Wait for all tasks to complete and combine results
        return futures.stream().map(CompletableFuture::join).flatMap(List::stream).collect(Collectors.toList());
    }

    /**
     * Fetches assignments for a single course.
     * Helper method for async fetching.
     */
    private List<Assignment> fetchAssignmentsForCourse(Course course, WeekRange range) {
        List<Assignment> assignments = new ArrayList<>();
        try {
            int courseId = Integer.parseInt(course.getCourseId());
            String json = api.getAssignmentsJson(courseId);
            if (json == null) {
                return assignments;
            }

            Type listType = new TypeToken<List<AssignmentDto>>() {}.getType();
            List<AssignmentDto> dtos = gson.fromJson(json, listType);

            if (dtos == null) {
                return assignments;
            }

            for (AssignmentDto dto : dtos) {
                if (dto.id != null && dto.course_id != null) {
                    Optional<LocalDate> dueDate = DateTimeUtil.parseDateFromDateTime(dto.due_at);
                    if (dueDate.isPresent() && isDateInRange(dueDate.get(), range)) {
                        assignments.add(mapToAssignment(dto));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching assignments for course " + course.getCourseId() + ": " + e.getMessage());
        }
        return assignments;
    }

    /**
     * Gets announcements for a specific week, using cached data if available.
     * Fetches from API only if cache is empty or needs refresh.
     * Uses async/parallel fetching for all courses simultaneously.
     * Returns empty list on errors.
     */
    public List<Announcement> fetchAnnouncements(WeekRange range) {
        try {
            // Try to load from cache first
            List<Announcement> cachedAnnouncements = announcementsRepository.findByWeek(
                range.startIncl(),
                range.endExcl()
            );
            if (!cachedAnnouncements.isEmpty()) {
                return cachedAnnouncements;
            }

            // If cache is empty, fetch from API async and save
            List<Announcement> fetchedAnnouncements = fetchAnnouncementsFromApiAsync(range);
            if (!fetchedAnnouncements.isEmpty()) {
                announcementsRepository.upsertAll(fetchedAnnouncements);
            }
            return fetchedAnnouncements;
        } catch (IOException e) {
            // If cache read fails, try API as fallback
            return fetchAnnouncementsFromApiAsync(range);
        }
    }

    /**
     * Fetches announcements from Canvas API asynchronously for all courses in parallel.
     * Dramatically improves performance when there are many courses.
     * Only includes announcements with posted dates within the specified range.
     * Returns empty list on API errors or parsing failures.
     */
    private List<Announcement> fetchAnnouncementsFromApiAsync(WeekRange range) {
        List<Course> courses = fetchCourses();
        if (courses.isEmpty()) {
            return Collections.emptyList();
        }

        // Create async tasks for each course
        List<CompletableFuture<List<Announcement>>> futures = courses
            .stream()
            .map(course -> CompletableFuture.supplyAsync(() -> fetchAnnouncementsForCourse(course, range), executor))
            .collect(Collectors.toList());

        // Wait for all tasks to complete and combine results
        return futures.stream().map(CompletableFuture::join).flatMap(List::stream).collect(Collectors.toList());
    }

    /**
     * Fetches announcements for a single course.
     * Helper method for async fetching.
     */
    private List<Announcement> fetchAnnouncementsForCourse(Course course, WeekRange range) {
        List<Announcement> announcements = new ArrayList<>();
        try {
            int courseId = Integer.parseInt(course.getCourseId());
            String json = api.getAnnouncementsJson(courseId);

            if (json == null) {
                return announcements;
            }

            Type listType = new TypeToken<List<AnnouncementDto>>() {}.getType();
            List<AnnouncementDto> dtos = gson.fromJson(json, listType);

            if (dtos == null) {
                return announcements;
            }

            for (AnnouncementDto dto : dtos) {
                if (dto.id != null) {
                    // Ensure course_id is set
                    if (dto.course_id == null) {
                        dto.course_id = Long.valueOf(courseId);
                    }

                    Optional<LocalDate> postedDate = DateTimeUtil.parseDateFromDateTime(dto.posted_at);
                    if (postedDate.isPresent() && isDateInRange(postedDate.get(), range)) {
                        announcements.add(mapToAnnouncement(dto));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(
                "Error fetching announcements for course " + course.getCourseId() + ": " + e.getMessage()
            );
        }
        return announcements;
    }

    /**
     * Forces a refresh of all assignments from the API and updates the cache.
     * This will be used by the 15-minute auto-refresh mechanism.
     * Uses async fetching for performance.
     */
    public List<Assignment> refreshAssignments(WeekRange range) {
        List<Assignment> assignments = fetchAssignmentsFromApiAsync(range);
        if (!assignments.isEmpty()) {
            try {
                assignmentsRepository.upsertAll(assignments);
            } catch (IOException e) {
                System.err.println("Error saving refreshed assignments: " + e.getMessage());
            }
        }
        return assignments;
    }

    /**
     * Forces a refresh of all announcements from the API and updates the cache.
     * Uses async fetching for performance.
     */
    public List<Announcement> refreshAnnouncements(WeekRange range) {
        List<Announcement> announcements = fetchAnnouncementsFromApiAsync(range);
        if (!announcements.isEmpty()) {
            try {
                announcementsRepository.upsertAll(announcements);
            } catch (IOException e) {
                System.err.println("Error saving refreshed announcements: " + e.getMessage());
            }
        }
        return announcements;
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

    /**
     * Shuts down the executor service.
     * Should be called when the application is closing.
     */
    public void shutdown() {
        executor.shutdown();
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
