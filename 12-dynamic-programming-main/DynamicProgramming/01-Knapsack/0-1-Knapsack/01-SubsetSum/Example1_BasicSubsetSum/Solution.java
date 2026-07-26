/**
 * ============================================================================
 *  SUBSET SUM  —  Example 1 : Basic feasibility
 * ============================================================================
 *  Given non-negative ints and a target, can any subset sum to exactly target?
 *
 *  IDENTIFY: 0/1 include-exclude choice against a TARGET SUM (capacity), asking
 *  "is it possible" -> boolean 0-1 knapsack (weight = value = element itself).
 * ============================================================================
 */
public class Solution {

    // 1) RECURSIVE — O(2^n)
    static boolean solveRecursive(int[] a, int n, int s) {
        if (s == 0) return true;          // empty subset achieves 0
        if (n == 0) return false;         // no elements left, s>0
        if (a[n - 1] <= s)
            return solveRecursive(a, n - 1, s - a[n - 1])  // include
                || solveRecursive(a, n - 1, s);            // exclude
        return solveRecursive(a, n - 1, s);
    }

    // 2) MEMOIZED — O(n*sum)
    static boolean solveMemoized(int[] a, int n, int s, Boolean[][] memo) {
        if (s == 0) return true;
        if (n == 0) return false;
        if (memo[n][s] != null) return memo[n][s];
        boolean res = (a[n - 1] <= s)
                ? solveMemoized(a, n - 1, s - a[n - 1], memo) || solveMemoized(a, n - 1, s, memo)
                : solveMemoized(a, n - 1, s, memo);
        return memo[n][s] = res;
    }

    // 3) TABULATION — O(n*sum)
    static boolean solveTabulation(int[] a, int n, int sum) {
        boolean[][] dp = new boolean[n + 1][sum + 1];
        for (int i = 0; i <= n; i++) dp[i][0] = true;   // sum 0 always achievable
        for (int i = 1; i <= n; i++)
            for (int s = 1; s <= sum; s++)
                dp[i][s] = (a[i - 1] <= s)
                        ? dp[i - 1][s - a[i - 1]] || dp[i - 1][s]
                        : dp[i - 1][s];
        return dp[n][sum];
    }

    public static void main(String[] args) {
        int[] a = {2, 3, 7, 8, 10};
        int sum = 11, n = a.length;
        boolean r = solveRecursive(a, n, sum);
        boolean m = solveMemoized(a, n, sum, new Boolean[n + 1][sum + 1]);
        boolean t = solveTabulation(a, n, sum);
        System.out.println("Subset summing to " + sum + " ? (3+8) -> true");
        System.out.println("Recursive=" + r + "  Memoized=" + m + "  Tabulation=" + t);
        assert r && m && t : "mismatch!";
    }
}
