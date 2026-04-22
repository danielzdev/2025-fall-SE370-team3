package csusm.cougarplanner.cache;

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