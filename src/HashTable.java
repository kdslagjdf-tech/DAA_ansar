/**
 * Hash Table with separate chaining and dynamic resizing.
 * When the load factor exceeds 0.75 the table doubles its capacity.
 */
public class HashTable<K, V> implements SymbolTable<K, V> {

    private static class Entry<K, V> {
        final K key;
        V value;
        Entry<K, V> next;

        Entry(K key, V value, Entry<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    private static final int DEFAULT_CAPACITY = 16;
    private static final double MAX_LOAD_FACTOR = 0.75;

    private Entry<K, V>[] buckets;
    private int size;

    public HashTable() {
        this(DEFAULT_CAPACITY);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public HashTable(int capacity) {
        buckets = (Entry<K, V>[]) new Entry[capacity];
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private int index(K key, int capacity) {
        return (key.hashCode() & 0x7fffffff) % capacity;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void resize(int newCapacity) {
        Entry<K, V>[] old = buckets;
        buckets = (Entry<K, V>[]) new Entry[newCapacity];
        for (Entry<K, V> head : old) {
            for (Entry<K, V> e = head; e != null; ) {
                Entry<K, V> next = e.next;
                int i = index(e.key, newCapacity);
                e.next = buckets[i];
                buckets[i] = e;
                e = next;
            }
        }
    }

    // ---------- insert ----------

    /** Inserts key; if the key already exists, its value is updated. */
    public void insert(K key, V value) {
        int i = index(key, buckets.length);
        for (Entry<K, V> e = buckets[i]; e != null; e = e.next) {
            if (e.key.equals(key)) {
                e.value = value;
                return;
            }
        }
        buckets[i] = new Entry<>(key, value, buckets[i]);
        size++;
        if ((double) size / buckets.length > MAX_LOAD_FACTOR) {
            resize(buckets.length * 2);
        }
    }

    // ---------- search ----------

    /** Returns the value for key, or null if the key is absent. */
    public V search(K key) {
        for (Entry<K, V> e = buckets[index(key, buckets.length)]; e != null; e = e.next) {
            if (e.key.equals(key)) return e.value;
        }
        return null;
    }

    public boolean contains(K key) {
        for (Entry<K, V> e = buckets[index(key, buckets.length)]; e != null; e = e.next) {
            if (e.key.equals(key)) return true;
        }
        return false;
    }

    // ---------- delete ----------

    /** Deletes key. Returns true if it was found and removed. */
    public boolean delete(K key) {
        int i = index(key, buckets.length);
        Entry<K, V> prev = null;
        for (Entry<K, V> e = buckets[i]; e != null; prev = e, e = e.next) {
            if (e.key.equals(key)) {
                if (prev == null) buckets[i] = e.next;
                else prev.next = e.next;
                size--;
                return true;
            }
        }
        return false;
    }

    // ---------- helpers ----------

    public int capacity() {
        return buckets.length;
    }

    public double loadFactor() {
        return (double) size / buckets.length;
    }

    public int longestChain() {
        int max = 0;
        for (Entry<K, V> head : buckets) {
            int len = 0;
            for (Entry<K, V> e = head; e != null; e = e.next) len++;
            max = Math.max(max, len);
        }
        return max;
    }
}
