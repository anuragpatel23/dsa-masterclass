/**
 * ============================================================================
 *  MIN SUBSET SUM DIFFERENCE  —  Example 3 : Balance two truck loads
 * ============================================================================
 *  Distribute package weights across two trucks to minimize the load imbalance.
 *  Real framing of min subset-sum difference.
 * ============================================================================
 */
public class Solution {

    static int solveRecursive(int[] w, int i, int s1, int total) {
        if (i == w.length) return Math.abs(total - 2 * s1);
        return Math.min(solveRecursive(w, i + 1, s1 + w[i], total),
                        solveRecursive(w, i + 1, s1, total));
    }

    static int solveMemoized(int[] w, int i, int s1, int total, Integer[][] memo) {
        if (i == w.length) return Math.abs(total - 2 * s1);
        if (memo[i][s1] != null) return memo[i][s1];
        return memo[i][s1] = Math.min(solveMemoized(w, i + 1, s1 + w[i], total, memo),
                                      solveMemoized(w, i + 1, s1, total, memo));
    }

    static int solveTabulation(int[] w, int total) {
        boolean[] dp = new boolean[total + 1];
        dp[0] = true;
        for (int x : w) for (int s = total; s >= x; s--) dp[s] = dp[s] || dp[s - x];
        int best = Integer.MAX_VALUE;
        for (int s1 = 0; s1 <= total / 2; s1++)
            if (dp[s1]) best = Math.min(best, total - 2 * s1);
        return best;
    }

    public static void main(String[] args) {
        int[] w = {10, 20, 30, 40, 50};   // total 150 -> 70 vs 80 -> diff 10
        int total = 0; for (int x : w) total += x;
        int r = solveRecursive(w, 0, 0, total);
        int m = solveMemoized(w, 0, 0, total, new Integer[w.length][total + 1]);
        int t = solveTabulation(w, total);
        System.out.println("Truck load imbalance {10..50} total=150 -> expected 10");
        System.out.println("Recursive=" + r + "  Memoized=" + m + "  Tabulation=" + t);
        assert r == 10 && m == 10 && t == 10 : "mismatch!";
    }
}
