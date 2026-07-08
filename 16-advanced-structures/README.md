# 16 — Advanced Structures & Patterns

## Why It Matters

These topics show up in senior loops and "design a data structure" rounds:
Union-Find (graph problems' secret weapon), segment/Fenwick trees (range queries),
intervals (calendar/booking systems), and LFU cache (design). Each connects
directly to production systems.

---

## 1. Union-Find (Disjoint Set Union)

Tracks which elements belong to which group, with near-O(1) `union` and `find`.

### Real-life analogy
**Merging friend circles**: when someone from circle A befriends someone from
circle B, the circles merge. "Are Alice and Bob in the same circle?" = `find(a) ==
find(b)`. Production: network connectivity, Kruskal's MST, image segmentation
(connected pixels), deduplicating accounts (same email/phone → same person —
real entity-resolution pipelines use exactly this).

```java
class UnionFind {
    private final int[] parent, rank;
    private int components;

    UnionFind(int n) {
        parent = new int[n];
        rank = new int[n];
        components = n;
        for (int i = 0; i < n; i++) parent[i] = i;
    }

    int find(int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]];       // path compression (halving)
            x = parent[x];
        }
        return x;
    }

    boolean union(int a, int b) {                // returns false if already connected
        int ra = find(a), rb = find(b);
        if (ra == rb) return false;
        if (rank[ra] < rank[rb]) { int t = ra; ra = rb; rb = t; }
        parent[rb] = ra;                         // attach smaller rank under larger
        if (rank[ra] == rank[rb]) rank[ra]++;
        components--;
        return true;
    }

    boolean connected(int a, int b) { return find(a) == find(b); }
    int count() { return components; }
}
```

With path compression + union by rank: amortized **O(α(n))** per op — inverse
Ackermann, ≤ 4 for any realistic n. Quoting α(n) correctly is a senior flex.

Classic uses: Number of Connected Components, Graph Valid Tree (n-1 edges + fully
connected), Redundant Connection (first edge whose union returns false = the cycle
maker), Accounts Merge, Number of Islands II (dynamic islands).

---

## 2. Range Queries — Fenwick & Segment Trees

Problem: array with **both** point updates and range queries. Prefix sums give
O(1) queries but O(n) updates; these trees give O(log n) for both.

### Real-life analogy
**Live leaderboard**: scores change constantly (updates) while users ask "total
points of ranks 100–200" (range query). Also: financial tickers ("max price in any
window"), analytics dashboards, order-statistics ("how many players below score X").

### Fenwick Tree (Binary Indexed Tree) — prefix sums, tiny code
```java
class Fenwick {
    private final long[] tree;                   // 1-indexed

    Fenwick(int n) { tree = new long[n + 1]; }

    void update(int i, long delta) {             // add delta at index i (1-based)
        for (; i < tree.length; i += i & (-i))   // jump to next responsible node
            tree[i] += delta;
    }

    long prefixSum(int i) {                      // sum of [1..i]
        long s = 0;
        for (; i > 0; i -= i & (-i))             // strip lowest set bit
            s += tree[i];
        return s;
    }

    long rangeSum(int l, int r) { return prefixSum(r) - prefixSum(l - 1); }
}
```

The `i & (-i)` (lowest set bit) trick from folder 15 is the whole mechanism.
Fenwick: sums/invertible ops only, ~10 lines. Segment tree: any associative op
(min/max/gcd), supports lazy propagation for **range updates**, more code.
Interview rule: Fenwick if sums suffice; segment tree if min/max or range-update needed.

### Segment tree (array-based, range minimum)
```java
class SegmentTree {
    private final int n;
    private final int[] tree;                    // tree[1] = root; children 2i, 2i+1

    SegmentTree(int[] a) {
        n = a.length;
        tree = new int[2 * n];
        System.arraycopy(a, 0, tree, n, n);      // leaves at [n, 2n)
        for (int i = n - 1; i >= 1; i--)
            tree[i] = Math.min(tree[2 * i], tree[2 * i + 1]);
    }

    void update(int i, int value) {
        i += n;
        tree[i] = value;
        for (i /= 2; i >= 1; i /= 2)
            tree[i] = Math.min(tree[2 * i], tree[2 * i + 1]);
    }

    int rangeMin(int l, int r) {                 // [l, r)
        int best = Integer.MAX_VALUE;
        for (l += n, r += n; l < r; l /= 2, r /= 2) {
            if ((l & 1) == 1) best = Math.min(best, tree[l++]);
            if ((r & 1) == 1) best = Math.min(best, tree[--r]);
        }
        return best;
    }
}
```

---

## 3. Intervals (a pattern family every senior loop touches)

**Real-life:** calendar apps, meeting-room booking, CPU task scheduling, hotel
reservations, IP range allocation, video buffering ranges.

The three moves:
1. **Sort by start** (usually), then sweep.
2. Overlap test: `a.start <= b.end && b.start <= a.end` — memorize it.
3. Merge: `newEnd = max(endA, endB)`.

```java
// Merge Intervals — O(n log n)
int[][] merge(int[][] intervals) {
    Arrays.sort(intervals, Comparator.comparingInt(i -> i[0]));
    List<int[]> merged = new ArrayList<>();
    int[] current = intervals[0];
    for (int i = 1; i < intervals.length; i++) {
        if (intervals[i][0] <= current[1])       // overlaps (touching counts here)
            current[1] = Math.max(current[1], intervals[i][1]);
        else {
            merged.add(current);
            current = intervals[i];
        }
    }
    merged.add(current);
    return merged.toArray(new int[0][]);
}
```

**Sweep line** (the generalization): convert intervals to +1/-1 events, sort, scan
keeping a running count — max concurrent meetings, flight bookings (difference
array), skyline problem (with a heap). This "events + running state" idea scales
to real telemetry processing.

```java
// My Calendar (booking without double-book) — TreeMap floor/ceiling
class MyCalendar {
    private final TreeMap<Integer, Integer> bookings = new TreeMap<>(); // start → end

    boolean book(int start, int end) {           // [start, end)
        Map.Entry<Integer, Integer> before = bookings.floorEntry(start);
        if (before != null && before.getValue() > start) return false;
        Map.Entry<Integer, Integer> after = bookings.ceilingEntry(start);
        if (after != null && after.getKey() < end) return false;
        bookings.put(start, end);
        return true;
    }
}
```

---

## 4. LFU Cache (design; the hard sibling of LRU)

Evict the **least frequently** used; ties broken by least recent. O(1) all ops:
key→node map + freq→doubly-linked-list map + minFreq tracker.

```java
class LFUCache {
    private final int capacity;
    private final Map<Integer, int[]> cache = new HashMap<>();          // key → [value, freq]
    private final Map<Integer, LinkedHashSet<Integer>> freqKeys = new HashMap<>(); // freq → keys (LRU order)
    private int minFreq = 0;

    LFUCache(int capacity) { this.capacity = capacity; }

    public int get(int key) {
        if (!cache.containsKey(key)) return -1;
        touch(key);
        return cache.get(key)[0];
    }

    public void put(int key, int value) {
        if (capacity == 0) return;
        if (cache.containsKey(key)) {
            cache.get(key)[0] = value;
            touch(key);
            return;
        }
        if (cache.size() == capacity) {                                  // evict LFU (LRU tiebreak)
            int evict = freqKeys.get(minFreq).iterator().next();         // oldest at minFreq
            freqKeys.get(minFreq).remove(evict);
            cache.remove(evict);
        }
        cache.put(key, new int[]{value, 1});
        freqKeys.computeIfAbsent(1, f -> new LinkedHashSet<>()).add(key);
        minFreq = 1;
    }

    private void touch(int key) {
        int freq = cache.get(key)[1];
        cache.get(key)[1] = freq + 1;
        freqKeys.get(freq).remove(key);
        if (freqKeys.get(freq).isEmpty() && freq == minFreq) minFreq++;
        freqKeys.computeIfAbsent(freq + 1, f -> new LinkedHashSet<>()).add(key);
    }
}
```

**Real-life:** CDN edge caches and database buffer pools use frequency-aware
eviction (LFU variants like TinyLFU in Caffeine — the JVM's best caching library).

---

## 5. Honorable Mentions (know the concept + one use case)

| Structure | One-liner | Where it lives |
|-----------|-----------|----------------|
| Skip list | probabilistic sorted list, O(log n) | Redis sorted sets (ZSET) |
| Bloom filter | "maybe present / definitely absent" in tiny space | Cassandra, HBase, Chrome safe-browsing |
| Count-Min Sketch | approximate frequency counts of a stream | trending topics, heavy hitters |
| HyperLogLog | approximate distinct count in KBs | Redis PFCOUNT, analytics uniques |
| Consistent hashing ring | stable key→server mapping | DynamoDB, Cassandra, CDNs |
| Merkle tree | hash tree for diffing datasets | git, blockchain, Dynamo anti-entropy |
| Suffix array | all substrings, search-friendly | full-text indexes, bioinformatics |

For a senior role, one crisp sentence + one real user per structure is worth more
than implementation details.

## 6. Interview Pitfalls

- Union-Find without path compression → O(n) chains; always add it (2 lines).
- Fenwick is 1-indexed — off-by-one bugs everywhere; wrap it in a class.
- Interval overlap boundary semantics: does touching count? ASK.
- LFU: forgetting the recency tiebreak within a frequency bucket.
- Sweep line: process end events before start events at the same coordinate
  (or after — depends on whether touching intervals overlap; be explicit).

## Must-Solve List
1. Number of Connected Components / Graph Valid Tree (Union-Find)
2. Redundant Connection / Accounts Merge
3. Merge Intervals / Insert Interval / Non-overlapping Intervals
4. Meeting Rooms I & II
5. My Calendar I–III
6. Range Sum Query — Mutable (Fenwick/segment tree)
7. Count of Smaller Numbers After Self (Fenwick alternative)
8. LFU Cache
9. The Skyline Problem (hard — sweep line + heap)
10. Design Twitter / Design Browser History (structure composition)

---

## Deep Dive & Worked Examples

> Runnable examples: `AdvancedDemo.java` in this folder — friend circles merging with redundant-edge detection, a Fenwick-tree leaderboard with live updates, and merge-intervals + sweep-line peak concurrency.

### Union-Find: watching the forest evolve

People {Ann, Ben, Cat, Dan, Eli, Fay}, friendships arriving in order:

```
init      : {Ann} {Ben} {Cat} {Dan} {Eli} {Fay}     6 components
Ann+Ben   : {Ann,Ben} {Cat} {Dan} {Eli} {Fay}       5
Cat+Dan   : {Ann,Ben} {Cat,Dan} {Eli} {Fay}         4
Eli+Fay   : {Ann,Ben} {Cat,Dan} {Eli,Fay}           3
Ben+Cat   : {Ann,Ben,Cat,Dan} {Eli,Fay}             2
Ann+Dan   : union returns FALSE — already connected  2  ← cycle edge!
```

That final `false` is the workhorse: in Redundant Connection it identifies the edge
to remove; in Kruskal's MST it rejects cycle-forming edges; in Graph Valid Tree it
proves the graph has a cycle. One boolean, three famous problems.

**Why path compression + union by rank gives α(n):** compression flattens chains
as a side effect of finds (each find makes future finds cheaper — self-optimizing);
rank keeps merge trees shallow. Either alone gives O(log n) amortized; together,
inverse-Ackermann α(n) ≤ 4 for any n that fits in the universe. Quote it, don't
prove it.

### Fenwick tree: what `i & (-i)` is actually doing

`tree[i]` is responsible for a range of length `lowbit(i) = i & (-i)` ending at i:

```
index : 1    2    3    4    5    6    7    8
covers: [1]  [1,2] [3]  [1,4] [5]  [5,6] [7]  [1,8]
```

`prefixSum(7)` = tree[7] + tree[6] + tree[4] — strip the low bit each time
(7→6→4→0): three node reads cover [7]+[5,6]+[1,4] = [1,7] ✓. `update(3)` adds the
delta to tree[3], tree[4], tree[8] (add the low bit: 3→4→8) — every responsible
node. Both walks touch ≤ log n nodes. Ten lines of code, and the entire mechanism
is the folder-15 lowbit trick — say that connection out loud.

### Sweep line: one idea, many hard problems

Convert intervals to events: (start, +1), (end, −1). Sort; scan with a running
counter. The demo finds "peak simultaneous meetings" this way. The same skeleton:

| Problem | Event payload | Running state |
|---------|--------------|----------------|
| Meeting Rooms II | ±1 | current meetings (max = rooms) |
| Employee Free Time | ±1 | zero-count gaps = free slots |
| Skyline | building height | max-heap of active heights |
| My Calendar III | ±1 in a TreeMap | max overlap (K-booking) |
| Points covered by intervals | ±1 (difference array) | coverage count |

Tie-break rule to state explicitly: at equal coordinates, process ends before
starts if touching intervals DON'T overlap ([1,3] and [3,5] compatible), starts
first if they do. Half the bugs in interval problems live in this line.

### LFU vs LRU vs production reality (design discussion ammo)

LRU: recency only — one scan of a big table evicts your whole hot set ("cache
pollution"). LFU: frequency — but old popular items linger after going cold
("cache ossification"), and naive counts never decay. Production answer:
**TinyLFU** (Caffeine): a Count-Min Sketch estimates frequency in tiny memory with
periodic halving (decay), gating admission to a small LRU window + main segmented
LRU. If a system-design interviewer asks "LRU or LFU?", the winning answer is
"here's the failure mode of each, here's the hybrid real caches use."

---

## You've Reached the End of the Roadmap

Suggested final 2 weeks before interviews:
- Redo every problem you ever failed, cold.
- 6–8 full mock interviews (timed, out loud — this is non-negotiable).
- Re-read every folder's **Pitfalls** section the night before.
- For senior loops: be ready to connect each structure to a system you've built —
  "I used exactly this pattern when..." stories score double.
