import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/** Correctness tests: both structures are compared against java.util collections. */
public class Tests {

    private static int checks = 0;

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }

    /** Random mix of insert / delete / search compared with a HashSet. */
    private static void randomOperations(SymbolTable<Integer, Integer> table, String name) {
        Random rng = new Random(1);
        int n = 2000;
        Set<Integer> reference = new HashSet<>();

        for (int step = 0; step < n * 5; step++) {
            int key = rng.nextInt(n + 1);
            double op = rng.nextDouble();
            if (op < 0.5) {
                table.insert(key, key * 10);
                reference.add(key);
            } else if (op < 0.8) {
                check(table.delete(key) == reference.contains(key), name + ": delete(" + key + ")");
                reference.remove(key);
            } else {
                check(table.contains(key) == reference.contains(key), name + ": contains(" + key + ")");
            }
            check(table.size() == reference.size(), name + ": size");
        }

        for (int key = 0; key <= n; key++) {
            check(table.contains(key) == reference.contains(key), name + ": final contains(" + key + ")");
            if (reference.contains(key)) {
                check(Integer.valueOf(key * 10).equals(table.search(key)), name + ": search(" + key + ")");
            } else {
                check(table.search(key) == null, name + ": search of missing key");
            }
        }

        for (int key : reference) check(table.delete(key), name + ": delete all");
        check(table.size() == 0, name + ": empty after deleting all");
        check(!table.delete(12345), name + ": delete from empty");
        check(!table.contains(0), name + ": contains on empty");
    }

    private static void duplicateKeys(SymbolTable<Integer, Integer> table, String name) {
        table.insert(5, 1);
        table.insert(5, 2);
        check(table.size() == 1, name + ": duplicate key must not grow size");
        check(Integer.valueOf(2).equals(table.search(5)), name + ": duplicate key updates value");
    }

    private static void bstDeleteCases() {
        BST<Integer, String> t = new BST<>();
        for (int k : new int[]{50, 30, 70, 20, 40, 60, 80, 65}) t.insert(k, "v" + k);

        t.delete(20); // leaf
        check(t.inorder().equals(List.of(30, 40, 50, 60, 65, 70, 80)), "BST: delete leaf");
        t.delete(60); // one child
        check(t.inorder().equals(List.of(30, 40, 50, 65, 70, 80)), "BST: delete node with one child");
        t.delete(50); // two children (the root)
        check(t.inorder().equals(List.of(30, 40, 65, 70, 80)), "BST: delete node with two children");
        check("v65".equals(t.search(65)), "BST: successor keeps its value");
    }

    private static void bstSortedOrder() {
        BST<Integer, Integer> t = new BST<>();
        Random rng = new Random(7);
        TreeSet<Integer> reference = new TreeSet<>();
        for (int i = 0; i < 1000; i++) {
            int k = rng.nextInt(10_000);
            t.insert(k, k);
            reference.add(k);
        }
        check(t.inorder().equals(new ArrayList<>(reference)), "BST: in-order traversal is sorted");
    }

    private static void bstDegenerateTree() {
        BST<Integer, Integer> t = new BST<>();
        for (int i = 0; i < 10_000; i++) t.insert(i, i); // sorted -> linked list
        check(t.height() == 10_000, "BST: sorted input gives height n");
        check(t.contains(9_999), "BST: search in degenerate tree");
        for (int i = 9_999; i >= 0; i--) t.delete(i);
        check(t.size() == 0, "BST: delete from degenerate tree");
    }

    private static void hashTableResize() {
        HashTable<Integer, Integer> h = new HashTable<>();
        for (int i = 0; i < 1000; i++) h.insert(i, i);
        check(h.capacity() > 16, "HashTable: table grows");
        check(h.loadFactor() <= 0.75, "HashTable: load factor stays <= 0.75");
        for (int i = 0; i < 1000; i++) check(h.contains(i), "HashTable: keys survive resize");
    }

    /** Key whose hashCode is always the same -> every key lands in one bucket. */
    private record BadKey(int id) {
        @Override
        public int hashCode() {
            return 7;
        }
    }

    private static void hashTableCollisions() {
        HashTable<BadKey, Integer> h = new HashTable<>();
        for (int i = 0; i < 100; i++) h.insert(new BadKey(i), i);
        check(h.longestChain() == 100, "HashTable: all keys collide into one chain");
        check(Integer.valueOf(42).equals(h.search(new BadKey(42))), "HashTable: search in long chain");
        check(h.delete(new BadKey(0)) && h.delete(new BadKey(99)) && h.delete(new BadKey(50)),
                "HashTable: delete head / tail / middle of chain");
        check(h.size() == 97 && !h.contains(new BadKey(50)), "HashTable: chain after deletes");
    }

    public static void main(String[] args) {
        randomOperations(new BST<>(), "BST");
        randomOperations(new HashTable<>(), "HashTable");
        duplicateKeys(new BST<>(), "BST");
        duplicateKeys(new HashTable<>(), "HashTable");
        bstDeleteCases();
        bstSortedOrder();
        bstDegenerateTree();
        hashTableResize();
        hashTableCollisions();
        System.out.println("All " + checks + " checks passed.");
    }
}
