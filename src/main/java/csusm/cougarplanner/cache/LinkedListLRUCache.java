package csusm.cougarplanner.cache;

import java.util.HashMap;

public class LinkedListLRUCache<T> implements Cache<T> {

    private final int maxSize;

    private final HashMap<String, Node<String, T>> cacheMap;

    private final Node<String, T> head;

    private final Node<String, T> tail;

    public LinkedListLRUCache(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be greater than 0, got: " + maxSize);
        }
        this.maxSize = maxSize;
        this.cacheMap = new HashMap<>();

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

        if (existing != null) {
            existing.value = value;
            moveToHead(existing);
            return;
        }

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