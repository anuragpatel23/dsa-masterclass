/**
 * ============================================================================
 *  TARGET SUM  —  Example 1 : Classic (LeetCode 494)
 * ============================================================================
 *  Assign +/- to each number so the signed sum == target; count the ways.
 *  IDENTIFY: reduces to countSubsetSum(arr, (target+total)/2).
 * ============================================================================
 */
public class Solution {

    // 1) RECURSIVE — branch +/- at each index. O(2^n)
    static int solveRecursive(int[] a, int i, int running, int target) {
        if (i == a.length) return running == target ? 1 : 0;
        return solveRecursive(a, i + 1, running + a[i], target)
             + solveRecursive(a, i + 1, running - a[i], target);
    }

    // 2) MEMOIZED — state (i, running) offset by total to keep index >= 0.
    static int solveMemoized(int[] a, int i, int running, int target, int total, Integer[][] memo) {
        if (i == a.length) return running == target ? 1 : 0;
        int idx = running + total;                      // shift into [0, 2*total]
        if (memo[i][idx] != null) return memo[i][idx];
        int ways = solveMemoized(a, i + 1, running + a[i], target, total, memo)
                 + solveMemoized(a, i + 1, running - a[i], target, total, memo);
        return memo[i][idx] = ways;
    }

    // 3) TABULATION — count subsets summing to (target+total)/2.
    static int solveTabulation(int[] a, int target) {
        int total = 0; for (int x : a) total += x;
        if ((target + total) % 2 != 0 || Math.abs(target) > total) return 0;
        int s = (target + total) / 2;
        int[] dp = new int[s + 1];
        dp[0] = 1;
        for (int x : a) for (int j = s; j >= x; j--) dp[j] += dp[j - x];
        return dp[s];
    }

    public static void main(String[] args) {
        int[] a = {1, 1, 1, 1, 1};
        int target = 3, total = 5;   // expected 5 ways
        int r = solveRecursive(a, 0, 0, target);
        int m = solveMemoized(a, 0, 0, target, total, new Integer[a.length][2 * total + 1]);
        int t = solveTabulation(a, target);
        System.out.println("Target sum {1,1,1,1,1}, target=3 -> expected 5");
        System.out.println("Recursive=" + r + "  Memoized=" + m + "  Tabulation=" + t);
        assert r == 5 && m == 5 && t == 5 : "mismatch!";
    }
}
