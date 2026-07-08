import java.util.Arrays;
import java.util.Set;

/**
 * Runnable illustrations for Dynamic Programming.
 * Compile & run:  javac DPDemo.java && java DPDemo
 *
 * Example 1: House Robber — the dp table printed step by step
 * Example 2: Coin Change — table fill + greedy counterexample
 * Example 3: Longest Common Subsequence — full 2D table rendered
 * Example 4: Word Break — prefix table trace
 */
public class DPDemo {

    public static void main(String[] args) {
        example1_houseRobber();
        example2_coinChange();
        example3_lcs();
        example4_wordBreak();
    }

    // ------------------------------------------------------------------
    // EXAMPLE 1: House Robber. dp[i] = max(dp[i-1], dp[i-2] + loot[i]).
    // ------------------------------------------------------------------
    static void example1_houseRobber() {
        System.out.println("=== Example 1: House Robber ===");
        int[] loot = {2, 7, 9, 3, 1};
        System.out.println("house values: " + Arrays.toString(loot));
        System.out.printf("%-8s %-8s %-24s%n", "house", "value", "best so far (skip vs rob)");

        int prev2 = 0, prev1 = 0;
        for (int i = 0; i < loot.length; i++) {
            int cur = Math.max(prev1, prev2 + loot[i]);
            System.out.printf("%-8d %-8d max(%d, %d+%d) = %d%n",
                    i, loot[i], prev1, prev2, loot[i], cur);
            prev2 = prev1;
            prev1 = cur;
        }
        System.out.println("answer: " + prev1 + "  (rob houses 0,2,4 -> 2+9+1 = 12)\n");
    }

    // ------------------------------------------------------------------
    // EXAMPLE 2: Coin Change with coins {1,3,4}, amount 6.
    // ALSO shows why greedy (largest coin first) fails on this system.
    // ------------------------------------------------------------------
    static void example2_coinChange() {
        System.out.println("=== Example 2: Coin Change (coins {1,3,4}, amount 6) ===");
        int[] coins = {1, 3, 4};
        int amount = 6;

        int[] dp = new int[amount + 1];
        Arrays.fill(dp, amount + 1);
        dp[0] = 0;
        for (int a = 1; a <= amount; a++)
            for (int coin : coins)
                if (coin <= a) dp[a] = Math.min(dp[a], dp[a - coin] + 1);
        System.out.println("dp table (amount -> min coins): " + Arrays.toString(dp));
        System.out.println("DP answer for 6: " + dp[6] + " coins (3+3)");

        // Greedy: always take the biggest coin that fits
        int remaining = amount, greedyCoins = 0;
        StringBuilder picks = new StringBuilder();
        for (int i = coins.length - 1; i >= 0; ) {
            if (coins[i] <= remaining) {
                remaining -= coins[i];
                greedyCoins++;
                picks.append(coins[i]).append(' ');
            } else {
                i--;
            }
        }
        System.out.println("Greedy answer: " + greedyCoins + " coins ("
                + picks.toString().trim() + ")");
        System.out.println("Greedy 4+1+1=3 loses to DP 3+3=2 -> local best != global best here.\n");
    }

    // ------------------------------------------------------------------
    // EXAMPLE 3: LCS of "ABCBDAB" and "BDCABA" with the full table.
    // ------------------------------------------------------------------
    static void example3_lcs() {
        System.out.println("=== Example 3: Longest Common Subsequence ===");
        String a = "ABCBDAB", b = "BDCABA";
        int m = a.length(), n = b.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++)
            for (int j = 1; j <= n; j++)
                dp[i][j] = a.charAt(i - 1) == b.charAt(j - 1)
                         ? dp[i - 1][j - 1] + 1
                         : Math.max(dp[i - 1][j], dp[i][j - 1]);

        System.out.println("a = " + a + ", b = " + b);
        System.out.print("      ");
        for (char c : b.toCharArray()) System.out.print(c + "  ");
        System.out.println();
        for (int i = 1; i <= m; i++) {
            System.out.print("  " + a.charAt(i - 1) + " ");
            for (int j = 1; j <= n; j++) System.out.printf("%2d ", dp[i][j]);
            System.out.println();
        }

        // Reconstruct the actual subsequence by walking backwards
        StringBuilder lcs = new StringBuilder();
        int i = m, j = n;
        while (i > 0 && j > 0) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) { lcs.append(a.charAt(i - 1)); i--; j--; }
            else if (dp[i - 1][j] >= dp[i][j - 1]) i--;
            else j--;
        }
        System.out.println("LCS length = " + dp[m][n] + ", one LCS = " + lcs.reverse() + "\n");
    }

    // ------------------------------------------------------------------
    // EXAMPLE 4: Word Break — dp over prefixes.
    // ------------------------------------------------------------------
    static void example4_wordBreak() {
        System.out.println("=== Example 4: Word Break ===");
        String s = "catsanddog";
        Set<String> dict = Set.of("cat", "cats", "and", "sand", "dog");
        boolean[] dp = new boolean[s.length() + 1];
        dp[0] = true;
        for (int i = 1; i <= s.length(); i++)
            for (int j = 0; j < i; j++)
                if (dp[j] && dict.contains(s.substring(j, i))) {
                    dp[i] = true;
                    System.out.printf("  prefix \"%s\" = OK-prefix \"%s\" + word \"%s\"%n",
                            s.substring(0, i), s.substring(0, j), s.substring(j, i));
                    break;
                }
                System.out.println("\"" + s + "\" breakable: " + dp[s.length()]
                + "  (cats+and+dog or cat+sand+dog)");
    }
}
