/**
 * ============================================================================
 *  CLASSIC 0-1 KNAPSACK  —  Example 3 : "Load balancer capacity"
 * ============================================================================
 *  Pack jobs (size, throughput) onto a single server of fixed capacity to
 *  maximize total throughput. Each job assigned once => 0/1 knapsack.
 *
 *  IDENTIFY: per-job include/exclude + capacity + overlapping (job, cap) states.
 * ============================================================================
 */
public class Solution {

    // 1) RECURSIVE — O(2^n)
    static int solveRecursive(int[] size, int[] tput, int n, int cap) {
        if (n == 0 || cap == 0) return 0;
        if (size[n - 1] <= cap)
            return Math.max(tput[n - 1] + solveRecursive(size, tput, n - 1, cap - size[n - 1]),
                            solveRecursive(size, tput, n - 1, cap));
        return solveRecursive(size, tput, n - 1, cap);
    }

    // 2) MEMOIZED — O(n*cap)
    static int solveMemoized(int[] size, int[] tput, int n, int cap, Integer[][] memo) {
        if (n == 0 || cap == 0) return 0;
        if (memo[n][cap] != null) return memo[n][cap];
        int best = (size[n - 1] <= cap)
                ? Math.max(tput[n - 1] + solveMemoized(size, tput, n - 1, cap - size[n - 1], memo),
                           solveMemoized(size, tput, n - 1, cap, memo))
                : solveMemoized(size, tput, n - 1, cap, memo);
        return memo[n][cap] = best;
    }

    // 3) TABULATION — O(n*cap)
    static int solveTabulation(int[] size, int[] tput, int n, int cap) {
        int[][] dp = new int[n + 1][cap + 1];
        for (int i = 1; i <= n; i++)
            for (int c = 1; c <= cap; c++)
                dp[i][c] = (size[i - 1] <= c)
                        ? Math.max(tput[i - 1] + dp[i - 1][c - size[i - 1]], dp[i - 1][c])
                        : dp[i - 1][c];
        return dp[n][cap];
    }

    public static void main(String[] args) {
        int[] size = {1, 3, 4, 5};
        int[] tput = {1, 4, 5, 7};
        int   cap  = 7;
        int   n    = size.length;

        int r = solveRecursive(size, tput, n, cap);
        int m = solveMemoized(size, tput, n, cap, new Integer[n + 1][cap + 1]);
        int t = solveTabulation(size, tput, n, cap);
        System.out.println("Load balancer, capacity " + cap);
        System.out.println("Recursive  : " + r);
        System.out.println("Memoized   : " + m);
        System.out.println("Tabulation : " + t);
        System.out.println("Jobs size3+size4 (=7) -> throughput 9");
        assert r == 9 && m == 9 && t == 9 : "mismatch!";
    }
}
