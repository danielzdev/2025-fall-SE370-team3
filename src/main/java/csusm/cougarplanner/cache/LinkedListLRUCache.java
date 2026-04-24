package csusm.cougarplanner.cache;

import java.util.HashMap;

/**
 * Cache implemented with a HashMap + doubly-linked list.
 * <p>
 * The map gives O(1) lookup from key to node; the linked list keeps nodes
 * ordered from most-recently-used (next to head) to least-recently-used
 * (next to tail). Every get/put moves the touched node to the head, so when
 * the cache fills up we can evict the tail in O(1).
 * <p>
 * {@code head} and {@code tail} are sentinel nodes — they don't hold real
 * data. Sentinels eliminate the null checks that would otherwise be needed
 * at the ends of the list on every insert/remove.
 */
public class LinkedListLRUCache<T> implements Cache<T> {

    private final int maxSize;

    private final HashMap<String, Node<String, T>> cacheMap;

    // head — the node immediately after head is the most recently used.
    private final Node<String, T> head;

    // tail — the node immediately before tail is the LRU and is evicted first.
    private final Node<String, T> tail;

    public LinkedListLRUCache(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be greater than 0, got: " + maxSize);
        }
        this.maxSize = maxSize;
        this.cacheMap = new HashMap<>();

        // Stitch the tail and head together so the list is never empty of pointers —
        // this lets addToHead / detach skip null checks on the endpoints.
        this.head = new Node<>(null, null);
        this.tail = new Node<>(null, null);
        head.next = tail;
        tail.prev = head;
    }

    @Override
    public T get(String key) {
        Node<String, T> node = cacheMap.get(key);
        if (node == null) {
            return null;
        }
        moveToHead(node);
        return node.value;
    }

    @Override
    public void put(String key, T value) {
        Node<String, T> existing = cacheMap.get(key);

        // Overwrite path: update value and bump to most-recently-used.
        if (existing != null) {
            existing.value = value;
            moveToHead(existing);
            return;
        }

        // Insert path: evict LRU first if we'd otherwise exceed capacity.
        if (cacheMap.size() >= maxSize) {
            removeTail();
        }

        Node<String, T> newNode = new Node<>(key, value);
        cacheMap.put(key, newNode);
        addToHead(newNode);
    }

    @Override
    public void remove(String key) {
        Node<String, T> node = cacheMap.remove(key);
        if (node != null) {
            detach(node);
        }
    }

    @Override
    public int size() {
        return cacheMap.size();
    }

    private void moveToHead(Node<String, T> node) {
        detach(node);
        addToHead(node);
    }

    private void addToHead(Node<String, T> node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    private void detach(Node<String, T> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void removeTail() {
        Node<String, T> lru = tail.prev;
        if (lru == head) {
            return;
        }
        detach(lru);
        cacheMap.remove(lru.key);
    }

    public boolean isEmpty() {
        return cacheMap.isEmpty();
    }
}
