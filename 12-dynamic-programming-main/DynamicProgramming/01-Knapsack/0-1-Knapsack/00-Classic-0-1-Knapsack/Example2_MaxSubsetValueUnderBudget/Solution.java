/**
 * ============================================================================
 *  CLASSIC 0-1 KNAPSACK  —  Example 2 : "Max value under a budget"
 * ============================================================================
 *  Same pattern, real-world framing: pick projects (cost, profit) to maximize
 *  profit without exceeding a budget. Each project done at most once => 0/1.
 *
 *  IDENTIFY: include/exclude choice + capacity(budget) + overlapping states.
 * ============================================================================
 */
public class Solution {

    // 1) RECURSIVE  — O(2^n)
    static int solveRecursive(int[] cost, int[] profit, int n, int budget) {
        if (n == 0 || budget == 0) return 0;
        if (cost[n - 1] <= budget) {
            int take = profit[n - 1] + solveRecursive(cost, profit, n - 1, budget - cost[n - 1]);
            int skip = solveRecursive(cost, profit, n - 1, budget);
            return Math.max(take, skip);
        }
        return solveRecursive(cost, profit, n - 1, budget);
    }

    // 2) MEMOIZED  — O(n*budget)
    static int solveMemoized(int[] cost, int[] profit, int n, int budget, Integer[][] memo) {
        if (n == 0 || budget == 0) return 0;
        if (memo[n][budget] != null) return memo[n][budget];
        int best;
        if (cost[n - 1] <= budget) {
            int take = profit[n - 1] + solveMemoized(cost, profit, n - 1, budget - cost[n - 1], memo);
            int skip = solveMemoized(cost, profit, n - 1, budget, memo);
            best = Math.max(take, skip);
        } else {
            best = solveMemoized(cost, profit, n - 1, budget, memo);
        }
        return memo[n][budget] = best;
    }

    // 3) TABULATION — O(n*budget)
    static int solveTabulation(int[] cost, int[] profit, int n, int budget) {
        int[][] dp = new int[n + 1][budget + 1];
        for (int i = 1; i <= n; i++)
            for (int b = 1; b <= budget; b++)
                dp[i][b] = (cost[i - 1] <= b)
                        ? Math.max(profit[i - 1] + dp[i - 1][b - cost[i - 1]], dp[i - 1][b])
                        : dp[i - 1][b];
        return dp[n][budget];
    }

    public static void main(String[] args) {
        int[] cost   = {10, 20, 30};
        int[] profit = {60, 100, 120};
        int   budget = 50;
        int   n      = cost.length;

        System.out.println("Max profit under budget " + budget);
        int r = solveRecursive(cost, profit, n, budget);
        int m = solveMemoized(cost, profit, n, budget, new Integer[n + 1][budget + 1]);
        int t = solveTabulation(cost, profit, n, budget);
        System.out.println("Recursive  : " + r);
        System.out.println("Memoized   : " + m);
        System.out.println("Tabulation : " + t);
        System.out.println("Pick projects 2+3 (cost 50) -> profit 220");
        assert r == 220 && m == 220 && t == 220 : "mismatch!";
    }
}
