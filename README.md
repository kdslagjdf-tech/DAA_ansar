# BST vs Hash Table

Implementation and comparison of a **Binary Search Tree** and a **Hash Table** in Java.

## Project structure

| File | Description |
|---|---|
| `src/SymbolTable.java` | Common interface: `insert`, `search`, `contains`, `delete`, `size` |
| `src/BST.java` | Binary Search Tree (unbalanced, iterative operations) |
| `src/HashTable.java` | Hash Table with separate chaining and dynamic resizing |
| `src/Tests.java` | Correctness tests (compared against `java.util.HashSet` / `TreeSet`) |
| `src/Benchmark.java` | Timing experiment for 100, 1,000 and 10,000 elements |
| `results.md` | Raw benchmark output |

## How to run

Requires JDK 17 or newer.

```bash
javac -d out src/*.java
java -cp out Tests
java -cp out Benchmark
```

---

## 1. Implementation

### Binary Search Tree (`BST.java`)

Each node stores a key, a value and links to the `left` and `right` children.
The BST property holds for every node: all keys in the left subtree are smaller,
all keys in the right subtree are larger. Duplicate keys are not stored twice;
inserting an existing key updates its value.

All operations are written **iteratively** (with a loop instead of recursion).
When the input is sorted the tree turns into a chain 10,000 levels deep, and a
recursive version would throw `StackOverflowError`.

### Hash Table (`HashTable.java`)

The table is an array of buckets. Each bucket is a singly linked list of entries
(**separate chaining**). The bucket of a key is

```
index = (key.hashCode() & 0x7fffffff) % capacity
```

The table starts with 16 buckets. When the **load factor** `α = size / capacity`
becomes greater than 0.75, the capacity is doubled and every entry is rehashed
into the new array. This keeps the chains short.

---

## 2. Analysis of insertion, searching and deletion

### BST

`h` denotes the height of the tree.

- **Insertion.** Start at the root, compare the new key with the current node and go
  left (smaller) or right (larger) until an empty position is found. The new node is
  attached there. The number of steps equals the depth of that position: **O(h)**.
- **Searching.** Follows the same path from the root. It stops when the key is found
  or when it reaches `null` (the key is absent): **O(h)**.
- **Deletion.** First the node is found (O(h)). Then there are three cases:
  1. *leaf*: it is simply removed;
  2. *one child*: the parent is linked directly to that child;
  3. *two children*: the in-order successor (the minimum of the right subtree) is
     found, its key and value are copied into the node, and the successor is removed.
     The successor has at most one child, so this reduces to case 1 or 2.

  Finding the successor continues down the same branch, so the total is still **O(h)**.

Everything depends on the height `h`:
- for keys inserted in **random order**, the expected height is about 2–3 · log₂ n,
  so all operations are **O(log n)** on average;
- for keys inserted in **sorted order**, every new key goes to the right of the
  previous one, the tree becomes a linked list with `h = n`, and all operations
  become **O(n)**.

### Hash Table

- **Insertion.** Compute the hash and the bucket index (O(1)), scan the chain to check
  whether the key already exists, then add the new entry at the head of the chain.
  The expected chain length is α ≤ 0.75, so this is **O(1)** on average. Sometimes an
  insertion triggers a resize, which rehashes all n entries (O(n)). Resizes happen only
  when the size doubles, so the cost spread over all insertions is still **amortized O(1)**.
- **Searching.** Compute the index, then scan only that one chain: **O(1)** on average.
- **Deletion.** Compute the index, find the entry in the chain and unlink it
  (`prev.next = e.next`): **O(1)** on average.

The worst case happens when many keys get the same index (collisions). In the extreme
case all n keys are in one chain and the table behaves like a linked list: **O(n)**.
`Tests.java` checks this case with a key class whose `hashCode()` always returns 7.

---

## 3. Best-case and worst-case time complexity

| Operation | BST best | BST average | BST worst | Hash Table best | Hash Table average | Hash Table worst |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| Insert | O(1) | O(log n) | O(n) | O(1) | O(1) amortized | O(n) |
| Search | O(1) | O(log n) | O(n) | O(1) | O(1) | O(n) |
| Delete | O(1) | O(log n) | O(n) | O(1) | O(1) | O(n) |
| Space | O(n) | O(n) | O(n) | O(n) | O(n) | O(n) |

**BST**
- *Best case O(1)*: the key is at the root (search / delete), or the tree is empty
  or the new position is directly under the root (insert).
- *Worst case O(n)*: the tree is degenerate (keys were inserted in sorted or reverse
  order), so we walk through all n nodes. A self-balancing tree (AVL, Red-Black)
  guarantees O(log n), but a plain BST does not.

**Hash Table**
- *Best case O(1)*: the key's bucket is empty or the key is the first entry in it.
- *Worst case O(n)*: all keys collide into the same bucket (bad hash function or
  adversarial input), or the insertion that triggers a resize has to rehash all n entries.

For building a structure of n elements: BST takes O(n log n) on average and O(n²) in
the worst case; the Hash Table takes O(n) expected.

---

## 4. Experimental results

**Method** (`Benchmark.java`):
- n = 100, 1,000 and 10,000 unique integer keys taken from the range [0, 10n);
- two input orders: **random** (the average case) and **sorted** (the worst case for a BST);
- for each run we measure the total time to insert all n keys, then search all n keys,
  then delete all n keys. Search and delete use a shuffled order. Timing uses `System.nanoTime()`;
- the JVM is warmed up first (5 full rounds), so that the JIT compiler has optimised the code.
  Each value in the table is the **median of 11 runs**;
- both structures get exactly the same keys (same random seed).

**Environment:** Java 21 (OpenJDK 21.0.12), Windows 11, Intel Core i7-12700KF, 40 GB RAM.

Times are in milliseconds, **total for all n operations**.

### Random keys (average case)

| n | Structure | Insert (ms) | Search (ms) | Delete (ms) | Notes |
|---:|---|---:|---:|---:|---|
| 100 | BST | 0.0013 | 0.0034 | 0.0043 | tree height = 12 |
| 100 | HashTable | 0.0015 | 0.0005 | 0.0006 | capacity = 256, load factor = 0.39, longest chain = 2 |
| 1,000 | BST | 0.0449 | 0.0543 | 0.0627 | tree height = 23 |
| 1,000 | HashTable | 0.0308 | 0.0052 | 0.0062 | capacity = 2048, load factor = 0.49, longest chain = 3 |
| 10,000 | BST | 0.7992 | 0.9593 | 1.0044 | tree height = 35 |
| 10,000 | HashTable | 0.2517 | 0.0677 | 0.0818 | capacity = 16384, load factor = 0.61, longest chain = 4 |

### Sorted keys (worst case for BST)

| n | Structure | Insert (ms) | Search (ms) | Delete (ms) | Notes |
|---:|---|---:|---:|---:|---|
| 100 | BST | 0.0071 | 0.0096 | 0.0066 | tree height = 100 |
| 100 | HashTable | 0.0013 | 0.0006 | 0.0006 | capacity = 256, load factor = 0.39, longest chain = 2 |
| 1,000 | BST | 0.7365 | 0.8734 | 0.6020 | tree height = 1000 |
| 1,000 | HashTable | 0.0177 | 0.0054 | 0.0063 | capacity = 2048, load factor = 0.49, longest chain = 3 |
| 10,000 | BST | 99.1782 | 118.9688 | 82.8557 | tree height = 10000 |
| 10,000 | HashTable | 0.2363 | 0.0683 | 0.0816 | capacity = 16384, load factor = 0.61, longest chain = 4 |

### Comparison

1. **The Hash Table is faster in almost every case.** With 10,000 random keys, search is
   **~14× faster** (0.068 ms vs 0.959 ms), delete is ~12× faster and insert is ~3× faster.
   The time per search stays almost constant: about 5 ns per operation for n = 1,000 and
   7 ns for n = 10,000. This matches **O(1)**. The longest chain never had more than 4 entries.
2. **The BST on random keys behaves like O(log n).** The tree height was 12 / 23 / 35,
   close to 2–3 · log₂ n. The time per search grows slowly: 34 → 54 → 96 ns per operation.
3. **The BST on sorted keys shows its O(n) worst case.** The height equals n
   (100 / 1,000 / 10,000). When n grows 10× (1,000 → 10,000), the total time grows
   **~135×**. That is quadratic growth for n operations, O(n²) = O(n) per operation.
   With 10,000 sorted keys the BST search is ~124× slower than on random keys and
   **~1,700× slower than the Hash Table**.
4. **Input order does not matter for the Hash Table.** Random and sorted keys give the
   same times, because the hash only depends on the key, not on the order of insertion.
5. **For small n the difference is negligible.** At n = 100 all operations take a few
   microseconds, and on random keys BST insertion is as fast as Hash Table insertion. The table has to
   resize 4 times while it grows from 16 to 256 buckets.
6. Hash Table insertion is ~4× slower than Hash Table search. Insertion also allocates
   new entries and periodically rehashes the whole table.

---

## 5. Why their performance may differ in practice

Big-O describes how the running time grows. Real running time also depends on the following:

1. **Shape of the tree / order of the input.** A plain BST is only fast if it stays balanced.
   Real data is often sorted or nearly sorted (IDs, timestamps, sorted files), which
   makes the tree degenerate (height 10,000 instead of 35 in our test). Self-balancing trees
   (AVL, Red-Black — e.g. Java's `TreeMap`) fix this and guarantee O(log n).
2. **Quality of the hash function and the load factor.** The Hash Table is O(1) only if
   keys are spread evenly. A bad hash function or adversarial keys put many keys into one
   bucket, giving O(n). `Integer.hashCode()` returns the number itself, so keys that are
   all multiples of the capacity would land in the same bucket. Java's `HashMap` therefore
   mixes the bits of the hash and turns long chains into small trees.
3. **Resizing.** Insertion is amortized O(1), but a single insert that triggers a resize
   costs O(n). That is why Hash Table insert is slower than search. If the size is known
   in advance, creating the table with enough capacity avoids rehashing.
4. **Constant factors.** A BST search does about log₂ n comparisons (`compareTo`), and the
   branch it takes (left or right) is hard for the CPU to predict. A hash lookup does one
   hash computation, one array access and usually one `equals`. Big-O ignores these
   constants, but they matter for small n.
5. **Memory layout and CPU cache.** BST nodes are separate objects scattered in memory;
   every step down the tree is a pointer dereference that can be a cache miss. The Hash Table
   jumps directly to one array slot. As n grows, the tree no longer fits in the fast cache,
   which is why the measured growth was a bit worse than the theory (×17.7 instead of ×13
   for random BST search; ×135 instead of ×100 for sorted BST).
6. **JVM and hardware effects.** JIT compilation, garbage collection, CPU frequency
   changes, and scheduling on performance or efficiency cores all affect timing.
   Before we added enough JVM warm-up, the first measurements were up to 5× slower.
   Repeated runs of the same test also differ (sorted BST, n = 10,000: 76–99 ms).
   This is why we use warm-up and the median of several runs.
7. **Different capabilities.** A BST keeps keys **in order**: it supports sorted traversal,
   min / max, successor / predecessor and range queries in O(h). A Hash Table cannot do any
   of these efficiently (O(n)). A Hash Table also wastes memory on empty buckets (16,384
   buckets for 10,000 keys), while a BST stores exactly n nodes with two links each.

---

## Conclusion

- For **exact-key operations** (insert / search / delete by key), the **Hash Table** is the
  better choice: O(1) on average, 3–14× faster than the BST on random data, and it does not
  depend on the input order.
- A plain **BST** is O(log n) only on random data. On sorted data it degrades to O(n)
  per operation, and in our test it was over 1,000× slower than the Hash Table.
- A BST (preferably a balanced one) is the right choice when **ordered** operations are
  needed: sorted output, range queries, min / max.
