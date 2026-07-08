# 12 — Dynamic Programming

## Why It Matters

DP is the topic that most separates offers from rejections at product companies.
It's not magic: DP = recursion + caching of overlapping subproblems. Master the
**framework** and the **classic families**, and most DP questions become
recognitions, not inventions.

---

## 1. When Is a Problem DP?

Two properties must hold:
1. **Optimal substructure** — the best answer for n builds from best answers of
   smaller inputs.
2. **Overlapping subproblems** — naive recursion recomputes the same states.

Trigger phrases: "minimum cost", "maximum profit", "number of ways", "longest/shortest
subsequence", "can it be partitioned/formed".

### Real-life analogy
Planning the **cheapest multi-city flight itinerary**: the cheapest way to reach
Delhi via Mumbai includes the cheapest way to reach Mumbai (optimal substructure),
and many itineraries pass through Mumbai (overlap) — so you compute "cheapest to
Mumbai" once and reuse it. Airline fare engines and route planners do exactly this.

---

## 2. The 5-Step Framework (use in every DP interview)

1. **Define the state** — "dp[i] = the answer for the first i items" (say it precisely!).
2. **Recurrence** — express dp[i] via smaller states.
3. **Base cases**.
4. **Order** — top-down memo or bottom-up table.
5. **Optimize space** — if dp[i] only needs dp[i-1], dp[i-2], keep two variables.

### Top-down vs bottom-up

| | Memoization (top-down) | Tabulation (bottom-up) |
|---|------------------------|------------------------|
| Style | recursion + cache | iterative table fill |
| Computes | only needed states | all states |
| Risk | stack overflow on deep states | must figure out fill order |
| When | complex state spaces, easier to derive | interview-preferred final form, space-optimizable |

Derive top-down first (it's just the backtracking tree + memo), then convert.

---

## 3. Family 1 — Linear DP (1D)

### Climbing Stairs → House Robber
```java
// House Robber — dp[i] = max loot from first i houses
// rob house i (skip i-1) or don't. Space-optimized to O(1).
int rob(int[] nums) {
    int prev2 = 0, prev1 = 0;                    // dp[i-2], dp[i-1]
    for (int n : nums) {
        int cur = Math.max(prev1, prev2 + n);    // skip house vs rob it
        prev2 = prev1;
        prev1 = cur;
    }
    return prev1;
}
```

**Real-life:** selecting non-adjacent ads/billboards along a highway to maximize
revenue when adjacent slots conflict.

### Coin Change (unbounded knapsack shape)
```java
// Fewest coins to make amount — dp[a] = min coins for amount a. O(amount × coins)
int coinChange(int[] coins, int amount) {
    int[] dp = new int[amount + 1];
    Arrays.fill(dp, amount + 1);                 // "infinity"
    dp[0] = 0;
    for (int a = 1; a <= amount; a++)
        for (int coin : coins)
            if (coin <= a)
                dp[a] = Math.min(dp[a], dp[a - coin] + 1);
    return dp[amount] > amount ? -1 : dp[amount];
}
```

**Real-life:** literally making change at a register; also minimal denominations
for payouts, and the general "reach a target with fewest steps" shape.

### Longest Increasing Subsequence
```java
// O(n²) DP — dp[i] = LIS ending at i
int lengthOfLIS(int[] nums) {
    int[] dp = new int[nums.length];
    Arrays.fill(dp, 1);
    int best = 1;
    for (int i = 1; i < nums.length; i++)
        for (int j = 0; j < i; j++)
            if (nums[j] < nums[i]) { dp[i] = Math.max(dp[i], dp[j] + 1); best = Math.max(best, dp[i]); }
    return best;
}
// Follow-up: O(n log n) with "tails" array + binary search — know the idea:
// tails[k] = smallest possible tail of an increasing subsequence of length k+1;
// each number replaces its lower-bound position (patience sorting).
```

---

## 4. Family 2 — 2D DP / Two Sequences

### Longest Common Subsequence (the mother of string DPs)
```java
// dp[i][j] = LCS length of first i chars of a and first j of b. O(m·n)
int longestCommonSubsequence(String a, String b) {
    int m = a.length(), n = b.length();
    int[][] dp = new int[m + 1][n + 1];
    for (int i = 1; i <= m; i++)
        for (int j = 1; j <= n; j++)
            dp[i][j] = (a.charAt(i - 1) == b.charAt(j - 1))
                     ? dp[i - 1][j - 1] + 1                      // chars match
                     : Math.max(dp[i - 1][j], dp[i][j - 1]);     // skip one side
    return dp[m][n];
}
```

**Real-life:** `git diff` and DNA sequence alignment are LCS variants; spell-check
suggestions use its sibling **Edit Distance** (same table, three operations:
insert/delete/replace → min of three neighbors + 1).

### Grid paths
```java
// Unique Paths — robot moves right/down. dp[j] += dp[j-1] row by row. O(m·n) time, O(n) space
int uniquePaths(int m, int n) {
    int[] dp = new int[n];
    Arrays.fill(dp, 1);
    for (int i = 1; i < m; i++)
        for (int j = 1; j < n; j++)
            dp[j] += dp[j - 1];                  // from-top (dp[j]) + from-left (dp[j-1])
    return dp[n - 1];
}
```

Same shape: Minimum Path Sum, Maximal Square, Dungeon Game (reverse iteration!).

---

## 5. Family 3 — Knapsack (the interview workhorse)

**0/1 Knapsack:** n items (weight, value), capacity W, each item once. dp over
(items, capacity). **Real-life:** cloud instance packing — fitting workloads
(CPU demand = weight, revenue = value) onto a server; cargo loading; marketing
budget allocation across campaigns.

```java
// 0/1 knapsack, space-optimized. KEY: iterate capacity DESCENDING (else item reused)
int knapsack(int[] weight, int[] value, int W) {
    int[] dp = new int[W + 1];                   // dp[w] = best value at capacity w
    for (int i = 0; i < weight.length; i++)
        for (int w = W; w >= weight[i]; w--)     // descending = each item once
            dp[w] = Math.max(dp[w], dp[w - weight[i]] + value[i]);
    return dp[W];
}
```

Disguised knapsacks (must recognize!):
- **Partition Equal Subset Sum** — can you hit sum/2? (boolean knapsack)
- **Target Sum** — +/- signs → subset-sum count.
- **Coin Change II** (count combinations) — unbounded: iterate capacity ASCENDING,
  coins outer (order of loops decides combinations vs permutations — classic trap).

---

## 6. Family 4 — Interval & State-Machine DP

- **Interval DP** — dp[i][j] over subarrays, solved by splitting at k: Burst
  Balloons, Matrix Chain Multiplication, Palindrome Partitioning II. Cost O(n³).
- **State machine DP** — Best Time to Buy/Sell Stock with cooldown/fees: states =
  {holding, not holding, cooldown}, transitions per day. **Real-life:** any
  system with modes and transition costs (spot-instance bidding, battery
  charge/discharge scheduling).

```java
// Stock with cooldown — three states per day, O(n)/O(1)
int maxProfit(int[] prices) {
    int hold = Integer.MIN_VALUE, sold = 0, rest = 0;
    for (int p : prices) {
        int prevSold = sold;
        sold = hold + p;                         // sell today
        hold = Math.max(hold, rest - p);         // keep holding or buy today
        rest = Math.max(rest, prevSold);         // cooldown after selling
    }
    return Math.max(sold, rest);
}
```

---

## 7. Word Break — DP over prefixes (top interview frequency)

```java
// Can s be segmented into dictionary words? dp[i] = s[0..i) segmentable. O(n²)
boolean wordBreak(String s, List<String> wordDict) {
    Set<String> dict = new HashSet<>(wordDict);
    boolean[] dp = new boolean[s.length() + 1];
    dp[0] = true;                                // empty prefix
    for (int i = 1; i <= s.length(); i++)
        for (int j = 0; j < i; j++)
            if (dp[j] && dict.contains(s.substring(j, i))) { dp[i] = true; break; }
    return dp[s.length()];
}
```

**Real-life:** tokenizing `#iamfeelinglucky` hashtags, URL slug splitting, and
CJK text segmentation (no spaces in Chinese/Japanese) all use this exact DP.

---

## 8. Complexity Summary

| Family | State | Time |
|--------|-------|------|
| Linear | dp[i] | O(n) or O(n²) |
| Two sequences | dp[i][j] | O(m·n) |
| Knapsack | dp[i][w] | O(n·W) — "pseudo-polynomial" (know this term) |
| Interval | dp[i][j] + split k | O(n³) |
| Bitmask (TSP-like) | dp[mask][i] | O(2ⁿ·n²), n ≤ ~20 |

## 9. Interview Pitfalls

- Vague state definitions — if you can't say "dp[i][j] means ___" in one sentence, stop and fix it.
- Wrong loop direction in knapsack (0/1 descending, unbounded ascending).
- Loop ORDER for combinations vs permutations (Coin Change II vs Combination Sum IV).
- Base cases: dp[0] = true/0/1 — reason it, don't guess.
- Forgetting space optimization — offering it unprompted is senior polish.
- Answer at dp[n] vs max over all dp[i] (LIS ends anywhere!).
- Jumping to tabulation before the recurrence is solid — derive top-down first.

## Must-Solve List (build up in this order)
1. Climbing Stairs → Min Cost Climbing Stairs
2. House Robber I & II
3. Coin Change I & II
4. Longest Increasing Subsequence (both O(n²) and O(n log n))
5. Longest Common Subsequence → Edit Distance
6. Unique Paths → Minimum Path Sum → Maximal Square
7. Partition Equal Subset Sum → Target Sum
8. Word Break
9. Decode Ways
10. Best Time to Buy and Sell Stock (all variants)
11. Longest Palindromic Subsequence / Palindromic Substrings
12. Burst Balloons (hard, interval DP)
13. Regular Expression Matching (hard — the final boss)

---

## Deep Dive & Worked Examples

> Runnable examples: `DPDemo.java` in this folder — House Robber decision table, Coin Change with a live greedy-fails demonstration, the full LCS matrix with answer reconstruction, and Word Break.

### Dry run: House Robber on [2, 7, 9, 3, 1]

dp[i] = max(dp[i-1] "skip", dp[i-2] + loot[i] "rob"):

| house | value | skip (dp[i-1]) | rob (dp[i-2]+v) | dp[i] |
|-------|-------|----------------|------------------|-------|
| 0 | 2 | 0 | 0+2=2 | 2 |
| 1 | 7 | 2 | 0+7=7 | 7 |
| 2 | 9 | 7 | 2+9=11 | 11 |
| 3 | 3 | 11 | 7+3=10 | 11 |
| 4 | 1 | 11 | 11+1=12 | 12 |

Answer 12 = houses 0, 2, 4. Note house 3's row: robbing it is WORSE than skipping —
the table records the decision, and walking the table backwards recovers *which*
houses ("did dp[i] come from skip or rob?"). Reconstruction-by-backwalk works for
every DP and is a standard follow-up: practice it once per family.

### Dry run: LCS("ABCBDAB", "BDCABA") — reading the matrix

The demo prints the full 7×6 table. The two rules while filling row by row:
chars match → diagonal + 1 (extend the common subsequence);
no match → max(up, left) (best of dropping one char from either string).
Answer cell (bottom-right) = 4, one LCS = "BCBA". Reconstruction: from the corner,
diagonal moves emit characters; up/left moves follow the larger neighbor.

Edit Distance is the same table with a third neighbor (diagonal+1 = replace,
up+1 = delete, left+1 = insert, diagonal+0 on match) — learn LCS thoroughly and
Edit Distance is a 5-minute delta.

### From recursion to DP, mechanically (the interview-safe path)

1. Write the brute-force recursion. For Coin Change: `f(a) = 1 + min over coins of
   f(a - coin)`, base `f(0) = 0`.
2. Identify the state — what arguments does the recursion actually depend on? Just
   `a` here. That's a 1-D memo.
3. Slap on a memo (`Map` or array). Correctness unchanged; complexity collapses to
   (#states × work per state) = O(amount × coins).
4. (Optional, for polish) Convert to bottom-up: the memo's fill order becomes a loop.
5. (Optional) Shrink space if dp[i] depends on a constant window of prior states.

State this 5-step plan out loud at the start of any DP problem — it converts the
scariest interview topic into a checklist, and interviewers reward the process even
if you stumble on details.

### The loop-order subtlety (Coin Change II vs Combination Sum IV)

Counting ways to make 4 with coins {1, 2}:
- **Coins OUTER, amount inner** → each coin considered once as a "phase" →
  combinations: {1,1,1,1}, {1,1,2}, {2,2} = **3 ways**.
- **Amount OUTER, coins inner** → at every amount every coin can appear →
  permutations: also counts (1,2,1), (2,1,1)... = **5 ways** (this is Climbing Stairs
  generalized).

Same code, swapped loops, different question answered. If you can explain WHY
(outer loop = what you commit to in order), you've demonstrated real DP understanding —
this is a favorite senior probe.

### Recognizing the family in 10 seconds

| Problem phrase | Family | State sketch |
|----------------|--------|--------------|
| "subsequence" of ONE array | linear DP | dp[i] ending at i |
| two strings / arrays | 2-D grid DP | dp[i][j] over prefixes |
| "pick items under a budget/capacity" | knapsack | dp[capacity] |
| "number of ways to form/reach" | counting DP | sum instead of min/max |
| "min/max over splitting a range" | interval DP | dp[l][r] with split point |
| "visit all subsets / n ≤ 20" | bitmask DP | dp[mask] |
| stock/machine with modes | state machine | one variable per mode |

### Interviewer follow-ups you should expect

- *"Space-optimize your 2-D DP."* Row i depends only on row i-1 → keep two rows
  (or one, iterated carefully — knapsack's descending loop is exactly this).
- *"Why is knapsack 'pseudo-polynomial'?"* O(n·W) looks polynomial but W is a
  *value*, encoded in log W bits — so the runtime is exponential in input SIZE.
  This is why subset-sum is NP-complete yet solvable for small W.
- *"Return the actual items/path, not the value."* Backwalk the table, or store a
  parent/choice array while filling.
- *"Top-down blew the stack at n = 10⁶."* Convert to bottom-up — this is the main
  practical reason tabulation matters.

**Next:** `13-greedy`
