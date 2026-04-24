package csusm.cougarplanner.cache;

// Caches keyed by resource type, registered in CacheManager.
// Capacities are chosen to fit a typical student's workload: a handful of
// active courses, and roughly one assignment/announcement bucket per course.

/** A student rarely has more than a few active courses at once. */
class CourseCache extends LinkedListLRUCache<Object> {

    private static final int DEFAULT_CAPACITY = 4;

    public CourseCache() {
        super(DEFAULT_CAPACITY);
    }
}

/** Capacity covers multiple courses' worth of data. */
class AssignmentCache extends LinkedListLRUCache<Object> {

    private static final int DEFAULT_CAPACITY = 12;

    public AssignmentCache() {
        super(DEFAULT_CAPACITY);
    }
}

/** Sized to match AssignmentCache. */
class AnnouncementCache extends LinkedListLRUCache<Object> {

    private static final int DEFAULT_CAPACITY = 12;

    public AnnouncementCache() {
        super(DEFAULT_CAPACITY);
    }
}
