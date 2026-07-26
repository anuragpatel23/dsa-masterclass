/**
 * ============================================================================
 *  EQUAL SUM PARTITION  —  Example 2 : Odd-total rejection
 * ============================================================================
 *  Demonstrates the fast-reject: an odd total can NEVER split into two equal
 *  integer-sum halves. All three approaches short-circuit on parity.
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
        int[] a = {1, 2, 3, 5};   // total 11 (odd) -> false
        boolean r = solveRecursive(a), m = solveMemoized(a), t = solveTabulation(a);
        System.out.println("Equal partition {1,2,3,5}? total=11 odd -> expected false");
        System.out.println("Recursive=" + r + "  Memoized=" + m + "  Tabulation=" + t);
        assert !r && !m && !t : "mismatch!";
    }
}
