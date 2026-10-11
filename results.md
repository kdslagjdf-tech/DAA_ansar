Java 21.0.12.1, Windows 11, median of 11 runs after JVM warm-up.

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

