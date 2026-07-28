/**
 * ============================================================================
 *  COUNT OF SUBSET SUM  —  Example 1 : Basic count
 * ============================================================================
 *  Count subsets summing to exactly target.
 *  IDENTIFY: subset-sum "how many ways" -> counting DP (OR becomes +).
 * ============================================================================
 */
public class Solution {

    // 1) RECURSIVE — O(2^n)
    static int solveRecursive(int[] a, int n, int s) {
        if (s == 0) return 1;         // empty subset is one valid way
        if (n == 0) return 0;
        if (a[n - 1] <= s)
            return solveRecursive(a, n - 1, s - a[n - 1]) + solveRecursive(a, n - 1, s);
        return solveRecursive(a, n - 1, s);
    }

    // 2) MEMOIZED — O(n*target)
    static int solveMemoized(int[] a, int n, int s, Integer[][] memo) {
        if (s == 0) return 1;
        if (n == 0) return 0;
        if (memo[n][s] != null) return memo[n][s];
        int res = (a[n - 1] <= s)
                ? solveMemoized(a, n - 1, s - a[n - 1], memo) + solveMemoized(a, n - 1, s, memo)
                : solveMemoized(a, n - 1, s, memo);
        return memo[n][s] = res;
    }

    // 3) TABULATION — O(n*target)
    static int solveTabulation(int[] a, int target) {
        int n = a.length;
        int[][] dp = new int[n + 1][target + 1];
        for (int i = 0; i <= n; i++) dp[i][0] = 1;   // sum 0 -> empty subset
        for (int i = 1; i <= n; i++)
            for (int s = 1; s <= target; s++)
                dp[i][s] = (a[i - 1] <= s)
                        ? dp[i - 1][s - a[i - 1]] + dp[i - 1][s]
                        : dp[i - 1][s];
        return dp[n][target];
    }

    public static void main(String[] args) {
        int[] a = {2, 3, 5, 6, 8, 10};
        int target = 10, n = a.length;   // {2,8},{2,3,5},{10} -> 3 ways
        int r = solveRecursive(a, n, target);
        int m = solveMemoized(a, n, target, new Integer[n + 1][target + 1]);
        int t = solveTabulation(a, target);
        System.out.println("Count subsets summing to " + target + " -> expected 3");
        System.out.println("Recursive=" + r + "  Memoized=" + m + "  Tabulation=" + t);
        assert r == 3 && m == 3 && t == 3 : "mismatch!";
    }
}
