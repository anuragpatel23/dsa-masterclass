# 14 — Two Pointers & Sliding Window

## Why It Matters

These two patterns convert O(n²) brute force into O(n) — they're asked more often
than any other optimization pattern, especially in phone screens. The insight in
both: **never re-examine what you've already ruled out**.

---

## 1. Two Pointers

### Variant A — Opposite ends (converging)

Requires sorted data or a symmetric structure. **Real-life:** two people searching
a bookshelf from both ends toward the middle for a pair of books whose combined
width fits a box exactly.

```java
// Two Sum II (sorted input) — O(n)/O(1)
int[] twoSumSorted(int[] a, int target) {
    int lo = 0, hi = a.length - 1;
    while (lo < hi) {
        int sum = a[lo] + a[hi];
        if (sum == target) return new int[]{lo, hi};
        if (sum < target) lo++;                  // need bigger — only lo++ can help
        else hi--;                               // need smaller — only hi-- can help
    }
    return new int[]{-1, -1};
}
```

Why it's safe (say this): if sum < target, no pair using `a[lo]` with anything
left of `hi` can work either (they're all smaller) — so `lo` is fully ruled out.

### Container With Most Water — greedy pointer movement
```java
int maxArea(int[] height) {
    int lo = 0, hi = height.length - 1, best = 0;
    while (lo < hi) {
        best = Math.max(best, Math.min(height[lo], height[hi]) * (hi - lo));
        if (height[lo] < height[hi]) lo++;       // shorter wall limits area — move it
        else hi--;
    }
    return best;
}
```

### 3Sum — sort + fixed element + two pointers, O(n²)
```java
List<List<Integer>> threeSum(int[] nums) {
    Arrays.sort(nums);
    List<List<Integer>> result = new ArrayList<>();
    for (int i = 0; i < nums.length - 2; i++) {
        if (i > 0 && nums[i] == nums[i - 1]) continue;        // skip duplicate anchors
        int lo = i + 1, hi = nums.length - 1;
        while (lo < hi) {
            int sum = nums[i] + nums[lo] + nums[hi];
            if (sum < 0) lo++;
            else if (sum > 0) hi--;
            else {
                result.add(List.of(nums[i], nums[lo], nums[hi]));
                while (lo < hi && nums[lo] == nums[lo + 1]) lo++;   // skip dup pairs
                while (lo < hi && nums[hi] == nums[hi - 1]) hi--;
                lo++; hi--;
            }
        }
    }
    return result;
}
```

### Variant B — Same direction (fast/slow, read/write)

```java
// Remove Duplicates from Sorted Array — write pointer trails read pointer
int removeDuplicates(int[] a) {
    int write = 1;
    for (int read = 1; read < a.length; read++)
        if (a[read] != a[read - 1]) a[write++] = a[read];
    return write;
}
```

Family: Move Zeroes, Merge Sorted Array (from the back!), linked-list cycle
detection (folder 03), Trapping Rain Water (two-pointer version).

---

## 2. Sliding Window

Maintain a window [left, right] plus a summary of its contents (count, sum, freq
map). Expand `right`; when the window violates constraints, shrink from `left`.
Each pointer moves at most n times → O(n).

### Real-life analogy
A **train window**: watching scenery you see a contiguous stretch of track at once;
as the train moves, one new field enters view and one leaves — you never re-observe
the whole landscape. Production versions: rate limiters ("max 100 requests in any
60s window"), moving averages on dashboards, TCP congestion windows.

### Fixed-size window
```java
// Max average of any k consecutive elements — slide by add/subtract
double findMaxAverage(int[] nums, int k) {
    long sum = 0;
    for (int i = 0; i < k; i++) sum += nums[i];
    long best = sum;
    for (int i = k; i < nums.length; i++) {
        sum += nums[i] - nums[i - k];            // one enters, one leaves
        best = Math.max(best, sum);
    }
    return (double) best / k;
}
```

### Variable-size window — THE template
```java
// Longest Substring Without Repeating Characters — O(n)
int lengthOfLongestSubstring(String s) {
    Map<Character, Integer> lastSeen = new HashMap<>();
    int left = 0, best = 0;
    for (int right = 0; right < s.length(); right++) {
        char c = s.charAt(right);
        if (lastSeen.containsKey(c) && lastSeen.get(c) >= left)
            left = lastSeen.get(c) + 1;          // jump past the duplicate
        lastSeen.put(c, right);
        best = Math.max(best, right - left + 1);
    }
    return best;
}

// Minimum window ≥ target sum (positive nums) — shrink while still valid
int minSubArrayLen(int target, int[] nums) {
    int left = 0, best = Integer.MAX_VALUE;
    long sum = 0;
    for (int right = 0; right < nums.length; right++) {
        sum += nums[right];
        while (sum >= target) {                  // valid — try to shrink
            best = Math.min(best, right - left + 1);
            sum -= nums[left++];
        }
    }
    return best == Integer.MAX_VALUE ? 0 : best;
}
```

### Minimum Window Substring (hard flagship)
Find the smallest window of `s` containing all chars of `t`: freq map of `t`, a
`matched` counter, expand right until all matched, shrink left while valid.

```java
String minWindow(String s, String t) {
    int[] need = new int[128];
    for (char c : t.toCharArray()) need[c]++;
    int required = t.length();
    int left = 0, bestLen = Integer.MAX_VALUE, bestStart = 0;

    for (int right = 0; right < s.length(); right++) {
        if (need[s.charAt(right)]-- > 0) required--;      // useful char consumed
        while (required == 0) {                           // window valid — shrink
            if (right - left + 1 < bestLen) {
                bestLen = right - left + 1;
                bestStart = left;
            }
            if (need[s.charAt(left++)]++ == 0) required++; // left char was essential
        }
    }
    return bestLen == Integer.MAX_VALUE ? "" : s.substring(bestStart, bestStart + bestLen);
}
```

### When sliding window does NOT work
Negative numbers break "expanding grows the sum / shrinking reduces it"
monotonicity → use prefix-sum + HashMap instead (folder 02/05). Saying this
unprompted when you spot negatives is a strong signal.

---

## 3. Pattern Recognition Cheat Sheet

| Phrase in problem | Pattern |
|-------------------|---------|
| "pair summing to X in sorted array" | opposite-end two pointers |
| "longest/shortest **substring/subarray** with condition" | variable sliding window |
| "every window of size k" | fixed window (or monotonic deque, folder 04) |
| "remove/compact in place" | read/write pointers |
| "subarray sum equals k, negatives allowed" | prefix sum + HashMap (NOT window) |
| "anagram/permutation in string" | fixed window + freq compare |

## 4. Interview Pitfalls

- Shrink with `while`, not `if` — one new element may require multiple shrinks.
- Window length is `right - left + 1` (inclusive) — off-by-ones kill.
- 3Sum duplicate handling — skip at anchor AND at both pointers.
- Using a window with negative numbers (see above).
- Freq-map windows: decrement AND remove-at-zero, or compare counts carefully.
- Forgetting the sorted-input precondition for converging pointers.

## Must-Solve List
1. Valid Palindrome / Two Sum II
2. 3Sum / 3Sum Closest
3. Container With Most Water
4. Trapping Rain Water (two-pointer version — explain why it works)
5. Longest Substring Without Repeating Characters
6. Permutation in String / Find All Anagrams
7. Minimum Size Subarray Sum
8. Minimum Window Substring (hard)
9. Longest Repeating Character Replacement
10. Fruit Into Baskets (at most K distinct)

---

## Deep Dive & Worked Examples

> Runnable examples: `TwoPointersDemo.java` in this folder — converging-pointer traces, Container With Most Water decisions, window jumps on "abcabcbb", and minimum window substring.

### Dry run: longest unique substring on "abcabcbb"

`lastSeen` maps each char to its most recent index; `left` jumps past duplicates:

| right | char | duplicate in window? | left | window | best |
|-------|------|---------------------|------|--------|------|
| 0 | a | no | 0 | "a" | 1 |
| 1 | b | no | 0 | "ab" | 2 |
| 2 | c | no | 0 | "abc" | 3 |
| 3 | a | yes (idx 0 ≥ left) | 1 | "bca" | 3 |
| 4 | b | yes (idx 1 ≥ left) | 2 | "cab" | 3 |
| 5 | c | yes (idx 2 ≥ left) | 3 | "abc" | 3 |
| 6 | b | yes (idx 4 ≥ left) | 5 | "cb" | 3 |
| 7 | b | yes (idx 6 ≥ left) | 7 | "b" | 3 |

The guard `lastSeen.get(c) >= left` matters: at right=6, 'b' was last seen at 4,
which is inside the window — jump. But a stale entry LEFT of the window must be
ignored (don't move `left` backwards!). Forgetting this guard is the #1 bug in this
problem.

### Why moving the taller wall can never help (Container With Most Water)

Area = min(h[lo], h[hi]) × width. Suppose h[lo] < h[hi]. Any container keeping `lo`
and using a wall inside (lo, hi) has strictly smaller width AND height still capped
by h[lo] → strictly smaller area. So `lo`'s best possible container is the current
one — `lo` is exhausted, discard it. That's a proof by dominated alternatives:
each step permanently eliminates one index with justification, hence O(n) AND
correct. Interviewers accept the algorithm quickly but probe exactly this "why is
it safe?" — have the two-sentence version ready.

### The window-invariant way to never write a buggy window

Define the invariant, then structure code around it:
- **"Window is always valid"** (Longest Substring Without Repeats): expand right;
  if invalid, shrink until valid; THEN record. Record after restoring the invariant.
- **"Record while valid, shrink to break"** (Minimum Window Substring): expand until
  valid; while valid, record + shrink. Recording happens inside the shrink loop.

Longest-style problems use the first shape, shortest-style the second. Identify
which one you're in before typing — it decides where `best` is updated.

### Longest Repeating Character Replacement (the subtle classic)

Window valid iff `windowLength − maxFreqInWindow ≤ k` (chars you'd have to replace).
The famous trick: `maxFreq` may go stale as the window shrinks, and that's *fine* —
the answer only improves when maxFreq genuinely increases, so a stale (too-high)
maxFreq only makes us keep a window that's not better than the best found. The
window never shrinks below the best length → O(n) with a never-decreasing window.
Understanding why staleness is harmless here is genuine senior-level reasoning.

### Interviewer follow-ups you should expect

- *"Your window solution — what if numbers can be negative?"* Sum-based windows
  need monotonicity ("expanding can't decrease the sum"); negatives break it →
  prefix sums + HashMap. Spotting this unprompted scores heavily.
- *"3Sum in O(n²) — can you beat it?"* No known o(n²) for general 3SUM (fine to say);
  what you CAN discuss: early termination when nums[i] > 0, and duplicate skipping.
- *"Trapping Rain Water: explain the two-pointer version."* Water at i is capped by
  min(maxLeft, maxRight); advance from the side with the smaller running max — that
  side's cap is already certain. If explaining feels shaky, offer the O(n)-space
  prefix-max version first, then optimize.
- *"Find All Anagrams: compare freq arrays per window?"* Keep a `matched` counter of
  how many of the 26 counts agree — O(1) per slide instead of O(26).

**Next:** `15-bit-manipulation`
