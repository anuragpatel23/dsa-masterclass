/**
 * ============================================================================
 *  SUBSET SUM  —  Example 3 : Resource allocation feasibility
 * ============================================================================
 *  Can we select a subset of task costs that exactly consumes a resource quota?
 *  Real framing of subset-sum; also RECONSTRUCTS which items were chosen.
 *
 *  IDENTIFY: 0/1 include-exclude to hit an exact quota (target sum).
 * ============================================================================
 */
import java.util.ArrayList;
import java.util.List;

public class Solution {

    // 1) RECURSIVE — O(2^n)
    static boolean solveRecursive(int[] c, int n, int quota) {
        if (quota == 0) return true;
        if (n == 0) return false;
        if (c[n - 1] <= quota)
            return solveRecursive(c, n - 1, quota - c[n - 1]) || solveRecursive(c, n - 1, quota);
        return solveRecursive(c, n - 1, quota);
    }

    // 2) MEMOIZED — O(n*quota)
    static boolean solveMemoized(int[] c, int n, int quota, Boolean[][] memo) {
        if (quota == 0) return true;
        if (n == 0) return false;
        if (memo[n][quota] != null) return memo[n][quota];
        boolean res = (c[n - 1] <= quota)
                ? solveMemoized(c, n - 1, quota - c[n - 1], memo) || solveMemoized(c, n - 1, quota, memo)
                : solveMemoized(c, n - 1, quota, memo);
        return memo[n][quota] = res;
    }

    // 3) TABULATION (+ reconstruct chosen items by walking the table back)
    static boolean solveTabulation(int[] c, int n, int quota, List<Integer> chosen) {
        boolean[][] dp = new boolean[n + 1][quota + 1];
        for (int i = 0; i <= n; i++) dp[i][0] = true;
        for (int i = 1; i <= n; i++)
            for (int s = 1; s <= quota; s++)
                dp[i][s] = (c[i - 1] <= s) ? dp[i - 1][s - c[i - 1]] || dp[i - 1][s] : dp[i - 1][s];

        if (dp[n][quota]) {                    // backtrack to recover the subset
            int s = quota;
            for (int i = n; i > 0 && s > 0; i--) {
                if (!dp[i - 1][s]) { chosen.add(c[i - 1]); s -= c[i - 1]; }
            }
        }
        return dp[n][quota];
    }

    public static void main(String[] args) {
        int[] c = {4, 6, 8, 3, 9};
        int quota = 20, n = c.length;
        List<Integer> chosen = new ArrayList<>();
        boolean r = solveRecursive(c, n, quota);
        boolean m = solveMemoized(c, n, quota, new Boolean[n + 1][quota + 1]);
        boolean t = solveTabulation(c, n, quota, chosen);
        System.out.println("Exact quota " + quota + " reachable? " + t + " via " + chosen);
        System.out.println("Recursive=" + r + "  Memoized=" + m + "  Tabulation=" + t);
        assert r && m && t : "mismatch!";
    }
}
