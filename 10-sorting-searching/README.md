# 10 — Sorting & Searching

## Why It Matters

You'll rarely implement quicksort in an interview, but you WILL: justify which sort
to use, exploit sortedness with binary search, and — the senior differentiator —
apply **binary search on the answer space**. Also: "sort first" simplifies dozens
of problems (intervals, two pointers, greedy).

---

## 1. Sorting Algorithms — the Landscape

| Algorithm | Best | Avg | Worst | Space | Stable | Notes |
|-----------|------|-----|-------|-------|--------|-------|
| Bubble/Insertion | n | n² | n² | O(1) | yes | insertion is great for nearly-sorted/small |
| Selection | n² | n² | n² | O(1) | no | minimal swaps |
| Merge sort | n log n | n log n | n log n | O(n) | **yes** | predictable; external sorting |
| Quick sort | n log n | n log n | n² | O(log n) | no | fastest in practice, cache friendly |
| Heap sort | n log n | n log n | n log n | O(1) | no | guaranteed, in-place, poor cache use |
| Counting sort | n+k | n+k | n+k | O(k) | yes | ints in small range |
| Radix sort | d(n+k) | d(n+k) | d(n+k) | O(n+k) | yes | fixed-width keys |
| TimSort | n | n log n | n log n | O(n) | yes | Java objects; exploits existing runs |

### What Java actually does (senior must-know)
- `Arrays.sort(int[])` → **dual-pivot quicksort** (primitives; instability invisible).
- `Arrays.sort(Object[])` / `Collections.sort` → **TimSort** (stability REQUIRED for
  objects — sorting people by name then by age must preserve name order within age).
- `Arrays.parallelSort` → parallel merge sort for large arrays.

### Why stability matters — real-life
An airline sorts passengers by boarding group, then by seat row. With a stable
sort, passengers in the same row keep their boarding-group order. Databases use
stable sorts for multi-key ORDER BY.

### Merge sort (implement fluently — basis for many problems)
```java
void mergeSort(int[] a, int lo, int hi) {
    if (hi - lo <= 1) return;
    int mid = (lo + hi) / 2;
    mergeSort(a, lo, mid);
    mergeSort(a, mid, hi);
    merge(a, lo, mid, hi);
}
private void merge(int[] a, int lo, int mid, int hi) {
    int[] tmp = new int[hi - lo];
    int i = lo, j = mid, k = 0;
    while (i < mid && j < hi) tmp[k++] = a[i] <= a[j] ? a[i++] : a[j++];
    while (i < mid) tmp[k++] = a[i++];
    while (j < hi)  tmp[k++] = a[j++];
    System.arraycopy(tmp, 0, a, lo, tmp.length);
}
```

Merge sort's structure solves: Count Inversions, Count of Smaller Numbers After
Self, Sort a Linked List (O(1) extra space possible!). **External sorting** — data
too big for RAM — is merge sort over disk chunks; how databases sort terabytes.

### QuickSelect — Kth smallest in O(n) average
```java
// Kth largest via QuickSelect (average O(n), worst O(n²); shuffle to protect)
int quickSelect(int[] a, int k) {                // k-th largest → index n-k of sorted
    int lo = 0, hi = a.length - 1, target = a.length - k;
    while (lo < hi) {
        int p = partition(a, lo, hi);
        if (p == target) return a[p];
        if (p < target) lo = p + 1; else hi = p - 1;
    }
    return a[lo];
}
private int partition(int[] a, int lo, int hi) {
    int pivot = a[hi], i = lo;
    for (int j = lo; j < hi; j++)
        if (a[j] <= pivot) { int t = a[i]; a[i] = a[j]; a[j] = t; i++; }
    int t = a[i]; a[i] = a[hi]; a[hi] = t;
    return i;
}
```

Contrast with heap approach (folder 07): heap O(n log K) but streaming-friendly;
QuickSelect O(n) but needs all data in memory and mutates it. Trade-off talk = senior points.

---

## 2. Binary Search — Beyond the Basics

The bar for seniors: write it bug-free first try, and generalize it to
**predicate search**: find the boundary where a monotonic condition flips.

### The universal template (lower bound)
```java
// First index where predicate is true (predicate must be monotonic F...FT...T)
int lowerBound(int lo, int hi, IntPredicate isTrue) {
    while (lo < hi) {
        int mid = lo + (hi - lo) / 2;
        if (isTrue.test(mid)) hi = mid;          // mid might be the answer — keep it
        else lo = mid + 1;                       // mid is definitely not — skip it
    }
    return lo;                                   // first true (== original hi if none)
}
```

Everything reduces to this: first occurrence (`a[mid] >= target`), last occurrence
(first `a[mid] > target`, minus 1), insertion point, floor/ceiling.

### Rotated sorted array (high-frequency)
```java
// Search in Rotated Sorted Array — one half is always sorted; check which
int search(int[] a, int target) {
    int lo = 0, hi = a.length - 1;
    while (lo <= hi) {
        int mid = lo + (hi - lo) / 2;
        if (a[mid] == target) return mid;
        if (a[lo] <= a[mid]) {                   // left half sorted
            if (a[lo] <= target && target < a[mid]) hi = mid - 1;
            else lo = mid + 1;
        } else {                                 // right half sorted
            if (a[mid] < target && target <= a[hi]) lo = mid + 1;
            else hi = mid - 1;
        }
    }
    return -1;
}
```

---

## 3. Binary Search on the Answer (the senior pattern)

When the answer itself lies in a numeric range and feasibility is **monotonic**
("if speed s works, any s' > s works"), binary-search the answer and write a
`feasible()` checker.

### Real-life framing
Capacity planning: "What's the minimum number of servers that keeps p99 latency
under 200ms?" You don't derive it — you binary search over server counts, load-testing
(feasibility check) each guess.

```java
// Koko Eating Bananas — min eating speed to finish piles within h hours
int minEatingSpeed(int[] piles, int h) {
    int lo = 1, hi = Arrays.stream(piles).max().getAsInt();
    while (lo < hi) {
        int mid = lo + (hi - lo) / 2;
        if (canFinish(piles, mid, h)) hi = mid;  // feasible → try slower
        else lo = mid + 1;                       // infeasible → must go faster
    }
    return lo;
}
private boolean canFinish(int[] piles, int speed, int h) {
    long hours = 0;
    for (int p : piles) hours += (p + speed - 1) / speed;  // ceil division
    return hours <= h;
}
```

Same skeleton solves: Capacity to Ship Packages in D Days, Split Array Largest Sum,
Minimum Days to Make Bouquets, Aggressive Cows / Magnetic Force Between Balls.
Recognizing "minimize the maximum / maximize the minimum" → binary search on answer
is one of the strongest signals you can send.

### Median of Two Sorted Arrays (hard, iconic)
Binary search the **partition point** of the smaller array so left halves contain
exactly half the elements and every left element ≤ every right element. O(log(min(m,n))).
Understand the invariant even if you memorize the code.

---

## 4. Complexity Summary

| Task | Approach | Cost |
|------|----------|------|
| Sort n elements (comparison) | merge/quick/heap | O(n log n) — proven lower bound |
| Sort small-range ints | counting sort | O(n + k) |
| Find in sorted array | binary search | O(log n) |
| Kth smallest | QuickSelect / heap | O(n) avg / O(n log k) |
| Min in answer-range R with O(n) check | BS on answer | O(n log R) |

## 5. Interview Pitfalls

- `(lo + hi) / 2` overflow → `lo + (hi - lo) / 2`. Always.
- Infinite loops: with `lo < hi` and `hi = mid`, mid must round DOWN; if you use
  `lo = mid`, round UP (`mid = lo + (hi - lo + 1) / 2`). Off-by-one here is the
  most common binary search bug.
- Deciding `<=` vs `<` in the loop condition: pick one template and drill it.
- Claiming quicksort is always O(n log n) — worst case n²; Java's introsort-style
  defenses; killer-input anecdotes.
- Sorting when a heap/QuickSelect suffices ("top K" doesn't need full sort).
- Using unstable sort where stability is semantically required.

## Must-Solve List
1. Binary Search / First & Last Position / Search Insert Position
2. Search in Rotated Sorted Array I & II + Find Minimum in Rotated Array
3. Koko Eating Bananas / Ship Packages / Split Array Largest Sum
4. Kth Largest Element (QuickSelect)
5. Merge Intervals (sort-based; folder 16 details intervals)
6. Sort List (merge sort on linked list)
7. Count of Smaller Numbers After Self (merge-sort counting — hard)
8. Median of Two Sorted Arrays (hard)
9. Find Peak Element (binary search on unsorted-but-structured data)
10. Sort Colors (counting / Dutch flag)

---

## Deep Dive & Worked Examples

> Runnable examples: `SortingSearchingDemo.java` in this folder — merge sort trace, boundary binary searches, QuickSelect partition steps, and Koko's feasibility search.

### Dry run: lowerBound on [1, 2, 4, 4, 4, 4, 7, 9], target 4

Predicate: `a[mid] >= 4`. Range [lo, hi) = [0, 8).

| lo | hi | mid | a[mid] | a[mid] ≥ 4? | action |
|----|----|-----|--------|-------------|--------|
| 0 | 8 | 4 | 4 | yes | hi = 4 (mid could be the answer — keep it in range) |
| 0 | 4 | 2 | 4 | yes | hi = 2 |
| 0 | 2 | 1 | 2 | no | lo = 2 (mid ruled out — skip past it) |
| 2 | 2 | — | — | — | loop ends, return 2 ✓ (first 4 is at index 2) |

The asymmetry (`hi = mid` vs `lo = mid + 1`) is THE thing to internalize: the side
that might still contain the answer keeps mid; the side that provably doesn't skips
it. With `lo < hi` and mid rounding down, this never infinite-loops. Every binary
search you'll ever write is this template with a different predicate.

### The predicate view unifies everything

| Problem | Predicate (monotonic F...FT...T) | Answer |
|---------|----------------------------------|--------|
| first occurrence of x | a[i] ≥ x | first true |
| last occurrence of x | a[i] > x | first true − 1 |
| insert position | a[i] ≥ x | first true |
| sqrt(n) floor | i·i > n | first true − 1 |
| Koko's min speed | canFinish(speed) | first true |
| min capacity to ship in D days | canShip(capacity) | first true |
| first bad version | isBad(v) | first true |

Rewriting a problem as "find the boundary of a monotonic predicate" is the whole
skill. If the predicate isn't monotonic, binary search is invalid — check before
you code (interviewers plant non-monotonic traps).

### Why is the comparison-sort floor n log n? (60-second proof sketch)

A comparison sort must distinguish all n! input orders. Each comparison has 2
outcomes → k comparisons distinguish ≤ 2^k orders. Need 2^k ≥ n! → k ≥ log₂(n!) ≈
n log n (Stirling). Counting/radix sorts beat it by NOT comparing — they inspect
digit/value structure. This is a real interview question at senior loops.

### TimSort: why Java objects sort differently (senior nugget)

TimSort scans for existing sorted "runs" (ascending or descending), merges them with
galloping mode. On nearly-sorted data — extremely common in practice (append-mostly
tables, log files) — it approaches O(n). It's stable, which `Collections.sort`
REQUIRES for the contract "equal elements keep their order". Primitives use
dual-pivot quicksort instead because stability is meaningless for `int` and quicksort
is faster in-cache. Knowing WHY the two differ is a classic Java-depth probe.

### Interviewer follow-ups you should expect

- *"Search in rotated array WITH duplicates?"* When a[lo] == a[mid] == a[hi] you
  can't tell which half is sorted — shrink both ends (lo++, hi--) → worst case O(n).
  Stating the degraded bound honestly is the expected answer.
- *"Find Peak Element: array isn't sorted — why does binary search work?"* The
  predicate `a[mid] > a[mid+1]` is a valid boundary detector: on the "false" side a
  peak must exist uphill. Binary search needs monotonic *predicate*, not sorted data.
- *"QuickSelect worst case?"* O(n²) on adversarial pivots; shuffle first or
  median-of-medians for guaranteed O(n) (know it exists; don't code it).
- *"External sort 100GB with 1GB RAM?"* Sort 1GB chunks in memory (100 runs), then
  K-way merge with a heap and buffered reads. Folder 07's pattern.

**Next:** `11-recursion-backtracking`
