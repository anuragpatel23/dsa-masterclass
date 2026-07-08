# 11 — Recursion & Backtracking

## Why It Matters

Recursion is the mental model behind trees, graphs, divide & conquer, and DP.
Backtracking is recursion + undo — it generates all valid configurations
(permutations, subsets, board placements). If DP is the differentiator, recursion
is its prerequisite: every DP starts life as a recursion.

---

## 1. Thinking Recursively

A recursive solution needs exactly three things:
1. **Base case** — smallest input answered directly.
2. **Recursive case** — solve for n assuming n-1 is magically solved ("the leap of faith").
3. **Progress** — every call moves toward the base case.

### Real-life analogy
**Russian nesting dolls**: to count dolls, open one and ask "how many are inside
you?" — the innermost doll answers "just me" (base case). Or asking the person in
front of you in a queue "what position are you?" — they ask the person ahead, until
the front person says "1".

```java
// Classic warm-ups
int factorial(int n) { return n <= 1 ? 1 : n * factorial(n - 1); }

// Reverse a string recursively
String reverse(String s) {
    if (s.length() <= 1) return s;
    return reverse(s.substring(1)) + s.charAt(0);
}

// Pow(x, n) — fast exponentiation, O(log n): the recursion worth showing off
double myPow(double x, long n) {
    if (n < 0) return 1 / myPow(x, -n);
    if (n == 0) return 1;
    double half = myPow(x, n / 2);
    return (n % 2 == 0) ? half * half : half * half * x;
}
```

### How the call stack works
Each call pushes a frame (params, locals, return address). Depth n → O(n) stack
space. JVM default stack ≈ 512KB–1MB → overflow around 10⁴–10⁵ frames. Java has
**no tail-call optimization** — converting deep recursion to iteration is on you.
(Great senior aside when asked "any concerns with this solution?")

---

## 2. Backtracking — the Template

Backtracking = DFS over a **decision tree**: at each step, try a choice, recurse,
**undo** the choice. Prune branches that can't lead to valid answers.

### Real-life analogy
Solving a **maze**: at each junction try a corridor; dead end → walk back to the
junction (undo) and try the next corridor. Or trying outfit combinations with
constraints ("no red with pink") — you abandon a partial outfit the moment it breaks
a rule rather than completing it (pruning).

```java
// THE template — adapt to everything below
void backtrack(State state, List<Choice> choices, List<Result> results) {
    if (isComplete(state)) {
        results.add(snapshot(state));            // copy! state is mutable
        return;
    }
    for (Choice c : choices) {
        if (!isValid(state, c)) continue;        // prune
        apply(state, c);                         // choose
        backtrack(state, nextChoices, results);  // explore
        undo(state, c);                          // un-choose  ← the "backtrack"
    }
}
```

---

## 3. The Canonical Problems (all follow the template)

### Subsets (power set) — include/exclude decisions
```java
List<List<Integer>> subsets(int[] nums) {        // O(n · 2ⁿ)
    List<List<Integer>> result = new ArrayList<>();
    dfs(nums, 0, new ArrayList<>(), result);
    return result;
}
private void dfs(int[] nums, int start, List<Integer> path, List<List<Integer>> result) {
    result.add(new ArrayList<>(path));           // every node is a valid subset
    for (int i = start; i < nums.length; i++) {
        path.add(nums[i]);                       // choose
        dfs(nums, i + 1, path, result);          // explore (i+1 avoids reuse)
        path.remove(path.size() - 1);            // un-choose
    }
}
```

### Permutations — order matters, track used
```java
List<List<Integer>> permute(int[] nums) {        // O(n · n!)
    List<List<Integer>> result = new ArrayList<>();
    permute(nums, new boolean[nums.length], new ArrayList<>(), result);
    return result;
}
private void permute(int[] nums, boolean[] used, List<Integer> path,
                     List<List<Integer>> result) {
    if (path.size() == nums.length) { result.add(new ArrayList<>(path)); return; }
    for (int i = 0; i < nums.length; i++) {
        if (used[i]) continue;
        used[i] = true;  path.add(nums[i]);
        permute(nums, used, path, result);
        used[i] = false; path.remove(path.size() - 1);
    }
}
```

### Combination Sum — reuse allowed, target-driven pruning
```java
void dfs(int[] candidates, int remain, int start, List<Integer> path,
         List<List<Integer>> result) {
    if (remain == 0) { result.add(new ArrayList<>(path)); return; }
    for (int i = start; i < candidates.length; i++) {
        if (candidates[i] > remain) break;       // prune (requires sorted input)
        path.add(candidates[i]);
        dfs(candidates, remain - candidates[i], i, path, result);  // i, not i+1: reuse ok
        path.remove(path.size() - 1);
    }
}
```

### Handling duplicates (Subsets II / Permutations II / Combination Sum II)
Sort first, then skip equal siblings at the same tree level:
```java
if (i > start && nums[i] == nums[i - 1]) continue;   // skip duplicate branch
```

### N-Queens — constraint tracking with sets
```java
// Place n queens so none attack. Diagonals identified by r-c and r+c.
public List<List<String>> solveNQueens(int n) {
    List<List<String>> results = new ArrayList<>();
    int[] queenCol = new int[n];
    place(0, n, queenCol, new HashSet<>(), new HashSet<>(), new HashSet<>(), results);
    return results;
}
private void place(int row, int n, int[] queenCol, Set<Integer> cols,
                   Set<Integer> diag1, Set<Integer> diag2, List<List<String>> results) {
    if (row == n) { results.add(render(queenCol, n)); return; }
    for (int col = 0; col < n; col++) {
        if (cols.contains(col) || diag1.contains(row - col) || diag2.contains(row + col))
            continue;                                        // attacked — prune
        cols.add(col); diag1.add(row - col); diag2.add(row + col);
        queenCol[row] = col;
        place(row + 1, n, queenCol, cols, diag1, diag2, results);
        cols.remove(col); diag1.remove(row - col); diag2.remove(row + col);
    }
}
private List<String> render(int[] queenCol, int n) {
    List<String> board = new ArrayList<>();
    for (int r = 0; r < n; r++) {
        char[] row = new char[n];
        Arrays.fill(row, '.');
        row[queenCol[r]] = 'Q';
        board.add(new String(row));
    }
    return board;
}
```

**Real-life for constraint search:** exam timetabling (no student in two exams at
once), airline crew rostering, register allocation in compilers — all backtracking
with pruning (or its industrial-strength cousin, constraint solvers).

### Word Search (grid) — mark/unmark visited (see folder 08 for trie version)

---

## 4. Complexity of Backtracking

| Problem | Search space | Complexity |
|---------|--------------|------------|
| Subsets | 2ⁿ | O(n·2ⁿ) |
| Permutations | n! | O(n·n!) |
| Combination Sum | branching by candidates | exponential, pruning-dependent |
| N-Queens | ~n! with heavy pruning | O(n!) |

State the exponential complexity honestly, then emphasize **pruning** — that's what
the interviewer wants to hear. Constraints (n ≤ 20 or so) are your permission slip.

---

## 5. Recursion → DP Bridge

When the decision tree has **repeated subproblems** (same state reached many ways),
add a memo. This is the entrance to folder 12:

```java
// Fibonacci: O(2ⁿ) → O(n) with one line of memoization
Map<Integer, Long> memo = new HashMap<>();
long fib(int n) {
    if (n <= 1) return n;
    return memo.computeIfAbsent(n, k -> fib(k - 1) + fib(k - 2));
}
```

Difference in one sentence: backtracking **enumerates configurations** (needs all
of them), DP **computes an optimal value** (only needs the best) — that's why DP
can collapse the tree and backtracking can't.

## 6. Interview Pitfalls

- Adding the mutable `path` list to results without copying → all results end empty.
- Forgetting to undo state (the `remove`/`used[i]=false`/set-removals).
- Duplicate results: sort + same-level skip, not a global Set of results (slow, hacky).
- Wrong reuse semantics: `i` vs `i + 1` in the recursive call.
- Not pruning at all — enumerate-then-filter times out; prune inside the loop.
- Recursion depth: path length ≤ n here, fine; but say you've considered it.

## Must-Solve List
1. Subsets I & II
2. Permutations I & II
3. Combination Sum I & II / Combinations
4. Letter Combinations of a Phone Number
5. Palindrome Partitioning
6. Word Search
7. N-Queens
8. Generate Parentheses
9. Sudoku Solver (hard)
10. Restore IP Addresses

---

## Deep Dive & Worked Examples

> Runnable examples: `BacktrackingDemo.java` in this folder — an indentation-traced subsets tree, permutations, and N-Queens with a pruning counter (nodes explored vs brute force).

### The subsets recursion tree for [1, 2, 3], drawn fully

Every node is a valid subset; edges are "add element i" choices; going back up an
edge is the backtrack (remove):

```
[]
├── [1]
│   ├── [1,2]
│   │   └── [1,2,3]
│   └── [1,3]
├── [2]
│   └── [2,3]
└── [3]
```

The `start` parameter is what prevents duplicates like [2,1]: after choosing index
i, you may only add indices > i. Contrast with permutations, where order matters,
so every unused element is a candidate at every level — hence `used[]` instead of
`start`, and n! leaves instead of 2ⁿ nodes.

### The three questions that design any backtracking solution

1. **What is a choice?** (an element to include; a column for this queen; a letter
   for this digit)
2. **What makes a partial state invalid?** — prune HERE, as early as possible.
3. **When is the state complete?** — record a COPY and return.

If you can answer these three, the template writes itself. Interviewers often grade
the *articulation* of these more than the code.

### Pruning quantified (why interviewers care)

N-Queens n=6 brute force tries 6⁶ = 46,656 placements. With column+diagonal pruning
the demo explores ~150 nodes — a 300× reduction, and the gap widens rapidly with n.
The order of magnitude matters: n=8 brute is 16.7M; pruned is ~2,000. When you say
"backtracking is exponential", follow with "but pruning cuts the effective branching
factor" — and if possible, prune with O(1) checks (hash sets for attacked
columns/diagonals, not an O(n) board rescan).

Second-order pruning worth mentioning: **ordering choices**. In Sudoku solvers,
picking the cell with FEWEST candidates first (most-constrained-variable) shrinks
the tree dramatically. Same idea powers real constraint solvers.

### Duplicate handling, precisely

For Subsets II / Permutations II with input [1, 2, 2']:
sort first, then at each tree LEVEL skip an element equal to its unchosen left
sibling: `if (i > start && nums[i] == nums[i-1]) continue;`. Why it works: choosing
2' at a level where 2 was available-but-skipped would rebuild an identical subtree.
The condition allows 2,2' *in sequence* (different levels — that's [2,2], legal) but
forbids 2' *instead of* 2 at the same level. Explain it with this exact sentence and
you'll stand out — most candidates memorize the line without being able to justify it.

### Interviewer follow-ups you should expect

- *"Generate Parentheses: what's the pruning rule?"* Track open/close counts: may
  add '(' while open < n; may add ')' while close < open. The prune IS the solution.
- *"Iterative subsets without recursion?"* Start with [[]]; for each element, append
  it to a copy of every existing subset. Or bitmask enumeration (folder 15).
- *"Count solutions only, not enumerate?"* If only counting, look for a DP instead —
  enumeration is exponential but counting often collapses (e.g., paths in a grid).
- *"Stack depth for permutations of 10⁴ elements?"* Trick question: 10⁴! results
  make enumeration impossible regardless — call out infeasibility rather than
  optimizing the doomed.

**Next:** `12-dynamic-programming`
