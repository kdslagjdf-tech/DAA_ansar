import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Unbalanced Binary Search Tree.
 *
 * All operations are iterative, so a degenerate tree (built from sorted keys)
 * with 10,000 levels does not cause a StackOverflowError.
 */
public class BST<K extends Comparable<K>, V> implements SymbolTable<K, V> {

    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> left, right;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private Node<K, V> root;
    private int size;

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    // ---------- insert ----------

    /** Inserts key; if the key already exists, its value is updated. */
    public void insert(K key, V value) {
        if (root == null) {
            root = new Node<>(key, value);
            size++;
            return;
        }
        Node<K, V> current = root;
        while (true) {
            int cmp = key.compareTo(current.key);
            if (cmp < 0) {
                if (current.left == null) {
                    current.left = new Node<>(key, value);
                    size++;
                    return;
                }
                current = current.left;
            } else if (cmp > 0) {
                if (current.right == null) {
                    current.right = new Node<>(key, value);
                    size++;
                    return;
                }
                current = current.right;
            } else {
                current.value = value; // duplicate key -> update
                return;
            }
        }
    }

    // ---------- search ----------

    /** Returns the value for key, or null if the key is absent. */
    public V search(K key) {
        Node<K, V> node = findNode(key);
        return node == null ? null : node.value;
    }

    public boolean contains(K key) {
        return findNode(key) != null;
    }

    private Node<K, V> findNode(K key) {
        Node<K, V> current = root;
        while (current != null) {
            int cmp = key.compareTo(current.key);
            if (cmp < 0) current = current.left;
            else if (cmp > 0) current = current.right;
            else return current;
        }
        return null;
    }

    // ---------- delete ----------

    /** Deletes key. Returns true if it was found and removed. */
    public boolean delete(K key) {
        Node<K, V> parent = null;
        Node<K, V> current = root;
        while (current != null) {
            int cmp = key.compareTo(current.key);
            if (cmp == 0) break;
            parent = current;
            current = cmp < 0 ? current.left : current.right;
        }
        if (current == null) return false;

        // Case 3: two children -> copy the in-order successor (minimum of the
        // right subtree) into this node, then delete the successor instead.
        if (current.left != null && current.right != null) {
            Node<K, V> succParent = current;
            Node<K, V> succ = current.right;
            while (succ.left != null) {
                succParent = succ;
                succ = succ.left;
            }
            current.key = succ.key;
            current.value = succ.value;
            parent = succParent;
            current = succ;
        }

        // Cases 1 and 2: current now has at most one child.
        Node<K, V> child = current.left != null ? current.left : current.right;
        if (parent == null) root = child;
        else if (parent.left == current) parent.left = child;
        else parent.right = child;

        size--;
        return true;
    }

    // ---------- helpers ----------

    /** Height of the tree (number of levels), computed level by level. */
    public int height() {
        if (root == null) return 0;
        int h = 0;
        List<Node<K, V>> level = new ArrayList<>();
        level.add(root);
        while (!level.isEmpty()) {
            h++;
            List<Node<K, V>> next = new ArrayList<>();
            for (Node<K, V> n : level) {
                if (n.left != null) next.add(n.left);
                if (n.right != null) next.add(n.right);
            }
            level = next;
        }
        return h;
    }

    /** Keys in sorted order (iterative in-order traversal). */
    public List<K> inorder() {
        List<K> result = new ArrayList<>(size);
        Deque<Node<K, V>> stack = new ArrayDeque<>();
        Node<K, V> current = root;
        while (!stack.isEmpty() || current != null) {
            while (current != null) {
                stack.push(current);
                current = current.left;
            }
            current = stack.pop();
            result.add(current.key);
            current = current.right;
        }
        return result;
    }
}
