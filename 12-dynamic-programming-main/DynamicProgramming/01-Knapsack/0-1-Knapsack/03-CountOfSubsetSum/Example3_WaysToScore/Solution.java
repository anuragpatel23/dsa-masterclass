/**
 * ============================================================================
 *  COUNT OF SUBSET SUM  —  Example 3 : Ways to reach a score
 * ============================================================================
 *  Given distinct question point-values, in how many ways can a student score
 *  exactly TARGET by answering a subset correctly? Counting subset-sum.
 * ============================================================================
 */
public class Solution {

    static int solveRecursive(int[] pts, int n, int s) {
        if (s == 0) return 1;
        if (n == 0) return 0;
        if (pts[n - 1] <= s)
            return solveRecursive(pts, n - 1, s - pts[n - 1]) + solveRecursive(pts, n - 1, s);
        return solveRecursive(pts, n - 1, s);
    }

    static int solveMemoized(int[] pts, int n, int s, Integer[][] memo) {
        if (s == 0) return 1;
        if (n == 0) return 0;
        if (memo[n][s] != null) return memo[n][s];
        int res = (pts[n - 1] <= s)
                ? solveMemoized(pts, n - 1, s - pts[n - 1], memo) + solveMemoized(pts, n - 1, s, memo)
                : solveMemoized(pts, n - 1, s, memo);
        return memo[n][s] = res;
    }

    static int solveTabulation(int[] pts, int target) {
        int[] dp = new int[target + 1];
        dp[0] = 1;
        for (int x : pts) for (int s = target; s >= x; s--) dp[s] += dp[s - x];
        return dp[target];
    }

    public static void main(String[] args) {
        int[] pts = {2, 4, 6, 8};
        int target = 10, n = pts.length;  // {2,8},{4,6} -> 2 ways
        int r = solveRecursive(pts, n, target);
        int m = solveMemoized(pts, n, target, new Integer[n + 1][target + 1]);
        int t = solveTabulation(pts, target);
        System.out.println("Ways to score exactly " + target + " -> expected 2");
        System.out.println("Recursive=" + r + "  Memoized=" + m + "  Tabulation=" + t);
        assert r == 2 && m == 2 && t == 2 : "mismatch!";
    }
}
