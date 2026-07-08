# 13 — Greedy Algorithms

## Why It Matters

Greedy solutions are short and fast — the hard part is knowing WHEN greed is safe
and being able to argue why. Interviewers probe exactly that: "why does taking the
local best never hurt?" A senior candidate answers with an exchange argument, not
"it just works."

---

## 1. The Greedy Principle

At each step take the locally optimal choice and never reconsider. Valid only when
the problem has the **greedy-choice property** (a local best is part of some global
best) plus optimal substructure.

### Real-life analogy — where greedy works
**Cashier's change** with standard denominations: always hand over the biggest coin
that fits. Works for ₹/€/$ coin systems.

### Where greedy FAILS (know a counterexample!)
Coins {1, 3, 4}, amount 6: greedy takes 4+1+1 = 3 coins; optimal is 3+3 = 2 coins.
→ That's why general Coin Change is DP. **Being able to produce this counterexample
on demand is a senior signal.**

### Greedy vs DP in one line
DP explores all choices and picks the best; greedy commits to one choice per step.
If choices interact across steps in ways a local rule can't capture → DP.

---

## 2. Family 1 — Interval Scheduling (the biggest family)

### Activity selection: max non-overlapping intervals → **sort by END time**

**Real-life:** maximizing the number of meetings one conference room can host;
ad-slot scheduling; job scheduling on one machine.

Why end time works (exchange argument): the meeting that ends earliest leaves the
most room for the rest; any optimal solution can swap its first meeting for the
earliest-ending one without losing count.

```java
// Non-overlapping Intervals — min removals = n - max kept
int eraseOverlapIntervals(int[][] intervals) {
    Arrays.sort(intervals, Comparator.comparingInt(i -> i[1]));   // by END
    int kept = 0, lastEnd = Integer.MIN_VALUE;
    for (int[] iv : intervals) {
        if (iv[0] >= lastEnd) {                  // fits after the last kept one
            kept++;
            lastEnd = iv[1];
        }
    }
    return intervals.length - kept;
}
```

Family: Minimum Number of Arrows to Burst Balloons (same, touching counts),
Maximum Length of Pair Chain, Meeting Rooms I.

---

## 3. Family 2 — Jump/Reach problems

```java
// Jump Game — can you reach the last index? Track farthest reachable. O(n)
boolean canJump(int[] nums) {
    int farthest = 0;
    for (int i = 0; i < nums.length; i++) {
        if (i > farthest) return false;          // gap — unreachable
        farthest = Math.max(farthest, i + nums[i]);
    }
    return true;
}

// Jump Game II — min jumps: greedy BFS-like "current window"
int jump(int[] nums) {
    int jumps = 0, currentEnd = 0, farthest = 0;
    for (int i = 0; i < nums.length - 1; i++) {
        farthest = Math.max(farthest, i + nums[i]);
        if (i == currentEnd) {                   // exhausted this jump's window
            jumps++;
            currentEnd = farthest;
        }
    }
    return jumps;
}
```

**Real-life:** minimum network hops with routers of limited range; multi-hop
delivery planning.

### Gas Station (elegant classic)
If total gas ≥ total cost, an answer exists and it's after the worst failure point:

```java
int canCompleteCircuit(int[] gas, int[] cost) {
    int total = 0, tank = 0, start = 0;
    for (int i = 0; i < gas.length; i++) {
        int gain = gas[i] - cost[i];
        total += gain;
        tank += gain;
        if (tank < 0) {                          // can't reach i+1 from current start
            start = i + 1;                       // no station in [start, i] works either
            tank = 0;
        }
    }
    return total >= 0 ? start : -1;
}
```

---

## 4. Family 3 — Sort by the Right Key, Then Commit

Half of greedy is choosing the sort order and defending it.

```java
// Assign Cookies — smallest cookie that satisfies each child, two pointers
int findContentChildren(int[] greed, int[] cookies) {
    Arrays.sort(greed);
    Arrays.sort(cookies);
    int child = 0;
    for (int c = 0; c < cookies.length && child < greed.length; c++)
        if (cookies[c] >= greed[child]) child++;
    return child;
}

// Largest Number — custom order: compare concatenations
String largestNumber(int[] nums) {
    String[] parts = Arrays.stream(nums).mapToObj(String::valueOf).toArray(String[]::new);
    Arrays.sort(parts, (a, b) -> (b + a).compareTo(a + b));   // "9" before "34" since 934 > 349
    if (parts[0].equals("0")) return "0";
    return String.join("", parts);
}
```

Family: Queue Reconstruction by Height (sort tall→short, insert by k),
Candy (two passes), Task Scheduler (most-frequent first — with a heap, folder 07),
Huffman coding (merge two smallest — data compression in ZIP/JPEG).

**Real-life for sort-then-commit:** boarding planes back-to-front, triaging support
tickets by SLA deadline (Earliest Deadline First — a real OS scheduling policy).

---

## 5. Proving Greedy Correct (2-minute interview version)

1. **Exchange argument:** take any optimal solution; show you can swap its first
   choice with the greedy choice without making it worse. Repeat inductively.
2. **Staying ahead:** show greedy's partial solution is ≥ any other partial
   solution at every step (e.g., farthest-reach in Jump Game).

You don't need formal proofs — a crisp verbal argument is exactly what's expected.

---

## 6. Recognize Greedy vs DP Quickly

| Signal | Likely approach |
|--------|----------------|
| "max non-overlapping", sortable by a natural key | Greedy |
| choices constrain far-future options in complex ways | DP |
| counterexample to the obvious local rule exists | DP |
| "minimum number of X to cover/reach" with monotone reach | Greedy |
| unsure? | state greedy idea → hunt counterexample aloud → fall back to DP |

That "hunt a counterexample aloud" habit is itself evaluated — it shows rigor.

## 7. Interview Pitfalls

- Asserting greedy without justification — always give at least an intuition.
- Sorting by START time in activity selection (wrong; sort by END).
- Coin-change greedy on arbitrary denominations (see counterexample above).
- Missing tie-break details in custom comparators (Largest Number's "0" edge case).
- Not recognizing when a greedy needs a helper structure (heap in Task Scheduler).

## Must-Solve List
1. Non-overlapping Intervals / Minimum Arrows
2. Jump Game I & II
3. Gas Station
4. Assign Cookies / Lemonade Change (warm-ups)
5. Partition Labels
6. Queue Reconstruction by Height
7. Candy (hard-ish two-pass)
8. Task Scheduler
9. Largest Number
10. Best Time to Buy and Sell Stock II (greedy version)

---

## Deep Dive & Worked Examples

> Runnable examples: `GreedyDemo.java` in this folder — the sort-by-start-vs-end comparison on the same meetings, a jump-window trace, and Gas Station's restart logic.

### The exchange argument, done properly once

Claim: for max non-overlapping meetings, picking the earliest-ENDING meeting first
is safe. Proof: let OPT be any optimal solution and let g be the earliest-ending
meeting overall. If OPT's first meeting (by end time) is m ≠ g, then end(g) ≤ end(m),
so g conflicts with nothing OPT schedules after m (they all start ≥ end(m) ≥ end(g)).
Swap m → g: still valid, same size. Hence SOME optimal solution starts with g; recurse
on the remaining meetings. ∎

That's the full rigor — 4 sentences. In an interview, 2 sentences suffice: *"any
optimal solution can swap its first meeting for the earliest-ending one without
losing anything, so committing to it is safe; repeat."* Practice compressing proofs
like this; hand-waving "greedy just works" is the most common senior-level ding.

### The demo's counterexample, spelled out

Meetings {[1,10], [2,3], [4,5], [6,7], [8,9]}:
- Sort by **start** → pick [1,10] first → it overlaps everything → total 1 meeting.
- Sort by **end** → [2,3], [4,5], [6,7], [8,9] → total 4 meetings.

One input, both heuristics, 4× difference. Keep a personal stock of counterexamples
like this and coins {1,3,4}@6 — producing one on demand instantly settles
"greedy or DP?" debates in interviews.

### Gas Station: why "restart past the failure" skips nothing

When the tank first goes negative at station i having started from s, consider any
intermediate start s' in (s, i]: the tank at each point from s' is the s-tank MINUS
the (non-negative) tank value s had at s' — i.e., never higher than s's tank was.
So s' also fails by station i. All of (s, i] is eliminated in one stroke → the next
candidate is i+1 → single pass, O(n). Combined with "total surplus ≥ 0 ⇒ some start
works" (provable by induction), the algorithm is complete.

### Greedy + helper structure (the hybrid tier)

Pure sorting isn't always enough; the greedy choice may need a live "best available"
query — that's a heap:
- **Task Scheduler / Reorganize String**: always place the most frequent remaining
  item → max-heap by count.
- **Huffman coding**: repeatedly merge the two smallest frequencies → min-heap.
  Result: optimal prefix-free code (ZIP's deflate ancestry).
- **IPO / maximize capital**: unlock projects by capital (sort), pick max profit
  among unlocked (heap) — two structures cooperating.

When your greedy rule says "the best X *currently available*", reach for a heap.

### Interviewer follow-ups you should expect

- *"Prove your greedy or give me a counterexample hunt."* Do the 2-sentence exchange
  argument; if you can't find one in 30 seconds, say what you'd check and hedge to DP.
- *"Partition Labels: what's the greedy insight?"* A partition must extend to the
  LAST occurrence of every char inside it; close it when i reaches the running max
  of last-occurrences. (Precompute last index per char.)
- *"Candy: why two passes?"* Left pass satisfies left-neighbor constraints, right
  pass right-neighbor; max of both satisfies both. Single-pass attempts fail on
  descending runs — know why.
- *"Is Dijkstra greedy? Is Kruskal?"* Yes — both are greedy WITH proofs (cut
  property for MST, the finalization argument for Dijkstra). Greedy isn't a hack;
  it's a proof obligation.

**Next:** `14-two-pointers-sliding-window`
