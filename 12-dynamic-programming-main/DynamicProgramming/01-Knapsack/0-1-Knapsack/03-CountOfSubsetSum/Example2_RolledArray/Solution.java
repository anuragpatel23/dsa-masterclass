/**
 * ============================================================================
 *  COUNT OF SUBSET SUM  —  Example 2 : Space-optimized 1-D array
 * ============================================================================
 *  Same count, but tabulation uses a single int[target+1] rolled DOWNWARD
 *  (the hallmark of 0/1 knapsack space optimization).
 * ============================================================================
 */
public class Solution {

    static int solveRecursive(int[] a, int n, int s) {
        if (s == 0) return 1;
        if (n == 0) return 0;
        if (a[n - 1] <= s)
            return solveRecursive(a, n - 1, s - a[n - 1]) + solveRecursive(a, n - 1, s);
        return solveRecursive(a, n - 1, s);
    }

    static int solveMemoized(int[] a, int n, int s, Integer[][] memo) {
        if (s == 0) return 1;
        if (n == 0) return 0;
        if (memo[n][s] != null) return memo[n][s];
        int res = (a[n - 1] <= s)
                ? solveMemoized(a, n - 1, s - a[n - 1], memo) + solveMemoized(a, n - 1, s, memo)
                : solveMemoized(a, n - 1, s, memo);
        return memo[n][s] = res;
    }

    // 3) TABULATION — O(target) SPACE. Iterate s downward so each item used once.
    static int solveTabulation(int[] a, int target) {
        int[] dp = new int[target + 1];
        dp[0] = 1;
        for (int x : a)
            for (int s = target; s >= x; s--)   // DOWNWARD => 0/1 (no reuse)
                dp[s] += dp[s - x];
        return dp[target];
    }

    public static void main(String[] args) {
        int[] a = {1, 1, 2, 3};
        int target = 4, n = a.length;  // {1,3},{1,3},{1,1,2} -> 3 ways
        int r = solveRecursive(a, n, target);
        int m = solveMemoized(a, n, target, new Integer[n + 1][target + 1]);
        int t = solveTabulation(a, target);
        System.out.println("Count subsets summing to " + target + " -> expected 3");
        System.out.println("Recursive=" + r + "  Memoized=" + m + "  Tabulation=" + t);
        assert r == 3 && m == 3 && t == 3 : "mismatch!";
    }
}
