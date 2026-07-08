import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Runnable illustrations for Two Pointers & Sliding Window.
 * Compile & run:  javac TwoPointersDemo.java && java TwoPointersDemo
 *
 * Example 1: Two Sum on sorted input — converging pointers trace
 * Example 2: Container With Most Water — why we move the shorter wall
 * Example 3: longest substring without repeating chars — window trace
 * Example 4: minimum window substring
 */
public class TwoPointersDemo {

    public static void main(String[] args) {
        example1_twoSumSorted();
        example2_container();
        example3_longestUnique();
        example4_minWindow();
    }

    // ------------------------------------------------------------------
    // EXAMPLE 1: each comparison permanently rules out one element.
    // ------------------------------------------------------------------
    static void example1_twoSumSorted() {
        System.out.println("=== Example 1: Two Sum on sorted array (target 13) ===");
        int[] a = {1, 3, 4, 6, 8, 10, 13};
        int lo = 0, hi = a.length - 1, target = 13;
        while (lo < hi) {
            int sum = a[lo] + a[hi];
            System.out.printf("  a[%d]=%d + a[%d]=%d = %-3d %s%n", lo, a[lo], hi, a[hi], sum,
                    sum == target ? "FOUND" : sum < target ? "-> need bigger, lo++" : "-> need smaller, hi--");
            if (sum == target) break;
            if (sum < target) lo++; else hi--;
        }
        System.out.println("Each step discards one index forever -> O(n) total.\n");
    }

    // ------------------------------------------------------------------
    // EXAMPLE 2: Container With Most Water — area limited by the SHORTER
    // wall, so moving the taller one can never help.
    // ------------------------------------------------------------------
    static void example2_container() {
        System.out.println("=== Example 2: Container With Most Water ===");
        int[] h = {1, 8, 6, 2, 5, 4, 8, 3, 7};
        System.out.println("heights: " + Arrays.toString(h));
        int lo = 0, hi = h.length - 1, best = 0;
        while (lo < hi) {
            int area = Math.min(h[lo], h[hi]) * (hi - lo);
            if (area > best) {
                best = area;
                System.out.printf("  new best: walls at %d,%d height min(%d,%d) width %d -> area %d%n",
                        lo, hi, h[lo], h[hi], hi - lo, area);
            }
            if (h[lo] < h[hi]) lo++; else hi--;              // move the SHORTER wall
        }
        System.out.println("max area = " + best + "\n");
    }

    // ------------------------------------------------------------------
    // EXAMPLE 3: variable window — expand right, jump left past dupes.
    // ------------------------------------------------------------------
    static void example3_longestUnique() {
        System.out.println("=== Example 3: longest substring without repeats ===");
        String s = "abcabcbb";
        Map<Character, Integer> lastSeen = new HashMap<>();
        int left = 0, best = 0, bestStart = 0;
        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            if (lastSeen.containsKey(c) && lastSeen.get(c) >= left) {
                System.out.printf("  '%c' repeats at %d -> left jumps %d -> %d%n",
                        c, right, left, lastSeen.get(c) + 1);
                left = lastSeen.get(c) + 1;
            }
            lastSeen.put(c, right);
            if (right - left + 1 > best) { best = right - left + 1; bestStart = left; }
        }
        System.out.printf("\"%s\" -> longest \"%s\" (length %d)%n%n",
                s, s.substring(bestStart, bestStart + best), best);
    }

    // ------------------------------------------------------------------
    // EXAMPLE 4: minimum window substring — expand until valid, then
    // shrink while still valid.
    // ------------------------------------------------------------------
    static void example4_minWindow() {
        System.out.println("=== Example 4: minimum window substring ===");
        String s = "ADOBECODEBANC", t = "ABC";
        int[] need = new int[128];
        for (char c : t.toCharArray()) need[c]++;
        int required = t.length();
        int left = 0, bestLen = Integer.MAX_VALUE, bestStart = 0;

        for (int right = 0; right < s.length(); right++) {
            if (need[s.charAt(right)]-- > 0) required--;
            while (required == 0) {                          // valid — record & shrink
                if (right - left + 1 < bestLen) {
                    bestLen = right - left + 1;
                    bestStart = left;
                    System.out.printf("  valid window \"%s\" [%d,%d]%n",
                            s.substring(left, right + 1), left, right);
                }
                if (need[s.charAt(left++)]++ == 0) required++;
            }
        }
        System.out.printf("s=\"%s\", t=\"%s\" -> smallest window \"%s\"%n",
                s, t, s.substring(bestStart, bestStart + bestLen));
        System.out.println("Both pointers only move right -> O(n).");
    }
}
