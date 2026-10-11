/** Common operations of BST and HashTable, so both can be tested the same way. */
public interface SymbolTable<K, V> {

    void insert(K key, V value);

    V search(K key);

    boolean contains(K key);

    boolean delete(K key);

    int size();
}
