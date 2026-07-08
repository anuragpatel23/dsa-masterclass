import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Runnable illustrations of complexity analysis.
 * Compile & run:  javac ComplexityDemo.java && java ComplexityDemo
 *
 * Example 1: growth rates measured empirically (O(n) vs O(n^2) vs O(log n))
 * Example 2: amortized cost — String += vs StringBuilder
 * Example 3: naive vs memoized Fibonacci (O(2^n) vs O(n))
 * Example 4: the "log-outer, i-inner" loop that is O(n), not O(n log n)
 */
public class ComplexityDemo {

    public static void main(String[] args) {
        example1_growthRates();
        example2_amortizedStrings();
        example3_fibonacci();
        example4_geometricLoop();
    }

    // ------------------------------------------------------------------
    // EXAMPLE 1: measure how doubling n changes runtime for each class.
    // Expectation: O(n) doubles, O(n^2) quadruples, O(log n) barely moves.
    // ------------------------------------------------------------------
    static void example1_growthRates() {
        System.out.println("=== Example 1: growth rates (doubling n) ===");
        System.out.printf("%-10s %12s %12s %12s%n", "n", "O(log n) ns", "O(n) us", "O(n^2) ms");

        for (int n = 4_000; n <= 32_000; n *= 2) {
            int[] sorted = new int[n];
            for (int i = 0; i < n; i++) sorted[i] = i * 2;

            long t0 = System.nanoTime();
            binarySearch(sorted, n - 1);                     // O(log n)
            long logNs = System.nanoTime() - t0;

            t0 = System.nanoTime();
            long sum = 0;
            for (int x : sorted) sum += x;                   // O(n)
            long linUs = (System.nanoTime() - t0) / 1_000;

            t0 = System.nanoTime();
            int dupPairs = 0;
            for (int i = 0; i < n; i++)                      // O(n^2) pair scan
                for (int j = i + 1; j < n; j++)
                    if (sorted[i] == sorted[j]) dupPairs++;
            long quadMs = (System.nanoTime() - t0) / 1_000_000;

            System.out.printf("%-10d %12d %12d %12d%n", n, logNs, linUs, quadMs);
            if (sum < 0 || dupPairs > 0) System.out.println("(impossible)");
        }
        System.out.println("Note how the O(n^2) column roughly QUADRUPLES per row " +
                "while O(n) doubles and O(log n) stays flat.\n");
    }

    static int binarySearch(int[] a, int target) {
        int lo = 0, hi = a.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;                    // overflow-safe midpoint
            if (a[mid] == target) return mid;
            if (a[mid] < target) lo = mid + 1;
            else hi = mid - 1;
        }
        return -1;
    }

    // ------------------------------------------------------------------
    // EXAMPLE 2: amortized analysis in the wild.
    // String += copies the whole string every time  -> O(n^2) total.
    // StringBuilder.append is amortized O(1) per char -> O(n) total.
    // ------------------------------------------------------------------
    static void example2_amortizedStrings() {
        System.out.println("=== Example 2: String += vs StringBuilder (n = 50,000) ===");
        int n = 50_000;

        long t0 = System.nanoTime();
        String s = "";
        for (int i = 0; i < n; i++) s += 'x';                // O(n^2)
        long slow = (System.nanoTime() - t0) / 1_000_000;

        t0 = System.nanoTime();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append('x');          // O(n) amortized
        long fast = (System.nanoTime() - t0) / 1_000_000;

        System.out.printf("String +=      : %5d ms   (quadratic: 1+2+...+n copies)%n", slow);
        System.out.printf("StringBuilder  : %5d ms   (amortized O(1) per append)%n", fast);
        System.out.println("Same output length (" + s.length() + " vs " + sb.length()
                + ") — wildly different cost.\n");
    }

    // ------------------------------------------------------------------
    // EXAMPLE 3: recursion-tree explosion vs memoization.
    // fib(40) naive makes ~330 million calls; memoized makes 40.
    // ------------------------------------------------------------------
    static long naiveCalls = 0;

    static void example3_fibonacci() {
        System.out.println("=== Example 3: fib(40) — O(2^n) vs O(n) ===");

        long t0 = System.nanoTime();
        long v1 = fibNaive(40);
        long naiveMs = (System.nanoTime() - t0) / 1_000_000;

        t0 = System.nanoTime();
        long v2 = fibMemo(40, new long[41]);
        long memoUs = (System.nanoTime() - t0) / 1_000;

        System.out.printf("naive   : %d in %d ms  (%,d recursive calls)%n", v1, naiveMs, naiveCalls);
        System.out.printf("memoized: %d in %d us  (each of 40 states computed once)%n", v2, memoUs);
        System.out.println("Same answer; the memo collapses an exponential tree to a line.\n");
    }

    static long fibNaive(int n) {
        naiveCalls++;
        if (n <= 1) return n;
        return fibNaive(n - 1) + fibNaive(n - 2);
    }

    static long fibMemo(int n, long[] memo) {
        if (n <= 1) return n;
        if (memo[n] != 0) return memo[n];
        return memo[n] = fibMemo(n - 1, memo) + fibMemo(n - 2, memo);
    }

    // ------------------------------------------------------------------
    // EXAMPLE 4: the deceptive loop — outer log n, inner i — totals O(n).
    // n + n/2 + n/4 + ... < 2n  (geometric series).
    // ------------------------------------------------------------------
    static void example4_geometricLoop() {
        System.out.println("=== Example 4: for(i=n; i>0; i/=2) for(j=0; j<i; j++) ===");
        System.out.printf("%-12s %-14s %-14s%n", "n", "iterations", "iterations/n");
        for (int n = 1_000_000; n <= 8_000_000; n *= 2) {
            long count = 0;
            for (int i = n; i > 0; i /= 2)
                for (int j = 0; j < i; j++)
                    count++;
            System.out.printf("%-12d %-14d %.3f%n", n, count, (double) count / n);
        }
        System.out.println("iterations/n stays < 2: the loop is O(n), NOT O(n log n).");
        System.out.println("Lesson: sum the TOTAL inner work; don't multiply blindly.");
    }
}
