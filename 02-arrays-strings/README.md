# 02 — Arrays & Strings

## Why It Matters

Roughly a third of all interview questions are array/string problems. They look easy,
which is exactly why the bar is high: interviewers expect the optimal solution, clean
edge-case handling, and pattern recognition (prefix sum, in-place, Kadane, etc.).


---


## 1. Arrays — Internals

An array is a **contiguous block of memory**. `arr[i]` computes `base + i × elementSize`
— that's why access is O(1). Contiguity also gives **cache locality**: iterating an
array is far faster in practice than iterating a linked list of the same size, even
though both are "O(n)". Mentioning cache locality is a senior-level signal.

### Real-life analogy
A row of numbered lockers in a gym. Locker #57 is instantly findable (O(1) access).
But inserting a new locker between #3 and #4 means shifting every locker after it (O(n) insert).

| Operation | Array | Notes |
|-----------|-------|-------|
| Access by index | O(1) | address arithmetic |
| Search (unsorted) | O(n) | linear scan |
| Search (sorted) | O(log n) | binary search |
| Insert/delete at end | O(1) amortized | ArrayList |
| Insert/delete at middle | O(n) | shift elements |

### Production use cases
- `ArrayList` backs most Java collections in practice.
- Database pages, message buffers, bitmap indexes — all arrays.
- Ring buffers (circular arrays) power Kafka-style logs and LMAX Disruptor.

---

## 2. Core Patterns with Java Code

### Pattern A — Prefix Sums (range queries in O(1))

**Real-life:** a bank statement. Instead of re-adding all transactions to know the
balance between March and July, keep a running balance; the range sum is
`balance[july] - balance[feb]`.

```java
// Range Sum Query: preprocess O(n), each query O(1)
class PrefixSum {
    private final long[] prefix;                 // prefix[i] = sum of first i elements

    PrefixSum(int[] nums) {
        prefix = new long[nums.length + 1];
        for (int i = 0; i < nums.length; i++)
            prefix[i + 1] = prefix[i] + nums[i];
    }

    long rangeSum(int left, int right) {         // inclusive indices
        return prefix[right + 1] - prefix[left];
    }
}
```

Extension seen in interviews: **Subarray Sum Equals K** — prefix sums + HashMap of
"how many times have I seen this prefix" → O(n). Also 2-D prefix sums for matrices.

```java
// Subarray Sum Equals K — count subarrays summing to k, O(n)
int subarraySum(int[] nums, int k) {
    Map<Long, Integer> seen = new HashMap<>();
    seen.put(0L, 1);                             // empty prefix
    long running = 0; int count = 0;
    for (int x : nums) {
        running += x;
        count += seen.getOrDefault(running - k, 0);
        seen.merge(running, 1, Integer::sum);
    }
    return count;
}
```

### Pattern B — Kadane's Algorithm (max subarray)

**Real-life:** best streak of daily profit/loss for a trading desk — at each day,
either extend the current streak or start fresh.

```java
int maxSubArray(int[] nums) {                    // O(n) time, O(1) space
    int best = nums[0], current = nums[0];
    for (int i = 1; i < nums.length; i++) {
        current = Math.max(nums[i], current + nums[i]);  // extend or restart
        best = Math.max(best, current);
    }
    return best;
}
```

### Pattern C — In-place manipulation

Interviewers love O(1)-space answers. Example: **rotate array right by k** using
triple reversal.

```java
void rotate(int[] nums, int k) {                 // O(n) time, O(1) space
    k %= nums.length;
    reverse(nums, 0, nums.length - 1);           // [1,2,3,4,5,6,7] k=3 → [7,6,5,4,3,2,1]
    reverse(nums, 0, k - 1);                     // → [5,6,7,4,3,2,1]
    reverse(nums, k, nums.length - 1);           // → [5,6,7,1,2,3,4]
}
private void reverse(int[] a, int i, int j) {
    while (i < j) { int t = a[i]; a[i++] = a[j]; a[j--] = t; }
}
```

Other classics: Move Zeroes (stable partition), Dutch National Flag (3-way partition,
sort colors), Next Permutation.

```java
// Dutch National Flag — sort 0s,1s,2s in one pass, O(n)/O(1)
void sortColors(int[] a) {
    int low = 0, mid = 0, high = a.length - 1;
    while (mid <= high) {
        if (a[mid] == 0)      swap(a, low++, mid++);
        else if (a[mid] == 2) swap(a, mid, high--);   // don't advance mid!
        else                  mid++;
    }
}
```

### Pattern D — Matrix traversal

Spiral order, rotate image 90° (transpose + reverse rows), set matrix zeroes
(use first row/col as markers for O(1) space).

```java
// Rotate NxN matrix 90° clockwise in place
void rotateImage(int[][] m) {
    int n = m.length;
    for (int i = 0; i < n; i++)                   // 1) transpose
        for (int j = i + 1; j < n; j++) {
            int t = m[i][j]; m[i][j] = m[j][i]; m[j][i] = t;
        }
    for (int[] row : m) reverse(row, 0, n - 1);   // 2) reverse each row
}
```

---

## 3. Strings — Internals (Java-specific, high interview value)

- `String` is **immutable**. Every `+`, `substring`, `replace` creates a new object.
- The **string pool** interns literals: `"a" == "a"` is true, but always compare with `.equals()`.
- `StringBuilder` for mutation (non-thread-safe), `StringBuffer` (synchronized, legacy).
- Java 9+ uses compact strings (byte[] with Latin-1/UTF-16 flag) — a nice depth signal.

### Real-life analogy for immutability
A published book: you can't edit a printed copy — you print a new edition. That's
`String`. `StringBuilder` is the manuscript you freely edit before publishing.

### Core string patterns

```java
// 1) Frequency counting — anagram check, O(n)
boolean isAnagram(String s, String t) {
    if (s.length() != t.length()) return false;
    int[] freq = new int[26];                     // ask: lowercase only? Unicode → HashMap
    for (int i = 0; i < s.length(); i++) {
        freq[s.charAt(i) - 'a']++;
        freq[t.charAt(i) - 'a']--;
    }
    for (int f : freq) if (f != 0) return false;
    return true;
}

// 2) Group Anagrams — canonical form as HashMap key, O(n · k log k)
List<List<String>> groupAnagrams(String[] strs) {
    Map<String, List<String>> groups = new HashMap<>();
    for (String s : strs) {
        char[] chars = s.toCharArray();
        Arrays.sort(chars);
        groups.computeIfAbsent(new String(chars), k -> new ArrayList<>()).add(s);
    }
    return new ArrayList<>(groups.values());
}

// 3) Longest Palindromic Substring — expand around center, O(n²)/O(1)
String longestPalindrome(String s) {
    int start = 0, maxLen = 0;
    for (int i = 0; i < s.length(); i++) {
        int len = Math.max(expand(s, i, i), expand(s, i, i + 1)); // odd & even centers
        if (len > maxLen) { maxLen = len; start = i - (len - 1) / 2; }
    }
    return s.substring(start, start + maxLen);
}
private int expand(String s, int l, int r) {
    while (l >= 0 && r < s.length() && s.charAt(l) == s.charAt(r)) { l--; r++; }
    return r - l - 1;
}
```

### String matching (know the names + ideas)
- **Naive:** O(n·m).
- **KMP:** O(n+m) using an LPS (longest proper prefix that is also suffix) failure table — used in `grep`-style scanning.
- **Rabin-Karp:** rolling hash, O(n+m) average — powers plagiarism detection and chunk deduplication in storage systems.
- For interviews: be able to explain KMP's idea and code Rabin-Karp if pushed.

---

## 4. Complexity Summary

| Problem type | Naive | Optimal | Trick |
|--------------|-------|---------|-------|
| Range sum queries | O(n) per query | O(1) per query | prefix sums |
| Max subarray | O(n²) | O(n) | Kadane |
| Subarray sum = k | O(n²) | O(n) | prefix + HashMap |
| Anagram check | O(n log n) sort | O(n) | frequency array |
| Rotate array | O(n) extra space | O(1) space | triple reverse |

---

## 5. Interview Pitfalls

- **Off-by-one errors** — always clarify inclusive/exclusive bounds; trace with a 2–3 element example.
- **Integer overflow** — sums of int arrays can exceed `Integer.MAX_VALUE`; use `long`.
- Empty array / single element / all negatives (breaks naive Kadane with `best = 0`).
- `s.charAt(i) - 'a'` assumes lowercase ASCII — **ask about the character set**.
- Concatenating strings in loops (O(n²)) — use `StringBuilder`.
- Modifying an array while iterating with an index you also move — trace carefully (Dutch flag `mid` vs `high`).

## Must-Solve List
1. Two Sum / Best Time to Buy & Sell Stock (easy fluency)
2. Product of Array Except Self (prefix/suffix, no division)
3. Maximum Subarray (Kadane)
4. Subarray Sum Equals K
5. Merge Intervals (also see 16-advanced)
6. Rotate Array / Rotate Image
7. Set Matrix Zeroes / Spiral Matrix
8. Group Anagrams / Valid Anagram
9. Longest Palindromic Substring
10. Next Permutation

---

## Deep Dive & Worked Examples

> Runnable examples: `ArraysStringsDemo.java` in this folder (`javac ArraysStringsDemo.java && java ArraysStringsDemo`)

### Dry run: Kadane on [-2, 1, -3, 4, -1, 2, 1, -5, 4]

The decision at every element: *extend the running streak, or is the element alone
better than the streak plus the element?*

| i | num | current = max(num, current+num) | best | reasoning |
|---|-----|--------------------------------|------|-----------|
| 0 | -2 | -2 | -2 | start |
| 1 | 1 | max(1, -1) = 1 | 1 | streak was a liability — restart |
| 2 | -3 | max(-3, -2) = -2 | 1 | extend (still better than -3 alone) |
| 3 | 4 | max(4, 2) = 4 | 4 | restart — the -2 streak only drags us down |
| 4 | -1 | max(-1, 3) = 3 | 4 | extend |
| 5 | 2 | max(2, 5) = 5 | 5 | extend |
| 6 | 1 | max(1, 6) = 6 | 6 | extend — answer subarray is [4,-1,2,1] |
| 7 | -5 | max(-5, 1) = 1 | 6 | extend but best unchanged |
| 8 | 4 | max(4, 5) = 5 | 6 | extend; final answer 6 |

Why it works: a prefix with negative sum can never help any subarray that extends
past it — dropping it is always at least as good. That's the "optimal substructure"
that later powers DP (Kadane IS a 1-D DP with O(1) space).

### Dry run: rotate [1,2,3,4,5,6,7] right by k=3 via triple reverse

```
start           : 1 2 3 4 5 6 7
reverse all     : 7 6 5 4 3 2 1     (last k are now first, but reversed)
reverse [0,k)   : 5 6 7 4 3 2 1     (fix the first k)
reverse [k,n)   : 5 6 7 1 2 3 4     (fix the rest) ✓
```

Why it works: rotation moves the last k elements to the front preserving order.
Reversing everything gets them to the front in *reversed* order; the two partial
reversals restore order within each block. No extra memory, three linear passes.

### Prefix sums, generalized

The prefix trick isn't just sums — any **invertible, associative** operation works:
prefix XOR (subarray XOR queries), prefix products (Product of Array Except Self =
prefix product × suffix product, avoiding division), and 2-D prefix sums where
`sum(r1..r2, c1..c2) = P[r2][c2] − P[r1-1][c2] − P[r2][c1-1] + P[r1-1][c1-1]`
(inclusion–exclusion — draw the four rectangles when explaining).

Difference arrays are the inverse trick: to apply "add v to range [l,r]" many times,
do `diff[l] += v; diff[r+1] -= v` then one prefix pass — O(1) per update. Powers
"Corporate Flight Bookings" and calendar-load problems.

### Interviewer follow-ups you should expect

- *"Your anagram solution assumes lowercase ASCII — what about Unicode?"* Switch
  `int[26]` to `HashMap<Character,Integer>` or `int[]` indexed by code point; note
  that Java `char` is a UTF-16 unit, so emoji become two chars — use `codePoints()`.
- *"Kadane returns the sum. Return the subarray itself."* Track `start` tentatively
  when you restart, commit it when `best` improves (the demo shows the indices).
- *"What if the array is all negatives?"* Kadane as written handles it (returns the
  max element). The buggy variant that initializes `best = 0` does not — classic trap.
- *"Product of Array Except Self with zeros?"* Count zeros: >1 zero → all zeros;
  exactly 1 → only that position gets the product of the rest.
- *"Why is String immutable in Java?"* Security (paths/class names can't change after
  checks), safe sharing across threads, string-pool interning, and stable hashCode
  caching (`hash` field computed once) — which is why String keys are so fast in HashMaps.

**Next:** `03-linked-lists`
