# DSA Roadmap — Senior Engineer / Architect Interview Preparation

A structured, phase-wise Data Structures & Algorithms curriculum designed to clear
product-company interviews (FAANG-level and equivalent). Every topic folder contains:

1. **Concept notes** — what it is, why it exists, internal working
2. **Real-life analogy / production use case** — where it shows up in real systems
3. **Java implementations** — clean, interview-ready code
4. **A runnable `*Demo.java`** — a `main` method with multiple worked examples that
   print step-by-step traces (dp tables, pointer movements, heap states...)
5. **Complexity tables** — time/space for every operation
6. **Deep Dive section** — dry runs, correctness arguments, interviewer follow-ups with answers
7. **Interview patterns & must-solve problems** — what interviewers actually ask
8. **Common pitfalls** — the mistakes that cost offers

### Running the demos

Each folder has one self-contained demo (no dependencies, no packages):

```bash
cd 07-heaps-priority-queues
javac HeapsDemo.java && java HeapsDemo      # JDK
# or, with Java 11+:
java HeapsDemo.java                          # compile-and-run in one step
```

Predict every printed line BEFORE running — that prediction habit is the actual studying.

---

## How to Use This Repo

- Study folders **in order** — each phase builds on the previous one.
- For each topic: read notes → implement the data structure from scratch in Java →
  solve the listed problems → revisit pitfalls before interviews.
- As a senior candidate, interviewers expect you to also **explain trade-offs**
  (why HashMap over TreeMap, why BFS over DFS), not just produce working code.
  Every folder highlights these trade-off discussions.

---

## Roadmap Structure

### Phase 1 — Foundations (Weeks 1–3)
| # | Folder | Topic | Why it matters |
|---|--------|-------|----------------|
| 01 | `01-complexity-analysis` | Big-O, time/space analysis, amortized cost | The language of every interview answer |
| 02 | `02-arrays-strings` | Arrays, strings, prefix sums, matrix | ~30% of all interview questions |
| 03 | `03-linked-lists` | Singly/doubly/circular lists, fast-slow pointers | Pointer manipulation fluency |
| 04 | `04-stacks-queues` | Stack, queue, deque, monotonic stack | Foundation for parsing, scheduling, trees, graphs |
| 05 | `05-hashing` | HashMap/HashSet internals, collision handling, design | The single most used tool in interviews |

### Phase 2 — Core Data Structures (Weeks 4–7)
| # | Folder | Topic | Why it matters |
|---|--------|-------|----------------|
| 06 | `06-trees-bst` | Binary trees, BST, traversals, balanced trees (AVL/Red-Black) | Recursion mastery + database indexes |
| 07 | `07-heaps-priority-queues` | Min/max heap, heapify, top-K pattern | Schedulers, streaming problems |
| 08 | `08-tries` | Prefix trees, autocomplete, word search | String-heavy product problems |
| 09 | `09-graphs` | BFS, DFS, topological sort, Dijkstra, MST | Hardest and highest-signal interview area |

### Phase 3 — Algorithmic Techniques (Weeks 8–11)
| # | Folder | Topic | Why it matters |
|---|--------|-------|----------------|
| 10 | `10-sorting-searching` | All sorts, binary search & its variants | Binary search-on-answer is a top senior pattern |
| 11 | `11-recursion-backtracking` | Recursion trees, permutations, N-Queens, subsets | Prerequisite for DP |
| 12 | `12-dynamic-programming` | Memoization, tabulation, classic DP families | The interview differentiator |
| 13 | `13-greedy` | Greedy choice, exchange argument, intervals | Fast optimal solutions + proving correctness |

### Phase 4 — Patterns & Advanced (Weeks 12–14)
| # | Folder | Topic | Why it matters |
|---|--------|-------|----------------|
| 14 | `14-two-pointers-sliding-window` | Two pointers, fixed/variable windows | Converts O(n²) to O(n) — asked constantly |
| 15 | `15-bit-manipulation` | Bitwise ops, masks, XOR tricks | Low-level fluency, embedded/system roles |
| 16 | `16-advanced-structures` | Union-Find, segment/Fenwick trees, LRU/LFU, intervals | Senior-level and design-adjacent questions |

---

## Recommended Weekly Rhythm

- **Weekdays (1.5–2 hrs/day):** 1 concept section + 2–3 problems on it.
- **Weekend day 1:** Implement that week's data structure from scratch, no references.
- **Weekend day 2:** Timed mock — 2 problems in 70 minutes, talk out loud.

## Problem Volume Targets (LeetCode-style)

| Level | Count | Purpose |
|-------|-------|---------|
| Easy | ~60 | Speed and syntax fluency |
| Medium | ~120 | Where interviews actually live |
| Hard | ~30 | Pattern depth (DP, graphs, binary search on answer) |

Quality beats quantity: redo every problem you couldn't solve unaided after 3 days,
then again after 2 weeks (spaced repetition).

---

## Interview Execution Framework (use in every round)

1. **Clarify** — inputs, ranges, duplicates, nulls, expected scale ("n up to 10⁹? Then O(n²) is out.")
2. **Examples** — walk a small example and an edge case by hand.
3. **Brute force first** — state it with its complexity; it anchors the optimization story.
4. **Optimize** — name the pattern (hash, two pointers, heap, DP...) and justify it.
5. **Code** — clean Java, meaningful names, helper methods.
6. **Test** — trace your own code on the example + edge cases (empty, single, max, negatives).
7. **Complexity** — state final time and space unprompted.

As a senior candidate you are also scored on communication and trade-off analysis.
Narrate decisions: *"I'll use a TreeMap since we need ordered keys with O(log n) ops;
a HashMap loses ordering."*

---

## Choosing the Right Structure — Cheat Sheet

| Need | Use | Ops |
|------|-----|-----|
| Fast lookup by key | HashMap / HashSet | O(1) avg |
| Ordered keys, range queries | TreeMap (Red-Black tree) | O(log n) |
| FIFO processing | ArrayDeque as queue | O(1) |
| LIFO / undo / matching pairs | ArrayDeque as stack | O(1) |
| Always need min/max | PriorityQueue (heap) | O(log n) insert, O(1) peek |
| Prefix matching | Trie | O(word length) |
| Dynamic connectivity | Union-Find | ~O(1) amortized (α(n)) |
| Range sum/min with updates | Segment tree / Fenwick | O(log n) |
| Kth smallest / top-K stream | Heap of size K | O(n log K) |
| Cycle detection, ordering with dependencies | Graph + DFS / topo sort | O(V+E) |

---

## Folder Index

```
dsa-roadmap/
├── README.md                      ← you are here
├── 01-complexity-analysis/       (README.md + ComplexityDemo.java — same pattern in every folder)
├── 02-arrays-strings/
├── 03-linked-lists/
├── 04-stacks-queues/
├── 05-hashing/
├── 06-trees-bst/
├── 07-heaps-priority-queues/
├── 08-tries/
├── 09-graphs/
├── 10-sorting-searching/
├── 11-recursion-backtracking/
├── 12-dynamic-programming/
├── 13-greedy/
├── 14-two-pointers-sliding-window/
├── 15-bit-manipulation/
└── 16-advanced-structures/
```

Each folder has a `README.md` with everything for that topic plus a runnable
`*Demo.java` (all 16 verified to compile and run on Java 11+).
