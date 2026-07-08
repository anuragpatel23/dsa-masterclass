import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Runnable illustrations for Arrays & Strings.
 * Compile & run:  javac ArraysStringsDemo.java && java ArraysStringsDemo
 *
 * Example 1: Prefix sums — bank-statement style range queries
 * Example 2: Kadane's algorithm with a step-by-step trace
 * Example 3: Rotate array via triple reversal (in place)
 * Example 4: Subarray Sum Equals K (prefix + HashMap)
 * Example 5: Longest palindromic substring (expand around center)
 */
public class ArraysStringsDemo {

    public static void main(String[] args) {
        example1_prefixSums();
        example2_kadaneTrace();
        example3_rotate();
        example4_subarraySumK();
        example5_palindrome();
    }

    // ------------------------------------------------------------------
    // EXAMPLE 1: prefix sums. Daily profits; answer "total for days l..r"
    // in O(1) after O(n) preprocessing.
    // ------------------------------------------------------------------
    static void example1_prefixSums() {
        System.out.println("=== Example 1: Prefix sums (daily profits) ===");
        int[] profit = {120, -40, 300, 75, -10, 220, 90};   // Mon..Sun
        long[] prefix = new long[profit.length + 1];
        for (int i = 0; i < profit.length; i++) prefix[i + 1] = prefix[i] + profit[i];

        System.out.println("profits : " + Arrays.toString(profit));
        System.out.println("prefix  : " + Arrays.toString(prefix));
        System.out.println("Sum Tue..Fri (idx 1..4) = prefix[5]-prefix[1] = "
                + (prefix[5] - prefix[1]));                 // -40+300+75-10 = 325
        System.out.println("Sum Sat..Sun (idx 5..6) = prefix[7]-prefix[5] = "
                + (prefix[7] - prefix[5]) + "\n");          // 220+90 = 310
    }

    // ------------------------------------------------------------------
    // EXAMPLE 2: Kadane — max subarray sum, tracing each decision:
    // "extend the running streak or start fresh at this element?"
    // ------------------------------------------------------------------
    static void example2_kadaneTrace() {
        System.out.println("=== Example 2: Kadane's algorithm (trace) ===");
        int[] nums = {-2, 1, -3, 4, -1, 2, 1, -5, 4};
        System.out.println("input: " + Arrays.toString(nums));
        System.out.printf("%-6s %-8s %-22s %-8s%n", "i", "num", "current(max(x,cur+x))", "best");

        int best = nums[0], current = nums[0];
        System.out.printf("%-6d %-8d %-22d %-8d%n", 0, nums[0], current, best);
        for (int i = 1; i < nums.length; i++) {
            current = Math.max(nums[i], current + nums[i]);
            best = Math.max(best, current);
            System.out.printf("%-6d %-8d %-22d %-8d%n", i, nums[i], current, best);
        }
        System.out.println("Answer: " + best + "  (subarray [4,-1,2,1])\n");
    }

    // ------------------------------------------------------------------
    // EXAMPLE 3: rotate right by k using three reversals, O(1) space.
    // ------------------------------------------------------------------
    static void example3_rotate() {
        System.out.println("=== Example 3: Rotate right by k=3 (triple reverse) ===");
        int[] a = {1, 2, 3, 4, 5, 6, 7};
        System.out.println("start          : " + Arrays.toString(a));
        int k = 3 % a.length;
        reverse(a, 0, a.length - 1);
        System.out.println("reverse all    : " + Arrays.toString(a));
        reverse(a, 0, k - 1);
        System.out.println("reverse [0,k)  : " + Arrays.toString(a));
        reverse(a, k, a.length - 1);
        System.out.println("reverse [k,n)  : " + Arrays.toString(a) + "  <- rotated!\n");
    }

    static void reverse(int[] a, int i, int j) {
        while (i < j) { int t = a[i]; a[i++] = a[j]; a[j--] = t; }
    }

    // ------------------------------------------------------------------
    // EXAMPLE 4: count subarrays summing to k. Works with negatives
    // (sliding window would NOT). prefix + HashMap, O(n).
    // ------------------------------------------------------------------
    static void example4_subarraySumK() {
        System.out.println("=== Example 4: Subarray Sum Equals K ===");
        int[] nums = {3, 4, -7, 1, 3, 3, 1, -4};
        int k = 7;
        Map<Long, Integer> seen = new HashMap<>();
        seen.put(0L, 1);                                    // empty prefix
        long running = 0;
        int count = 0;
        for (int x : nums) {
            running += x;
            count += seen.getOrDefault(running - k, 0);     // earlier prefix p with running-p=k
            seen.merge(running, 1, Integer::sum);
        }
        System.out.println("input: " + Arrays.toString(nums) + ", k=" + k);
        System.out.println("subarrays summing to 7: " + count
                + "  e.g. [3,4], [3,4,-7,1,3,3], [3,3,1], [4,-7,1,3,3,1,-4]... \n");
    }

    // ------------------------------------------------------------------
    // EXAMPLE 5: longest palindromic substring, expanding around each
    // center (2n-1 centers: letters and gaps).
    // ------------------------------------------------------------------
    static void example5_palindrome() {
        System.out.println("=== Example 5: Longest palindromic substring ===");
        for (String s : new String[]{"babad", "forgeeksskeegfor"}) {
            int start = 0, maxLen = 0;
            for (int i = 0; i < s.length(); i++) {
                int len = Math.max(expand(s, i, i), expand(s, i, i + 1));
                if (len > maxLen) { maxLen = len; start = i - (len - 1) / 2; }
            }
            System.out.println("\"" + s + "\" -> \"" + s.substring(start, start + maxLen) + "\"");
        }
        System.out.println("O(n^2) time, O(1) space — good enough unless asked for Manacher.");
    }

    static int expand(String s, int l, int r) {
        while (l >= 0 && r < s.length() && s.charAt(l) == s.charAt(r)) { l--; r++; }
        return r - l - 1;
    }
}
