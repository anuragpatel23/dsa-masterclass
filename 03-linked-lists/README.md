# 03 — Linked Lists

## Why It Matters

Linked lists test whether you can manipulate pointers without losing references —
a proxy for careful, bug-free coding. Nearly every list question is solvable with
three tools: **dummy head, fast/slow pointers, and in-place reversal**.

---

## 1. Internals & Trade-offs

A linked list stores nodes anywhere in memory; each node holds a value + reference(s).

### Real-life analogy
A **treasure hunt**: each clue tells you where the next clue is. You can't jump to
clue #7 directly (no random access), but inserting a new clue mid-hunt is trivial —
just rewrite one clue (O(1) insert given the node). An array is a printed itinerary:
instant lookup, painful mid-edit.

| Operation | Array/ArrayList | LinkedList |
|-----------|----------------|------------|
| Access by index | O(1) | O(n) |
| Insert/delete at known node | O(n) (shift) | O(1) |
| Insert at front | O(n) | O(1) |
| Memory | compact, cache-friendly | +pointer overhead, cache-hostile |

**Senior trade-off answer:** in practice `ArrayList` beats `LinkedList` for almost
everything in Java because of cache locality and allocation overhead — even Joshua
Bloch says he barely uses `LinkedList`. Say this when asked "when would you use a
linked list?" then give the real uses: LRU caches (with HashMap), chaining in
HashMap buckets (pre-treeify), undo chains, allocator free-lists.

### Variants
- **Singly** — next only. Most interview problems.
- **Doubly** — prev + next. Needed for O(1) delete of arbitrary node (LRU cache).
- **Circular** — tail links to head. Round-robin schedulers, ring buffers.

---

## 2. The Node + Core Toolkit

```java
class ListNode {
    int val;
    ListNode next;
    ListNode(int val) { this.val = val; }
}
```

### Tool 1 — Dummy (sentinel) head
Eliminates the "is it the first node?" special case. Use in nearly every problem
that modifies a list.

```java
// Remove all nodes with a given value
ListNode removeElements(ListNode head, int val) {
    ListNode dummy = new ListNode(0);
    dummy.next = head;
    ListNode cur = dummy;
    while (cur.next != null) {
        if (cur.next.val == val) cur.next = cur.next.next;  // unlink
        else cur = cur.next;
    }
    return dummy.next;
}
```

### Tool 2 — Fast & slow pointers (Floyd)

**Real-life:** two runners on a circular track — if the track loops, the faster
runner inevitably laps the slower one.

```java
// Detect cycle — O(n) time, O(1) space
boolean hasCycle(ListNode head) {
    ListNode slow = head, fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
        if (slow == fast) return true;
    }
    return false;
}

// Find middle (slow lands on mid when fast hits end)
ListNode middle(ListNode head) {
    ListNode slow = head, fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next; fast = fast.next.next;
    }
    return slow;
}
```

Follow-up interviewers love: **where does the cycle start?** After meeting, reset
one pointer to head; advance both one step at a time; they meet at the cycle start
(provable with algebra — distance head→start equals meeting-point→start around the loop).

### Tool 3 — In-place reversal (must be automatic)

```java
// Iterative — O(n)/O(1). Practice until you can write it in 60 seconds.
ListNode reverse(ListNode head) {
    ListNode prev = null, cur = head;
    while (cur != null) {
        ListNode next = cur.next;   // save
        cur.next = prev;            // flip
        prev = cur;                 // advance prev
        cur = next;                 // advance cur
    }
    return prev;
}

// Recursive — elegant, O(n) stack space
ListNode reverseRec(ListNode head) {
    if (head == null || head.next == null) return head;
    ListNode newHead = reverseRec(head.next);
    head.next.next = head;          // make the next node point back to me
    head.next = null;
    return newHead;
}
```

---

## 3. Composite Classics (combine the tools)

```java
// Merge two sorted lists — the building block of merge sort & k-way merge
ListNode mergeTwoLists(ListNode a, ListNode b) {
    ListNode dummy = new ListNode(0), tail = dummy;
    while (a != null && b != null) {
        if (a.val <= b.val) { tail.next = a; a = a.next; }
        else                { tail.next = b; b = b.next; }
        tail = tail.next;
    }
    tail.next = (a != null) ? a : b;
    return dummy.next;
}

// Remove Nth node from end — one pass, gap-of-n pointers
ListNode removeNthFromEnd(ListNode head, int n) {
    ListNode dummy = new ListNode(0);
    dummy.next = head;
    ListNode fast = dummy, slow = dummy;
    for (int i = 0; i < n; i++) fast = fast.next;   // create gap of n
    while (fast.next != null) { fast = fast.next; slow = slow.next; }
    slow.next = slow.next.next;
    return dummy.next;
}

// Palindrome list — middle + reverse second half + compare, O(n)/O(1)
boolean isPalindrome(ListNode head) {
    ListNode mid = middle(head);
    ListNode second = reverse(mid);
    ListNode p1 = head, p2 = second;
    boolean ok = true;
    while (p2 != null) {
        if (p1.val != p2.val) { ok = false; break; }
        p1 = p1.next; p2 = p2.next;
    }
    return ok;   // mention: restore the list if interviewer cares about immutability
}
```

---

## 4. The Crown Jewel — LRU Cache (doubly linked list + HashMap)

**Real-life:** browser tabs on a memory-constrained phone — the least recently
used tab is killed first. Redis, CPU caches, and CDN edge caches all use LRU variants.

Why this design: HashMap gives O(1) key→node lookup; the doubly linked list keeps
recency order and allows O(1) unlink of any node (you need `prev`, hence doubly).

```java
class LRUCache {
    private class Node {
        int key, value;
        Node prev, next;
        Node(int k, int v) { key = k; value = v; }
    }

    private final int capacity;
    private final Map<Integer, Node> map = new HashMap<>();
    private final Node head = new Node(0, 0);   // sentinel: most recent side
    private final Node tail = new Node(0, 0);   // sentinel: least recent side

    LRUCache(int capacity) {
        this.capacity = capacity;
        head.next = tail;
        tail.prev = head;
    }

    public int get(int key) {
        Node n = map.get(key);
        if (n == null) return -1;
        moveToFront(n);
        return n.value;
    }

    public void put(int key, int value) {
        Node n = map.get(key);
        if (n != null) { n.value = value; moveToFront(n); return; }
        if (map.size() == capacity) {
            Node lru = tail.prev;               // evict least recently used
            unlink(lru);
            map.remove(lru.key);
        }
        Node fresh = new Node(key, value);
        map.put(key, fresh);
        insertAfterHead(fresh);
    }

    private void unlink(Node n) { n.prev.next = n.next; n.next.prev = n.prev; }
    private void insertAfterHead(Node n) {
        n.next = head.next; n.prev = head;
        head.next.prev = n; head.next = n;
    }
    private void moveToFront(Node n) { unlink(n); insertAfterHead(n); }
}
```

(Java shortcut worth mentioning, then implementing manually anyway:
`LinkedHashMap` with `accessOrder=true` + overridden `removeEldestEntry`.)

---

## 5. Interview Pitfalls

- **Losing the reference**: always save `cur.next` before overwriting it.
- Forgetting the dummy head → special-casing head deletion → bugs.
- Null checks: `fast != null && fast.next != null` — order matters.
- Even-length lists: decide which "middle" you want (first or second of the pair).
- Cycle in modified lists — if you spliced wrong, your traversal never ends.
- Recursion depth on lists of 10⁵+ nodes → prefer iteration.

## Must-Solve List
1. Reverse Linked List (iterative AND recursive)
2. Merge Two Sorted Lists → Merge K Sorted Lists (heap; see folder 07)
3. Linked List Cycle I & II
4. Remove Nth Node From End
5. Reorder List (middle + reverse + interleave)
6. Palindrome Linked List
7. Add Two Numbers
8. Copy List with Random Pointer (HashMap or interleaving trick)
9. LRU Cache (design; asked at every senior loop)
10. Reverse Nodes in k-Group (hard; combines everything)

---

## Deep Dive & Worked Examples

> Runnable examples: `LinkedListDemo.java` in this folder (`javac LinkedListDemo.java && java LinkedListDemo`)

### Dry run: reversing 1→2→3 pointer by pointer

State after each iteration (`prev | cur`):

```
init : null | 1→2→3
step1: 1    | 2→3        (1.next now = null)
step2: 2→1  | 3          (2.next now = 1)
step3: 3→2→1| null       (3.next now = 2)  → return prev = 3
```

The invariant to say out loud: *"everything left of `cur` is already reversed and
reachable from `prev`; everything from `cur` on is untouched."* If you can state the
invariant, you'll never fumble the pointer order.

### Why Floyd's cycle detection finds the START of the cycle

Let the distance head→cycle-start be `a`, and the meeting point be `b` steps into the
cycle of length `c`. When slow enters the cycle, fast is somewhere inside; fast gains
one step per iteration, so they must meet. At the meeting: slow walked `a + b`, fast
walked `2(a + b)`. Fast's extra distance `a + b` must be whole loops: `a + b = k·c`,
so `a = k·c − b`. Meaning: from the meeting point, walking `a` more steps lands
exactly on the cycle start (b + a = k·c ≡ start). Hence phase 2: one pointer from
head, one from the meeting point, both stepping once — they collide at the entry.
Interviewers frequently ask for exactly this derivation; it's three lines of algebra.

**Where cycles occur in practice:** corrupted free-lists in allocators, circular
`next` references built from bad data imports, and dependency chains — Floyd gives
you O(1)-space detection when you can't afford a visited set.

### LRU cache — why exactly this pair of structures?

Requirements: `get` O(1), `put` O(1), evict least-recent O(1).

| Structure alone | What breaks |
|-----------------|-------------|
| HashMap only | no recency order — can't find the LRU victim |
| List only | O(n) lookup by key |
| Singly linked list + map | unlinking a node needs its predecessor → O(n) |
| **Doubly linked list + map** | all three ops O(1) ✓ |

The `prev` pointer is the entire reason for "doubly": `map.get(key)` hands you the
node, and you can unlink it without walking the list. Sentinels (dummy head + tail)
remove all null checks — the same dummy-head idea from basic problems, doubled.

Follow-up interviewers love: *"make it thread-safe."* Options in increasing
sophistication: synchronize both methods (coarse, contention on every get);
`ConcurrentHashMap` + a lock only around list surgery; or per-segment LRU
(how real caches shard). Also know `Caffeine` uses a probabilistic
Window-TinyLFU rather than strict LRU — strict LRU is easily polluted by scans.

### Interviewer follow-ups you should expect

- *"Reverse in groups of k"* — reverse k nodes, recurse/iterate for the rest; the
  dummy head + "tail of previous group" bookkeeping is the whole difficulty.
- *"Palindrome check without modifying the list?"* O(n) space stack/recursion, or
  restore the list by re-reversing the second half after comparing (mention it!).
- *"Copy List with Random Pointer without extra space?"* Interleave copies
  (A→A'→B→B'), wire randoms via `orig.random.next`, then unweave. HashMap version
  first, then this as the optimization.
- *"Why does Java's LinkedList rarely win?"* Cache locality: nodes are heap-scattered
  objects with 2 pointers + object header (~40 bytes overhead per element); iteration
  causes cache misses. `ArrayDeque` or `ArrayList` beat it for queues/stacks too.

**Next:** `04-stacks-queues`
