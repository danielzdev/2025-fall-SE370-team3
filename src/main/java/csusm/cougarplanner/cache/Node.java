package csusm.cougarplanner.cache;

/**
 * Doubly-linked list node used internally by {@link LinkedListLRUCache}.
 * <p>
 * Stores both the key (so we can remove the entry from the backing hash map
 * when evicted) and the value, plus prev/next pointers that let the cache
 * re-order nodes to the head in O(1) on every access.
 */
public class Node<K, T> {

    K key;

    T value;

    Node<K, T> prev;

    Node<K, T> next;

    public Node(K key, T value) {
        this.key   = key;
        this.value = value;
        this.prev  = null;
        this.next  = null;
    }
}