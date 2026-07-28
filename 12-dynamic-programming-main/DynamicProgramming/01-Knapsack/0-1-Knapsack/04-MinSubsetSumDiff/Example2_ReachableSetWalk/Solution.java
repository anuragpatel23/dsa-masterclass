/**
 * ============================================================================
 *  MIN SUBSET SUM DIFFERENCE  —  Example 2 : Reachable-set walk
 * ============================================================================
 *  Emphasizes the two-phase method: (1) build the boolean reachability row via
 *  the 2-D subset-sum table, (2) walk reachable s1 <= total/2 for the min gap.
 * ============================================================================
 */
public class Solution {

    static int solveRecursive(int[] a, int i, int s1, int total) {
        if (i == a.length) return Math.abs(total - 2 * s1);
        return Math.min(solveRecursive(a, i + 1, s1 + a[i], total),
                        solveRecursive(a, i + 1, s1, total));
    }

    static int solveMemoized(int[] a, int i, int s1, int total, Integer[][] memo) {
        if (i == a.length) return Math.abs(total - 2 * s1);
        if (memo[i][s1] != null) return memo[i][s1];
        return memo[i][s1] = Math.min(solveMemoized(a, i + 1, s1 + a[i], total, memo),
                                      solveMemoized(a, i + 1, s1, total, memo));
    }

    // 3) TABULATION via full 2-D subset-sum table, then walk last row.
    static int solveTabulation(int[] a, int total) {
        int n = a.length;
        boolean[][] dp = new boolean[n + 1][total + 1];
        for (int i = 0; i <= n; i++) dp[i][0] = true;
        for (int i = 1; i <= n; i++)
            for (int s = 1; s <= total; s++)
                dp[i][s] = (a[i - 1] <= s) ? dp[i - 1][s - a[i - 1]] || dp[i - 1][s] : dp[i - 1][s];
        int best = Integer.MAX_VALUE;
        for (int s1 = 0; s1 <= total / 2; s1++)
            if (dp[n][s1]) best = Math.min(best, total - 2 * s1);
        return best;
    }

    public static void main(String[] args) {
        int[] a = {3, 1, 4, 2, 2, 1};   // total 13 -> best split 6 vs 7 -> diff 1
        int total = 0; for (int x : a) total += x;
        int r = solveRecursive(a, 0, 0, total);
        int m = solveMemoized(a, 0, 0, total, new Integer[a.length][total + 1]);
        int t = solveTabulation(a, total);
        System.out.println("Min diff {3,1,4,2,2,1} total=13 -> expected 1");
        System.out.println("Recursive=" + r + "  Memoized=" + m + "  Tabulation=" + t);
        assert r == 1 && m == 1 && t == 1 : "mismatch!";
    }
}
