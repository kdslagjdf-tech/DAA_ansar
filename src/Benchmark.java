import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Benchmark of BST vs HashTable with 100, 1,000 and 10,000 elements.
 *
 * Two input orders are tested:
 *   random keys - average case for the BST
 *   sorted keys - worst case for the BST (the tree becomes a linked list)
 *
 * For every run we time inserting all n keys, searching all n keys and
 * deleting all n keys (search and delete go in a shuffled order).
 * The JVM is warmed up first, then each value is the median of REPEATS runs.
 * Results are printed and saved to results.md.
 */
public class Benchmark {

    private static final int[] SIZES = {100, 1_000, 10_000};
    private static final int REPEATS = 11;
    private static final int WARMUP_ROUNDS = 5;
    private static final long SEED = 42;

    private record Result(String order, int n, String structure,
                          double insertMs, double searchMs, double deleteMs, String notes) {}

    private interface Factory {
        SymbolTable<Integer, Integer> create();
    }

    /** n unique random integers from [0, 10n), optionally sorted. */
    private static Integer[] makeKeys(int n, boolean sorted, Random rng) {
        int range = n * 10;
        int[] pool = new int[range];
        for (int i = 0; i < range; i++) pool[i] = i;
        Integer[] keys = new Integer[n];
        for (int i = 0; i < n; i++) { // partial Fisher-Yates shuffle
            int j = i + rng.nextInt(range - i);
            int tmp = pool[i];
            pool[i] = pool[j];
            pool[j] = tmp;
            keys[i] = pool[i];
        }
        if (sorted) Arrays.sort(keys);
        return keys;
    }

    private static Integer[] shuffled(Integer[] keys, Random rng) {
        Integer[] copy = keys.clone();
        Collections.shuffle(Arrays.asList(copy), rng);
        return copy;
    }

    /** One run; returns {insertNs, searchNs, deleteNs}. */
    private static long[] runOnce(SymbolTable<Integer, Integer> table, Integer[] keys, Integer[] lookupOrder) {
        long t0 = System.nanoTime();
        for (Integer k : keys) table.insert(k, k);
        long t1 = System.nanoTime();
        int found = 0;
        for (Integer k : lookupOrder) if (table.contains(k)) found++;
        long t2 = System.nanoTime();
        for (Integer k : lookupOrder) table.delete(k);
        long t3 = System.nanoTime();

        // also stops the JIT from removing the search loop as dead code
        if (found != keys.length || table.size() != 0) {
            throw new IllegalStateException("benchmark run produced wrong result");
        }
        return new long[]{t1 - t0, t2 - t1, t3 - t2};
    }

    private static double medianMs(long[] values) {
        long[] sorted = values.clone();
        Arrays.sort(sorted);
        return sorted[sorted.length / 2] / 1_000_000.0;
    }

    private static String notes(String structure, Integer[] keys) {
        if (structure.equals("BST")) {
            BST<Integer, Integer> t = new BST<>();
            for (Integer k : keys) t.insert(k, k);
            return "tree height = " + t.height();
        }
        HashTable<Integer, Integer> h = new HashTable<>();
        for (Integer k : keys) h.insert(k, k);
        return String.format(Locale.US, "capacity = %d, load factor = %.2f, longest chain = %d",
                h.capacity(), h.loadFactor(), h.longestChain());
    }

    private static List<Result> runAll(boolean print) {
        String[] names = {"BST", "HashTable"};
        Factory[] factories = {BST::new, HashTable::new};
        List<Result> results = new ArrayList<>();

        for (String order : new String[]{"random", "sorted"}) {
            for (int n : SIZES) {
                for (int s = 0; s < names.length; s++) {
                    Random rng = new Random(SEED); // same keys for both structures
                    Integer[] keys = makeKeys(n, order.equals("sorted"), rng);
                    long[] ins = new long[REPEATS], sea = new long[REPEATS], del = new long[REPEATS];
                    for (int r = 0; r < REPEATS; r++) {
                        long[] t = runOnce(factories[s].create(), keys, shuffled(keys, rng));
                        ins[r] = t[0];
                        sea[r] = t[1];
                        del[r] = t[2];
                    }
                    Result res = new Result(order, n, names[s],
                            medianMs(ins), medianMs(sea), medianMs(del), notes(names[s], keys));
                    results.add(res);
                    if (print) {
                        System.out.printf(Locale.US,
                                "%-6s n=%6d %-9s insert=%10.4f ms  search=%10.4f ms  delete=%10.4f ms  (%s)%n",
                                order, n, names[s], res.insertMs(), res.searchMs(), res.deleteMs(), res.notes());
                    }
                }
            }
        }
        return results;
    }

    private static String toMarkdown(List<Result> results) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Java %s, %s, median of %d runs after JVM warm-up.%n%n",
                System.getProperty("java.version"), System.getProperty("os.name"), REPEATS));
        String[][] sections = {{"random", "Random keys (average case)"},
                               {"sorted", "Sorted keys (worst case for BST)"}};
        for (String[] section : sections) {
            sb.append("### ").append(section[1]).append("\n\n");
            sb.append("| n | Structure | Insert (ms) | Search (ms) | Delete (ms) | Notes |\n");
            sb.append("|---:|---|---:|---:|---:|---|\n");
            for (Result r : results) {
                if (!r.order().equals(section[0])) continue;
                sb.append(String.format(Locale.US, "| %,d | %s | %.4f | %.4f | %.4f | %s |%n",
                        r.n(), r.structure(), r.insertMs(), r.searchMs(), r.deleteMs(), r.notes()));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Warming up the JVM...");
        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            runAll(false); // let the JIT compiler optimise the code before measuring
        }
        List<Result> results = runAll(true);

        String md = toMarkdown(results);
        Files.writeString(Path.of("results.md"), md);
        System.out.println();
        System.out.println(md);
    }
}
