# 07 — Heaps & Priority Queues

## Why It Matters

Whenever a problem says "K largest", "K closest", "most frequent", "merge sorted
streams", or "schedule by priority" — the answer is a heap. Top-K questions are
among the most reused interview questions in the industry.

---

## 1. What a Heap Is

A **binary heap** is a complete binary tree stored in an array, satisfying the heap
property: every parent ≤ its children (min-heap) or ≥ (max-heap). Only the ROOT is
guaranteed extreme — a heap is NOT sorted.

Array representation (no pointers needed, cache friendly):
- parent of `i` → `(i-1)/2`
- children of `i` → `2i+1`, `2i+2`

### Real-life analogy
A **hospital emergency room**. Patients aren't treated in arrival order (queue) but
by severity (priority). The triage board always knows who's most critical (peek
O(1)); admitting or discharging re-sorts only along one path (O(log n)) rather
than the whole board.

### Production use cases
- OS/CPU schedulers, `ScheduledThreadPoolExecutor`'s delayed-work queue.
- Dijkstra / A* / Prim (folder 09).
- K-way merge in LSM-tree compaction and external (disk) sorting.
- Rate-limited job queues, timer wheels' simpler cousins.

| Operation | Cost | How |
|-----------|------|-----|
| peek (min/max) | O(1) | root |
| offer (insert) | O(log n) | append, sift up |
| poll (remove root) | O(log n) | move last to root, sift down |
| build heap from n items | **O(n)** | heapify bottom-up (not n log n — classic question!) |
| search arbitrary element | O(n) | no order across siblings |

---

## 2. Java PriorityQueue

```java
Queue<Integer> minHeap = new PriorityQueue<>();
Queue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());

// Custom priority — e.g., tasks by (priority, then timestamp)
record Task(int priority, long timestamp) {}
Queue<Task> tasks = new PriorityQueue<>(
        Comparator.comparingInt(Task::priority)
                  .thenComparingLong(Task::timestamp));
```

Gotchas: iteration order is NOT sorted; `remove(Object)` is O(n); not thread-safe
(use `PriorityBlockingQueue` for concurrency).

---

## 3. Build-Your-Own MinHeap (asked at senior loops)

```java
class MinHeap {
    private final List<Integer> a = new ArrayList<>();

    int peek() { return a.get(0); }

    void offer(int v) {
        a.add(v);
        int i = a.size() - 1;
        while (i > 0) {                              // sift up
            int parent = (i - 1) / 2;
            if (a.get(parent) <= a.get(i)) break;
            Collections.swap(a, parent, i);
            i = parent;
        }
    }

    int poll() {
        int top = a.get(0);
        int last = a.remove(a.size() - 1);
        if (!a.isEmpty()) {
            a.set(0, last);
            siftDown(0);
        }
        return top;
    }

    private void siftDown(int i) {
        int n = a.size();
        while (true) {
            int l = 2 * i + 1, r = 2 * i + 2, smallest = i;
            if (l < n && a.get(l) < a.get(smallest)) smallest = l;
            if (r < n && a.get(r) < a.get(smallest)) smallest = r;
            if (smallest == i) break;
            Collections.swap(a, i, smallest);
            i = smallest;
        }
    }
}
```

---

## 4. The Big Three Patterns

### Pattern A — Top-K with a size-K heap (opposite-polarity trick)

K **largest** → keep a **min**-heap of size K (evict the smallest of the elite).
O(n log K) time, O(K) space — crucial when n is a huge stream and K is small.

**Real-life:** a leaderboard showing top 10 of 50 million players — you never sort
50M rows; you keep a 10-element min-heap per update.

```java
int findKthLargest(int[] nums, int k) {
    Queue<Integer> minHeap = new PriorityQueue<>();
    for (int n : nums) {
        minHeap.offer(n);
        if (minHeap.size() > k) minHeap.poll();   // evict smallest
    }
    return minHeap.peek();
}
```

(Alternative for Kth largest: QuickSelect, O(n) average — see folder 10. Comparing
the two unprompted is a strong senior move.)

### Pattern B — K-way merge

**Real-life:** merging sorted log files from many servers into one timeline
(exactly what log aggregators and LSM compaction do).

```java
// Merge K sorted lists — O(N log k), N = total nodes
ListNode mergeKLists(ListNode[] lists) {
    Queue<ListNode> heap = new PriorityQueue<>(Comparator.comparingInt(n -> n.val));
    for (ListNode l : lists) if (l != null) heap.offer(l);

    ListNode dummy = new ListNode(0), tail = dummy;
    while (!heap.isEmpty()) {
        ListNode min = heap.poll();
        tail.next = min; tail = min;
        if (min.next != null) heap.offer(min.next);
    }
    return dummy.next;
}
```

### Pattern C — Two heaps (running median)

**Real-life:** live median latency on a metrics dashboard as requests stream in.
Max-heap holds the lower half, min-heap the upper half; keep sizes within 1.

```java
class MedianFinder {
    private final Queue<Integer> lo = new PriorityQueue<>(Comparator.reverseOrder()); // lower half
    private final Queue<Integer> hi = new PriorityQueue<>();                          // upper half

    void addNum(int num) {
        lo.offer(num);
        hi.offer(lo.poll());                     // ensure every lo ≤ every hi
        if (hi.size() > lo.size()) lo.offer(hi.poll());  // rebalance sizes
    }

    double findMedian() {
        return lo.size() > hi.size() ? lo.peek() : (lo.peek() + hi.peek()) / 2.0;
    }
}
```

---

## 5. Scheduling Classic — Task Scheduler / Meeting Rooms II

```java
// Meeting Rooms II — min rooms for overlapping meetings
// Sort by start; min-heap of end times = rooms in use
int minMeetingRooms(int[][] intervals) {
    Arrays.sort(intervals, Comparator.comparingInt(i -> i[0]));
    Queue<Integer> endTimes = new PriorityQueue<>();
    for (int[] meeting : intervals) {
        if (!endTimes.isEmpty() && endTimes.peek() <= meeting[0])
            endTimes.poll();                     // a room frees up — reuse it
        endTimes.offer(meeting[1]);
    }
    return endTimes.size();
}
```

**Real-life:** literally conference-room booking; also: minimum servers for
overlapping jobs, gate assignment at airports.

---

## 6. Interview Pitfalls

- Heap ≠ sorted: only the root is guaranteed; don't iterate expecting order.
- Wrong polarity: K largest needs a MIN-heap of size K (think "evict the weakest").
- `PriorityQueue.remove(Object)` is O(n) — for "delete arbitrary" use lazy deletion
  (keep a counter/set of stale entries, skip them on poll) or an indexed heap.
- Comparator inconsistency with equals can cause subtle bugs in de-dup logic.
- Building a heap of n items by n offers is O(n log n); heapify is O(n) — know why
  (most nodes are near the leaves and sift down only a little).
- Integer overflow in comparators: `(a, b) -> a - b` overflows; use `Integer.compare(a, b)`.

## Must-Solve List
1. Kth Largest Element (heap AND QuickSelect)
2. Top K Frequent Elements / Words
3. K Closest Points to Origin
4. Merge K Sorted Lists
5. Find Median from Data Stream
6. Meeting Rooms II
7. Task Scheduler
8. Reorganize String (greedy + max-heap)
9. Kth Smallest in Sorted Matrix
10. Design Twitter (heap-based feed merge — mini system design)

---

## Deep Dive & Worked Examples

> Runnable examples: `HeapsDemo.java` in this folder — offer/poll traces on a hand-built heap, streaming Kth-largest, running median, and Meeting Rooms II.

### Dry run: offering 7, 3, 9, 1, 5 into a min-heap

Array view (children of i at 2i+1, 2i+2):

```
offer 7: [7]
offer 3: [7,3] → 3 < parent 7 → swap → [3,7]
offer 9: [3,7,9]                        (9 ≥ 3, stays)
offer 1: [3,7,9,1] → 1 < 7 swap → [3,1,9,7] → 1 < 3 swap → [1,3,9,7]
offer 5: [1,3,9,7,5] → 5 ≥ 3, stays
```

Tree view of final state:
```
        1
       / \
      3   9
     / \
    7   5
```
Only the root is globally guaranteed. 5 sits below 3 but beside 9 — the heap is NOT
sorted; siblings have no relationship. Poll order (1,3,5,7,9) IS sorted — that's heapsort.

### Why heapify is O(n), not O(n log n) — the honest argument

Building by n offers: each sift-up can climb the full height → O(n log n).
Heapify (sift-down from the last parent upward): a node's sift-down cost is its
height above the leaves. Half the nodes are leaves (height 0, free), a quarter at
height 1, an eighth at height 2... Total = Σ (n/2^(h+1))·h = n·Σ h/2^(h+1) < n·1 = O(n).
The geometric decay eats the log. `PriorityQueue(Collection)` uses heapify — prefer
it over n offers when you have all the data.

### The two-heap median: invariants that make it work

1. `lo` (max-heap) holds the smaller half, `hi` (min-heap) the larger half.
2. Every element of lo ≤ every element of hi — enforced by always inserting into lo
   then moving lo's max to hi (the "wash" step).
3. |size difference| ≤ 1, lo gets the extra — enforced by the rebalance step.

Median = lo.peek() (odd count) or average of both peeks (even). Each insert: O(log n).
Follow-up: *"median of a sliding window?"* — now you need delete-arbitrary; use lazy
deletion with a delayed-removal HashMap, or an order-statistics tree. Hard but asked.

### Top-K polarity: a table to burn in

| Want | Keep a heap of size K that is a... | Evict when |
|------|-----------------------------------|------------|
| K largest | MIN-heap ("weakest of the elite falls out") | size > K |
| K smallest | MAX-heap | size > K |
| K most frequent | MIN-heap by frequency | size > K |

If you catch yourself building a max-heap of ALL n elements for K largest, you've
spent O(n) memory and O(n log n) time where O(n log K)/O(K) sufficed — and lost the
"works on streams" property that interviewers often ask about next.

### Interviewer follow-ups you should expect

- *"Kth largest: heap vs QuickSelect vs sorting?"* Sort O(n log n): simplest, mutates.
  QuickSelect O(n) avg: fastest, needs all data in memory, O(n²) worst. Heap O(n log K):
  streaming-friendly, K memory. Choose by: is it a stream? is K small? can I mutate?
- *"Merge 1000 sorted files of 1GB each with 8GB RAM"* — external K-way merge: heap
  of (currentValue, fileIndex), read files in buffered chunks. This is exactly
  LSM-tree compaction and classic MapReduce shuffle.
- *"PriorityQueue with updatable priorities?"* Java's PQ can't (remove is O(n)).
  Options: lazy deletion (push the new priority, skip stale entries on poll — used
  in the Dijkstra code in folder 09), or an indexed heap (position map + sift both ways).
- *"Why is peek O(1) but contains O(n)?"* Heap order is only root-to-leaf; horizontal
  order is arbitrary → search must scan.

**Next:** `08-tries`
