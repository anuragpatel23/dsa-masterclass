/**
 * ============================================================================
 *  EQUAL SUM PARTITION  —  Example 3 : Fair team split
 * ============================================================================
 *  Split player skill ratings into two equally-skilled teams (equal sum).
 *  Same reduction to subsetSum(total/2); real-world framing.
 * ============================================================================
 */
public class Solution {

    static boolean subsetRec(int[] a, int n, int s) {
        if (s == 0) return true;
        if (n == 0) return false;
        if (a[n - 1] <= s) return subsetRec(a, n - 1, s - a[n - 1]) || subsetRec(a, n - 1, s);
        return subsetRec(a, n - 1, s);
    }
    static boolean solveRecursive(int[] a) {
        int total = 0; for (int x : a) total += x;
        return (total & 1) == 0 && subsetRec(a, a.length, total / 2);
    }

    static boolean subsetMemo(int[] a, int n, int s, Boolean[][] memo) {
        if (s == 0) return true;
        if (n == 0) return false;
        if (memo[n][s] != null) return memo[n][s];
        boolean res = (a[n - 1] <= s)
                ? subsetMemo(a, n - 1, s - a[n - 1], memo) || subsetMemo(a, n - 1, s, memo)
                : subsetMemo(a, n - 1, s, memo);
        return memo[n][s] = res;
    }
    static boolean solveMemoized(int[] a) {
        int total = 0; for (int x : a) total += x;
        if ((total & 1) == 1) return false;
        int half = total / 2;
        return subsetMemo(a, a.length, half, new Boolean[a.length + 1][half + 1]);
    }

    static boolean solveTabulation(int[] a) {
        int total = 0; for (int x : a) total += x;
        if ((total & 1) == 1) return false;
        int half = total / 2;
        boolean[] dp = new boolean[half + 1]; dp[0] = true;
        for (int x : a) for (int s = half; s >= x; s--) dp[s] = dp[s] || dp[s - x];
        return dp[half];
    }

    public static void main(String[] args) {
        int[] ratings = {10, 20, 15, 5, 25, 5};  // total 80 -> each team 40
        boolean r = solveRecursive(ratings), m = solveMemoized(ratings), t = solveTabulation(ratings);
        System.out.println("Fair split possible? total=80, target 40 -> expected true");
        System.out.println("Recursive=" + r + "  Memoized=" + m + "  Tabulation=" + t);
        assert r && m && t : "mismatch!";
    }
}
