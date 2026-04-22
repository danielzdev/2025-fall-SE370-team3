package csusm.cougarplanner.cache;

class CourseCache extends LinkedListLRUCache<Object> {

    private static final int DEFAULT_CAPACITY = 4;

    public CourseCache() {
        super(DEFAULT_CAPACITY);
    }
}

class AssignmentCache extends LinkedListLRUCache<Object> {

    private static final int DEFAULT_CAPACITY = 12;

    public AssignmentCache() {
        super(DEFAULT_CAPACITY);
    }
}

class AnnouncementCache extends LinkedListLRUCache<Object> {

    private static final int DEFAULT_CAPACITY = 12;

    public AnnouncementCache() {
        super(DEFAULT_CAPACITY);
    }
}