/**
 * ============================================================================
 *  TARGET SUM  —  Example 3 : Impossible target (fast reject)
 * ============================================================================
 *  When (target + total) is odd, or |target| > total, there are ZERO ways.
 *  All three approaches converge to 0.
 * ============================================================================
 */
public class Solution {

    static int solveRecursive(int[] a, int i, int running, int target) {
        if (i == a.length) return running == target ? 1 : 0;
        return solveRecursive(a, i + 1, running + a[i], target)
             + solveRecursive(a, i + 1, running - a[i], target);
    }

    static int solveMemoized(int[] a, int i, int running, int target, int total, Integer[][] memo) {
        if (i == a.length) return running == target ? 1 : 0;
        int idx = running + total;
        if (memo[i][idx] != null) return memo[i][idx];
        int ways = solveMemoized(a, i + 1, running + a[i], target, total, memo)
                 + solveMemoized(a, i + 1, running - a[i], target, total, memo);
        return memo[i][idx] = ways;
    }

    static int solveTabulation(int[] a, int target) {
        int total = 0; for (int x : a) total += x;
        if ((target + total) % 2 != 0 || Math.abs(target) > total) return 0;
        int s = (target + total) / 2;
        int[] dp = new int[s + 1]; dp[0] = 1;
        for (int x : a) for (int j = s; j >= x; j--) dp[j] += dp[j - x];
        return dp[s];
    }

    public static void main(String[] args) {
        int[] a = {1, 2, 3};
        int target = 100, total = 6;   // |target| > total -> 0
        int r = solveRecursive(a, 0, 0, target);
        int m = solveMemoized(a, 0, 0, target, total, new Integer[a.length][2 * total + 1]);
        int t = solveTabulation(a, target);
        System.out.println("Target sum {1,2,3}, target=100 -> expected 0 (impossible)");
        System.out.println("Recursive=" + r + "  Memoized=" + m + "  Tabulation=" + t);
        assert r == 0 && m == 0 && t == 0 : "mismatch!";
    }
}
