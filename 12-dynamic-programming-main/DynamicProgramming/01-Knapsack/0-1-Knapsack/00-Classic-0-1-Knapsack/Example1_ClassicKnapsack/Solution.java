/**
 * ============================================================================
 *  CLASSIC 0-1 KNAPSACK  —  Example 1  (the reference template)
 * ============================================================================
 *
 *  PROBLEM
 *  -------
 *  Given n items, each with a weight wt[i] and a value val[i], and a knapsack
 *  of capacity W, select a subset of items to MAXIMIZE total value such that
 *  the total weight does not exceed W. Each item may be used AT MOST ONCE.
 *
 *  HOW TO IDENTIFY THIS PATTERN
 *  ----------------------------
 *   1. There is a repeated CHOICE per element: include it or exclude it (0/1).
 *   2. You are optimizing (max/min) a quantity against a CAPACITY/target.
 *   3. The same sub-state (index, remainingCapacity) recurs across the
 *      recursion tree  ->  overlapping subproblems  ->  Dynamic Programming.
 *
 *  STATE
 *  -----
 *   (n, W) : "best value achievable using the first n items with capacity W".
 *
 *  THREE APPROACHES (read top to bottom to see the DP evolution):
 *   1) solveRecursive    - brute force, O(2^n)
 *   2) solveMemoized     - top-down cache,  O(n*W)
 *   3) solveTabulation   - bottom-up table, O(n*W)  (+ space-optimized note)
 * ============================================================================
 */
public class Solution {

    /* ------------------------------------------------------------------ *
     * 1) RECURSIVE (brute force)                                          *
     *    Try both choices for the last item and take the maximum.         *
     *    Time:  O(2^n)   Space: O(n) recursion stack                      *
     * ------------------------------------------------------------------ */
    static int solveRecursive(int[] wt, int[] val, int n, int W) {
        // Base case: no items left OR no capacity left -> nothing more to gain.
        if (n == 0 || W == 0) return 0;

        if (wt[n - 1] <= W) {
            int include = val[n - 1] + solveRecursive(wt, val, n - 1, W - wt[n - 1]);
            int exclude = solveRecursive(wt, val, n - 1, W);
            return Math.max(include, exclude);
        }
        // Current item too heavy -> we are forced to exclude it.
        return solveRecursive(wt, val, n - 1, W);
    }

    /* ------------------------------------------------------------------ *
     * 2) MEMOIZED (top-down)                                              *
     *    Identical recursion + a cache keyed by the state (n, W).         *
     *    Each distinct (n, W) is computed once.                           *
     *    Time:  O(n*W)   Space: O(n*W) cache + O(n) stack                 *
     * ------------------------------------------------------------------ */
    static int solveMemoized(int[] wt, int[] val, int n, int W, Integer[][] memo) {
        if (n == 0 || W == 0) return 0;
        if (memo[n][W] != null) return memo[n][W];   // reuse overlapping subproblem

        int best;
        if (wt[n - 1] <= W) {
            int include = val[n - 1] + solveMemoized(wt, val, n - 1, W - wt[n - 1], memo);
            int exclude = solveMemoized(wt, val, n - 1, W, memo);
            best = Math.max(include, exclude);
        } else {
            best = solveMemoized(wt, val, n - 1, W, memo);
        }
        return memo[n][W] = best;
    }

    /* ------------------------------------------------------------------ *
     * 3) TABULATION (bottom-up)                                           *
     *    dp[i][w] = best value using first i items with capacity w.        *
     *    Fill from base cases (i=0 or w=0 -> 0) upward.                    *
     *    Time:  O(n*W)   Space: O(n*W)  (can be rolled to O(W), see note)  *
     * ------------------------------------------------------------------ */
    static int solveTabulation(int[] wt, int[] val, int n, int W) {
        int[][] dp = new int[n + 1][W + 1];          // row/col 0 already 0 (base case)

        for (int i = 1; i <= n; i++) {
            for (int w = 1; w <= W; w++) {
                if (wt[i - 1] <= w) {
                    int include = val[i - 1] + dp[i - 1][w - wt[i - 1]];
                    int exclude = dp[i - 1][w];
                    dp[i][w] = Math.max(include, exclude);
                } else {
                    dp[i][w] = dp[i - 1][w];         // too heavy -> carry down value
                }
            }
        }
        return dp[n][W];
        // SPACE OPTIMIZATION: dp[i] depends only on dp[i-1]. Keep a single
        // int[W+1] row and iterate w DOWNWARD to reuse it -> O(W) space.
    }

    /* ------------------------------------------------------------------ *
     * main() — runs all three approaches on the SAME input and traces it. *
     * ------------------------------------------------------------------ */
    public static void main(String[] args) {
        int[] wt  = {2, 3, 1};
        int[] val = {3, 4, 5};
        int    W  = 3;
        int    n  = wt.length;

        System.out.println("0-1 Knapsack  |  W = " + W);
        System.out.println("items (wt,val): (2,3) (3,4) (1,5)\n");

        System.out.println("Recursive  : " + solveRecursive(wt, val, n, W));
        System.out.println("Memoized   : " + solveMemoized(wt, val, n, W, new Integer[n + 1][W + 1]));
        System.out.println("Tabulation : " + solveTabulation(wt, val, n, W));

        System.out.println("\nDRY RUN (tabulation dp[i][w]):");
        System.out.println("        w=0  w=1  w=2  w=3");
        System.out.println("i=0      0    0    0    0");
        System.out.println("i=1      0    0    3    3   (item wt2,val3)");
        System.out.println("i=2      0    0    3    4   (+ item wt3,val4)");
        System.out.println("i=3      0    5    5    8   (+ item wt1,val5) -> answer 8");
        // Expected output: all three print 8.
    }
}
