package csusm.cougarplanner.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import csusm.cougarplanner.API;
import csusm.cougarplanner.cache.CacheManager;
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

    private static final String CACHE_COURSES = "courses";
    private static final String CACHE_ASSIGNMENTS_PREFIX = "assignments:";
    private static final String CACHE_ANNOUNCEMENTS_PREFIX = "announcements:";

    private final API api;
    private final Gson gson;
    private final CoursesRepository coursesRepository;
    private final AssignmentsRepository assignmentsRepository;
    private final AnnouncementsRepository announcementsRepository;
    private final ExecutorService executor;
    private final CacheManager cacheManager;

    public CanvasService(API api) {
        this.api = api;
        this.gson = new Gson();
        this.coursesRepository = new CoursesRepository();
        this.assignmentsRepository = new AssignmentsRepository();
        this.announcementsRepository = new AnnouncementsRepository();
        this.executor = Executors.newFixedThreadPool(10);
        this.cacheManager = CacheManager.getInstance();
    }

    /**
     * Gets all active courses using a three difference lookup
     * LRU memory cache
     * repository
     * Canvas API
     * Populates every layer on a cache miss so the next call is cheaper.
     *
     * @return list of active courses, or an empty list on unrecoverable error
     */
    public List<Course> fetchCourses() {
        // 1. LRU memory cache
        List<Course> cached = cacheManager.get(CACHE_COURSES, CACHE_COURSES);
        if (cached != null) {
            return cached;
        }
        // 2. CSV repository
        try {
            List<Course> fromDisk = coursesRepository.findAll();
            if (!fromDisk.isEmpty()) {
                cacheManager.put(CACHE_COURSES, CACHE_COURSES, fromDisk);
                return fromDisk;
            }
        } catch (IOException e) {
            System.err.println("Error reading courses from repository: " + e.getMessage());
        }
        // 3. Canvas API
        List<Course> fromApi = fetchCoursesFromApi();
        if (!fromApi.isEmpty()) {
            try {
                coursesRepository.upsertAll(fromApi);
            } catch (IOException e) {
                System.err.println("Error persisting courses to repository: " + e.getMessage());
            }
            cacheManager.put(CACHE_COURSES, CACHE_COURSES, fromApi);
        }
        return fromApi;
    }

    public List<Assignment> fetchAssignments(WeekRange range) {
        String cacheKey = buildWeekCacheKey(CACHE_ASSIGNMENTS_PREFIX, range);
        List<Assignment> cached = cacheManager.get("assignments", cacheKey);
        if (cached != null) {
            return cached;
        }
        try {
            List<Assignment> fromDisk = assignmentsRepository.findByWeek(range.startIncl(), range.endExcl());
            if (!fromDisk.isEmpty()) {
                cacheManager.put("assignments", cacheKey, fromDisk);
                return fromDisk;
            }
        } catch (IOException e) {
            System.err.println("Error reading assignments from repository: " + e.getMessage());
        }
        List<Assignment> fromApi = fetchAssignmentsFromApiAsync(range);
        if (!fromApi.isEmpty()) {
            try {
                assignmentsRepository.upsertAll(fromApi);
            } catch (IOException e) {
                System.err.println("Error persisting assignments to repository: " + e.getMessage());
            }
            cacheManager.put("assignments", cacheKey, fromApi);
        }
        return fromApi;
    }

    public List<Announcement> fetchAnnouncements(WeekRange range) {
        String cacheKey = buildWeekCacheKey(CACHE_ANNOUNCEMENTS_PREFIX, range);
        List<Announcement> cached = cacheManager.get("announcements", cacheKey);
        if (cached != null) {
            return cached;
        }
        try {
            List<Announcement> fromDisk = announcementsRepository.findByWeek(range.startIncl(), range.endExcl());
            if (!fromDisk.isEmpty()) {
                cacheManager.put("announcements", cacheKey, fromDisk);
                return fromDisk;
            }
        } catch (IOException e) {
            System.err.println("Error reading announcements from repository: " + e.getMessage());
        }
        List<Announcement> fromApi = fetchAnnouncementsFromApiAsync(range);
        if (!fromApi.isEmpty()) {
            try {
                announcementsRepository.upsertAll(fromApi);
            } catch (IOException e) {
                System.err.println("Error persisting announcements to repository: " + e.getMessage());
            }
            cacheManager.put("announcements", cacheKey, fromApi);
        }
        return fromApi;
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
    private List<Assignment> fetchAssignmentsFromApiAsync(WeekRange range) {
        List<Course> courses = fetchCourses();
        if (courses.isEmpty()) {
            return Collections.emptyList();
        }

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
    private List<Announcement> fetchAnnouncementsFromApiAsync(WeekRange range) {
        List<Course> courses = fetchCourses();
        if (courses.isEmpty()) {
            return Collections.emptyList();
        }

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
     * Builds a stable, human-readable cache key for a week range.
     */
    private String buildWeekCacheKey(String prefix, WeekRange range) {
        return prefix + range.startIncl() + "/" + range.endExcl();
    }

    /**
     * Returns true if the given date falls within the week range [startIncl, endExcl).
     */
    private boolean isDateInRange(LocalDate date, WeekRange range) {
        return !date.isBefore(range.startIncl()) && date.isBefore(range.endExcl());
    }

    private Course mapToCourse(CourseDto dto) {
        String courseId = String.valueOf(dto.id);
        String courseName = CourseCodeUtil.extract(dto.name != null ? dto.name.trim() : "");
        return new Course(courseId, courseName);
    }

    private Assignment mapToAssignment(AssignmentDto dto) {
        String assignmentId = String.valueOf(dto.id);
        String courseId = String.valueOf(dto.course_id);
        String name = dto.name != null ? dto.name.trim() : "";

        Optional<LocalDate> dueDateOpt = DateTimeUtil.parseDateFromDateTime(dto.due_at);
        Optional<LocalTime> dueTimeOpt = DateTimeUtil.parseTimeFromDateTime(dto.due_at);

        String dueDate = dueDateOpt.map(DateTimeUtil::formatYMD).orElse("");
        String dueTime = dueTimeOpt.map(DateTimeUtil::formatHM).orElse("");
        String createdAt = dto.created_at != null ? dto.created_at.trim() : "";

        return new Assignment(assignmentId, courseId, name, dueDate, dueTime, null, createdAt);
    }

    private Announcement mapToAnnouncement(AnnouncementDto dto) {
        String announcementId = String.valueOf(dto.id);
        String courseId = String.valueOf(dto.course_id);
        String title = dto.title != null ? dto.title.trim() : "";

        Optional<LocalDate> postedDateOpt = DateTimeUtil.parseDateFromDateTime(dto.posted_at);
        Optional<LocalTime> postedTimeOpt = DateTimeUtil.parseTimeFromDateTime(dto.posted_at);

        String postedAt = (postedDateOpt.isPresent() && postedTimeOpt.isPresent())
                ? DateTimeUtil.formatYMD(postedDateOpt.get()) + " " + DateTimeUtil.formatHM(postedTimeOpt.get())
                : "";

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