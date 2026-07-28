/**
 * ============================================================================
 *  MIN SUBSET SUM DIFFERENCE  —  Example 1 : Classic
 * ============================================================================
 *  Split into two subsets minimizing |sum1 - sum2|.
 *  IDENTIFY: subset-sum reachability + minimize (total - 2*s1) over reachable s1.
 * ============================================================================
 */
public class Solution {

    // 1) RECURSIVE — explore include/exclude, track running s1, minimize gap. O(2^n)
    static int solveRecursive(int[] a, int i, int s1, int total) {
        if (i == a.length) return Math.abs(total - 2 * s1);
        int include = solveRecursive(a, i + 1, s1 + a[i], total);
        int exclude = solveRecursive(a, i + 1, s1, total);
        return Math.min(include, exclude);
    }

    // 2) MEMOIZED — state (i, s1). O(n*total)
    static int solveMemoized(int[] a, int i, int s1, int total, Integer[][] memo) {
        if (i == a.length) return Math.abs(total - 2 * s1);
        if (memo[i][s1] != null) return memo[i][s1];
        int include = solveMemoized(a, i + 1, s1 + a[i], total, memo);
        int exclude = solveMemoized(a, i + 1, s1, total, memo);
        return memo[i][s1] = Math.min(include, exclude);
    }

    // 3) TABULATION — reachable subset sums, then scan lower half. O(n*total)
    static int solveTabulation(int[] a, int total) {
        boolean[] dp = new boolean[total + 1];
        dp[0] = true;
        for (int x : a) for (int s = total; s >= x; s--) dp[s] = dp[s] || dp[s - x];
        int best = Integer.MAX_VALUE;
        for (int s1 = 0; s1 <= total / 2; s1++)
            if (dp[s1]) best = Math.min(best, total - 2 * s1);
        return best;
    }

    public static void main(String[] args) {
        int[] a = {1, 6, 11, 5};   // {1,5,6}=12 vs {11}=11 -> diff 1
        int total = 0; for (int x : a) total += x;
        int r = solveRecursive(a, 0, 0, total);
        int m = solveMemoized(a, 0, 0, total, new Integer[a.length][total + 1]);
        int t = solveTabulation(a, total);
        System.out.println("Min subset-sum diff {1,6,11,5} -> expected 1");
        System.out.println("Recursive=" + r + "  Memoized=" + m + "  Tabulation=" + t);
        assert r == 1 && m == 1 && t == 1 : "mismatch!";
    }
}
