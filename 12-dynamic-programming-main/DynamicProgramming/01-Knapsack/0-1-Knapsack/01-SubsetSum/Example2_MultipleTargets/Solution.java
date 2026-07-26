/**
 * ============================================================================
 *  SUBSET SUM  —  Example 2 : Query multiple targets
 * ============================================================================
 *  Precompute one dp row set, then answer many "is target reachable?" queries.
 *  Shows WHY tabulation shines: build the reachability table once, O(1) lookups.
 *
 *  IDENTIFY: same 0/1 subset-sum core; the twist is amortizing the table.
 * ============================================================================
 */
public class Solution {

    // 1) RECURSIVE (per query) — O(2^n) each
    static boolean solveRecursive(int[] a, int n, int s) {
        if (s == 0) return true;
        if (n == 0) return false;
        if (a[n - 1] <= s)
            return solveRecursive(a, n - 1, s - a[n - 1]) || solveRecursive(a, n - 1, s);
        return solveRecursive(a, n - 1, s);
    }

    // 2) MEMOIZED (per query) — O(n*s) each
    static boolean solveMemoized(int[] a, int n, int s, Boolean[][] memo) {
        if (s == 0) return true;
        if (n == 0) return false;
        if (memo[n][s] != null) return memo[n][s];
        boolean res = (a[n - 1] <= s)
                ? solveMemoized(a, n - 1, s - a[n - 1], memo) || solveMemoized(a, n - 1, s, memo)
                : solveMemoized(a, n - 1, s, memo);
        return memo[n][s] = res;
    }

    // 3) TABULATION — build reachable[0..maxSum] once, then O(1) per query
    static boolean[] buildReachable(int[] a, int maxSum) {
        boolean[] dp = new boolean[maxSum + 1];
        dp[0] = true;
        for (int x : a)                       // 0/1: iterate sums DOWNWARD
            for (int s = maxSum; s >= x; s--)
                dp[s] = dp[s] || dp[s - x];
        return dp;
    }

    public static void main(String[] args) {
        int[] a = {1, 5, 11, 5};
        int n = a.length, maxSum = 22;
        int[] queries = {11, 22, 4, 21};
        boolean[] reach = buildReachable(a, maxSum);

        System.out.println("target | recursive | memo | table");
        boolean allMatch = true;
        for (int q : queries) {
            boolean r = solveRecursive(a, n, q);
            boolean m = solveMemoized(a, n, q, new Boolean[n + 1][q + 1]);
            boolean t = reach[q];
            System.out.printf("  %2d   |   %-5b   | %-4b | %b%n", q, r, m, t);
            allMatch &= (r == m && m == t);
        }
        System.out.println("expected: 11=T 22=T 4=F 21=T");
        assert allMatch : "mismatch!";
    }
}
