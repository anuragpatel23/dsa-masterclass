# 01 — Complexity Analysis (Big-O) — The Complete Guide

> Runnable examples: `ComplexityDemo.java` in this folder (`javac ComplexityDemo.java && java ComplexityDemo`)

## Why This Comes First

Every answer you give in an interview ends with "...and this runs in O(n log n) time,
O(n) space." If you can't analyze complexity fluently, nothing else lands. Interviewers
at product companies use complexity as the primary filter: a correct O(n²) solution to
an O(n) problem is often a rejection at senior level. Beyond interviews, complexity
analysis is how architects reason about capacity: "this endpoint does an O(n²) diff on
the cart — what happens when enterprise customers have 10,000 line items?"

---

## 1. The Formal Idea (and why we simplify)

We want to compare *algorithms*, not *computers*. A 2010 laptop and a 2026 server run
the same bubble sort at wildly different speeds, but on both machines doubling the
input quadruples the time. That machine-independent growth curve is what asymptotic
notation captures.

**Formal definition:** f(n) = O(g(n)) if there exist constants c > 0 and n₀ such that
f(n) ≤ c·g(n) for all n ≥ n₀.

In plain words: past some input size, f is bounded above by g times a constant.

**Worked example.** Take f(n) = 3n² + 50n + 7.
- Claim: f(n) = O(n²). Pick c = 4, n₀ = 51. For n ≥ 51: 50n + 7 ≤ 50n + n = 51n ≤ n·n = n²,
  so f(n) ≤ 3n² + n² = 4n². ✓
- This is why we drop constants (3 →) and lower-order terms (50n + 7 →): for large n
  they're absorbed by c and n₀.

### The three notations

| Notation | Meaning | Analogy (exam scores) |
|----------|---------|----------------------|
| O(g) — Big-O | grows **no faster** than g | "I scored at most 90" |
| Ω(g) — Big-Omega | grows **at least** as fast as g | "I scored at least 60" |
| Θ(g) — Big-Theta | grows **exactly** like g | "I scored between 75 and 80" |

Interviews use "O" loosely to mean Θ of the worst case. Don't be pedantic in the room —
but if asked the difference, the table above is the answer.

**Subtle point seniors should know:** Big-O bounds a *function*, and we choose which
function to bound — usually worst-case running time, sometimes average-case (quicksort's
O(n log n) is average-case) or amortized (ArrayList add). Always name which one you mean:
"worst case O(n²), average O(n log n)."

---

## 2. The Growth Ladder — with Real Numbers and Real Feelings

```
O(1) < O(log n) < O(√n) < O(n) < O(n log n) < O(n²) < O(n³) < O(2ⁿ) < O(n!)
```

Concrete table at n = 1,000,000 (assume 100M simple ops/sec ≈ 1 second per 10⁸):

| Complexity | Operations | Wall-clock feel |
|------------|-----------:|-----------------|
| O(1) | 1 | instant |
| O(log n) | ~20 | instant |
| O(√n) | 1,000 | instant |
| O(n) | 10⁶ | ~10 ms |
| O(n log n) | 2×10⁷ | ~0.2 s |
| O(n²) | 10¹² | ~3 hours |
| O(2ⁿ) | 2^1,000,000 | more steps than atoms in the universe |

### Intuition for each class, with a real-life picture

- **O(1) — constant.** A vending machine: press B4, get the snack. It doesn't matter
  if the machine has 10 slots or 10,000. *Code:* array index, HashMap get, stack push.
- **O(log n) — logarithmic.** Guess-the-number between 1 and a million: "higher/lower"
  halves the range; 20 guesses always suffice. Doubling n adds ONE step. *Code:* binary
  search, balanced-tree ops, heap sift. Handy fact: log₂(10⁶) ≈ 20, log₂(10⁹) ≈ 30.
- **O(n) — linear.** Reading a book: twice the pages, twice the time. *Code:* single
  scan, building a frequency map.
- **O(n log n) — linearithmic.** Sorting a deck by repeatedly merging sorted piles.
  This is the proven floor for comparison sorting — you cannot do better in general.
- **O(n²) — quadratic.** Everyone at a party shaking hands with everyone: 10 people =
  45 handshakes, 100 people = 4,950. *Code:* nested dependent loops, comparing all pairs.
- **O(2ⁿ) — exponential.** Trying every subset of toppings on a pizza: each new topping
  DOUBLES the combinations. *Code:* naive recursion with two branches (fib), power set.
- **O(n!) — factorial.** Trying every possible seating order at a dinner table.
  10 guests = 3.6M orders; 20 guests = 2.4×10¹⁸. *Code:* permutations, brute-force TSP.

### Reading constraints like an interviewer (crucial skill)

The problem's constraints leak the intended solution:

| Constraint on n | Intended complexity | Typical technique |
|-----------------|--------------------|--------------------|
| n ≤ 12 | O(n!) | permutations, brute force |
| n ≤ 20 | O(2ⁿ · n) | bitmask DP, subset enumeration |
| n ≤ 100 | O(n³) | Floyd-Warshall, interval DP |
| n ≤ 3,000 | O(n²) | 2D DP, all-pairs |
| n ≤ 10⁵–10⁶ | O(n log n) / O(n) | sort, heap, sliding window, hash |
| n ≤ 10⁹ | O(log n) / O(√n) | binary search on answer, math |
| n ≤ 10¹⁸ | O(log n) | matrix power, digit tricks |

In the room: read constraints FIRST, announce the budget out loud — "n is 10⁵ so I need
roughly O(n log n); an O(n²) pair-scan won't fly." Interviewers score this explicitly.

---

## 3. Analyzing Code — a Dry-Run Masterclass

### Rule 1: sequential blocks ADD, nested blocks MULTIPLY

```java
for (int i = 0; i < n; i++) { ... }        // O(n)
for (int j = 0; j < m; j++) { ... }        // O(m)
// total: O(n + m)  — NOT O(n·m); they are sequential

for (int i = 0; i < n; i++)
    for (int j = 0; j < m; j++) { ... }    // nested → O(n·m)
```

### Rule 2: what the loop DOES to its variable decides the class

```java
for (int i = 0; i < n; i++)        // i += 1      → n iterations      → O(n)
for (int i = 1; i < n; i *= 2)     // i doubles   → log₂(n) iterations → O(log n)
for (int i = n; i > 1; i /= 2)     // i halves    → log₂(n) iterations → O(log n)
for (int i = 1; i*i < n; i++)      // i² < n      → √n iterations      → O(√n)
```

### Rule 3: triangular nested loops are still O(n²)

```java
for (int i = 0; i < n; i++)
    for (int j = i + 1; j < n; j++)   // (n-1) + (n-2) + ... + 1 = n(n-1)/2
        ...                            // → O(n²), constant ½ dropped
```

Dry run for n = 5: inner runs 4+3+2+1 = 10 = 5·4/2 times. The sum 1+2+...+n = n(n+1)/2
is the single most useful formula in complexity analysis — memorize it.

### Rule 4: hidden loops inside library calls

```java
for (int i = 0; i < n; i++)
    if (list.contains(x)) ...          // contains() is O(n) → total O(n²)!

for (String s : words)
    result += s;                       // String += copies everything → O(total²)

s.substring(0, k)                      // O(k) — copies characters (Java 7+)
map.containsKey(k)                     // O(1) avg — but hashing a String costs O(its length)
```

An interviewer WILL plant one of these. Audit every library call in your solution.

### Rule 5: two pointers that never reset are LINEAR despite nesting

```java
int j = 0;
for (int i = 0; i < n; i++) {
    while (j < n && condition(i, j)) j++;   // j only moves forward — total ≤ n steps
}
// O(n), not O(n²): count TOTAL work of the inner loop across the whole run
```

This "aggregate/amortized counting" idea is exactly why sliding window (folder 14) and
monotonic stack (folder 04) are O(n).

### Full worked dry-run: what is the complexity?

```java
int f(int n) {
    int count = 0;
    for (int i = n; i > 0; i /= 2)         // outer: log n iterations, i = n, n/2, n/4...
        for (int j = 0; j < i; j++)        // inner: i iterations
            count++;
    return count;
}
```

Careless answer: "log n outer × n inner = O(n log n)." Correct answer: inner totals
n + n/2 + n/4 + ... = **2n → O(n)**. The geometric series sums to a constant factor.
This exact question is a favorite for separating levels. (Verify by running
`ComplexityDemo` — it counts the iterations for you.)

---

## 4. Recursion Analysis — Recurrences and the Master Theorem

### Draw the recursion tree

For `fib(n)` (naive): each call spawns 2 calls, depth ≈ n → tree has ~2ⁿ nodes → O(2ⁿ)
(tighter: O(1.618ⁿ), the golden ratio — nice trivia). With memoization each distinct
argument (n of them) computed once → O(n). `ComplexityDemo` times both — the difference
at n=40 is ~1 second vs microseconds.

### The Master Theorem (for T(n) = a·T(n/b) + O(n^d))

Compare d with log_b(a):

| Case | Condition | Result | Example |
|------|-----------|--------|---------|
| Leaves dominate | d < log_b a | T = O(n^(log_b a)) | Karatsuba: 3T(n/2)+O(n) → O(n^1.585) |
| Balanced | d = log_b a | T = O(n^d · log n) | merge sort: 2T(n/2)+O(n) → O(n log n) |
| Root dominates | d > log_b a | T = O(n^d) | quickselect (avg): T(n/2)+O(n) → O(n) |

Three worked examples:
- **Binary search:** T(n) = T(n/2) + O(1). a=1, b=2, d=0. log₂1 = 0 = d → O(log n).
- **Merge sort:** T(n) = 2T(n/2) + O(n). a=2, b=2, d=1. log₂2 = 1 = d → O(n log n).
- **"Bad" divide & conquer:** T(n) = 2T(n/2) + O(n²). d=2 > 1 → O(n²) — the merge cost
  dwarfs the recursion; dividing didn't help.

You rarely need the theorem verbatim in interviews, but "2T(n/2) + O(n) is n log n"
should be instant recall.

---

## 5. Space Complexity — the Forgotten Half

Count **extra** memory beyond the input. Three sources: variables/structures you
allocate, the recursion call stack, and output (usually excluded by convention — say so).

```java
// O(1) space — in-place two-pointer reverse
void reverse(int[] a) {
    int i = 0, j = a.length - 1;
    while (i < j) { int t = a[i]; a[i++] = a[j]; a[j--] = t; }
}

// O(n) space — the copy
int[] reversedCopy(int[] a) { ... new int[a.length] ... }

// O(n) space WITHOUT any array — recursion depth!
int sumRec(int[] a, int i) {
    if (i == a.length) return 0;
    return a[i] + sumRec(a, i + 1);       // n stacked frames before first return
}
```

### Call-stack reality in the JVM (senior material)
Each frame stores parameters, locals, and the return address. Default thread stack is
~512KB–1MB (`-Xss` flag tunes it) → StackOverflowError typically around 10,000–100,000
frames depending on frame size. Java has **no tail-call optimization** — a tail-recursive
function still stacks frames. Consequences you should volunteer in interviews:
- Recursing down a 10⁶-node skewed tree or 10⁶-node linked list WILL crash → offer the
  iterative version with an explicit `ArrayDeque`.
- DFS space = O(height); BFS space = O(max width). For a complete binary tree the last
  level holds n/2 nodes → BFS can be far more memory-hungry than DFS.

### Time–space trade-off (the interview meta-pattern)
Almost every optimization story is "spend memory to save time": Two Sum O(n²)/O(1) →
O(n)/O(n) with a HashMap; memoization turns 2ⁿ time into n time and n space. State the
trade explicitly: *"I can get O(n) time if I'm allowed O(n) extra space — acceptable?"*

---

## 6. Amortized Analysis — the Full Story

**Definition:** average cost per operation over a worst-case *sequence* of operations —
not probability, a guarantee about totals.

### ArrayList growth, with the actual accounting

Start capacity 8, double when full. Insert 33 elements:
resizes at sizes 8→16 (copy 8), 16→32 (copy 16), 32→64 (copy 32). Total copies = 56,
total appends = 33 → total work ≈ 89 for 33 ops ≈ 2.7 per op. In general, copies sum to
n + n/2 + n/4 + ... < 2n → **O(1) amortized per add**. (Java's ArrayList actually grows
by 1.5×; same math, different constant.)

**Banker's view:** charge every insert 3 coins — 1 to insert, 2 saved. When a resize
copies k elements, those k elements have 2k coins banked, which pays for the copy. No
operation is ever in debt → amortized O(1). Being able to tell this story cleanly is a
senior differentiator.

### Where amortized costs bite in production
A latency-sensitive API that appends to a 100M-element ArrayList takes an occasional
multi-ms pause on resize — invisible in averages, visible in p99.9. Fixes: pre-size the
list (`new ArrayList<>(expected)`), or use chunked structures. Same story with HashMap
rehashing. Interviewers love "your solution is O(1) amortized — when is that not good
enough?"

### String concatenation — the classic O(n²) trap, quantified
`s += x` copies the whole string each time: 1 + 2 + ... + n = n(n+1)/2 copies → O(n²).
`StringBuilder.append` is amortized O(1) per char (it's a dynamic char array).
`ComplexityDemo` times both at n = 50,000 — expect ~1000× difference.

---

## 7. Best / Average / Worst — and Adversarial Thinking

| Algorithm | Best | Average | Worst | What triggers worst |
|-----------|------|---------|-------|--------------------|
| QuickSort (naive pivot) | n log n | n log n | n² | sorted input, first-element pivot |
| HashMap get | 1 | 1 | log n (Java 8+) | hash collisions on one bucket |
| BST insert | log n | log n | n | sorted insertion order |
| Insertion sort | n | n² | n² | reverse-sorted (best: nearly sorted) |

Three stories to know cold:
1. **Hash-flood DoS:** pre-Java-8, attackers sent HTTP params crafted to collide into
   one HashMap bucket → each insert O(k) → server CPU pegged. Java 8's fix: treeify
   buckets past 8 entries (worst case O(log n)). Real CVE, great interview anecdote.
2. **Quicksort killer:** naive pivots on sorted data degrade to O(n²); production sorts
   defend with randomized/median-of-three pivots or introsort (switch to heapsort when
   recursion gets too deep — C++ std::sort does this).
3. **BST from sorted input** degenerates to a linked list — the reason self-balancing
   trees (and B-trees in databases) exist.

The senior habit: for any data structure you propose, ask yourself *"what input makes
this pathological, and do I care?"*

---

## 8. Big-O Is Not Wall-Clock: Constants and Caches

Two O(n) algorithms can differ 50× in practice. Reasons worth articulating:

- **Cache locality.** Arrays are contiguous; iterating one streams through CPU cache
  lines (64 bytes each). A LinkedList of boxed Integers scatters across the heap —
  every step is a potential cache miss (~100ns vs ~1ns). This is why `ArrayList`
  virtually always beats `LinkedList`, and why sorting an int[] beats sorting an
  Integer[] (boxing = pointer chasing + memory overhead).
- **Constant factors.** Radix sort is O(n) yet often loses to quicksort's tight loops.
  For n < ~50, insertion sort beats everything — TimSort and Arrays.sort exploit this
  by switching to insertion sort on tiny ranges.
- **When to say it:** after giving the asymptotic answer — "asymptotically these tie,
  but the array version will dominate in practice due to cache behavior." Shows you've
  shipped real systems, not just solved puzzles.

---

## 9. Interview Pitfalls (recap + additions)

- Saying "O(n)" without defining n — matrix problems: say O(rows × cols).
- Hidden O(n) library calls inside loops (`contains`, `indexOf`, `substring`, `+=` on String).
- Claiming O(1) space while recursing — stack frames count.
- Confusing amortized (sequence guarantee) with average (probabilistic) with worst case.
- `(lo + hi) / 2` overflows; write `lo + (hi - lo) / 2`.
- Quoting HashMap as O(1) worst case — it's O(1) *average*, O(log n) worst (Java 8+).
- Forgetting output space: "O(n) extra space, plus O(n) for the output list."
- Multiplying when you should aggregate (Rule 5 — two pointers, monotonic stacks).

## 10. Practice Drill

1. For each snippet in §3, cover the answer and re-derive it.
2. Two Sum three ways: O(n²)/O(1) → sort + two pointers O(n log n)/O(1) → HashMap O(n)/O(n).
   Narrate the trade-offs — this is the canonical "optimization story."
3. Run `ComplexityDemo.java`; predict every number before reading the output.
4. Derive merge sort's recurrence and solve it by tree AND by master theorem.
5. Explain to a rubber duck why ArrayList add is O(1) amortized, using the banker's method.

**Next:** `02-arrays-strings`
