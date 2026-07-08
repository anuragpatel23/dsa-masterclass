import java.util.Arrays;

/**
 * Runnable illustrations for Bit Manipulation.
 * Compile & run:  javac BitsDemo.java && java BitsDemo
 *
 * Example 1: the core tricks, printed in binary so you can SEE them
 * Example 2: XOR magic — Single Number and Missing Number
 * Example 3: subsets of a set via bitmask enumeration
 * Example 4: Java-specific gotchas (>>> vs >>, shift wraparound)
 */
public class BitsDemo {

    public static void main(String[] args) {
        example1_coreTricks();
        example2_xor();
        example3_bitmaskSubsets();
        example4_javaGotchas();
    }

    static String bin(int x) { return String.format("%8s", Integer.toBinaryString(x)).replace(' ', '0'); }

    // ------------------------------------------------------------------
    // EXAMPLE 1: watch each trick operate on actual bits.
    // ------------------------------------------------------------------
    static void example1_coreTricks() {
        System.out.println("=== Example 1: core bit tricks (8-bit view) ===");
        int x = 0b01011010;                                  // 90
        System.out.println("x                =  " + bin(x) + "  (" + x + ")");
        System.out.println("set bit 2        =  " + bin(x | (1 << 2)) + "  (x | 1<<2)");
        System.out.println("clear bit 3      =  " + bin(x & ~(1 << 3)) + "  (x & ~(1<<3))");
        System.out.println("toggle bit 0     =  " + bin(x ^ 1) + "  (x ^ 1)");
        System.out.println("lowest set bit   =  " + bin(x & -x) + "  (x & -x)   <- powers Fenwick trees");
        System.out.println("drop lowest bit  =  " + bin(x & (x - 1)) + "  (x & (x-1)) <- Kernighan");
        System.out.println("popcount(x)      =  " + Integer.bitCount(x) + " set bits");
        System.out.println("isPowerOfTwo(64) =  " + (64 > 0 && (64 & 63) == 0) + "   (x>0 && (x&(x-1))==0)");
        System.out.println();
    }

    // ------------------------------------------------------------------
    // EXAMPLE 2: XOR pairs annihilate — two classics.
    // ------------------------------------------------------------------
    static void example2_xor() {
        System.out.println("=== Example 2: XOR magic ===");

        int[] nums = {4, 1, 2, 1, 2};                        // 4 appears once
        int result = 0;
        for (int n : nums) result ^= n;
        System.out.println("Single Number of " + Arrays.toString(nums) + " = " + result
                + "  (1^1 and 2^2 cancel)");

        int[] arr = {3, 0, 1};                               // 0..3 with one missing
        int x = arr.length;
        for (int i = 0; i < arr.length; i++) x ^= i ^ arr[i];
        System.out.println("Missing Number of " + Arrays.toString(arr) + " (range 0..3) = " + x);
        System.out.println("Both O(n) time, O(1) space — no HashSet needed.\n");
    }

    // ------------------------------------------------------------------
    // EXAMPLE 3: every int in [0, 2^n) IS a subset of an n-element set.
    // ------------------------------------------------------------------
    static void example3_bitmaskSubsets() {
        System.out.println("=== Example 3: subsets of {a, b, c} via bitmask ===");
        char[] items = {'a', 'b', 'c'};
        for (int mask = 0; mask < (1 << items.length); mask++) {
            StringBuilder subset = new StringBuilder("{");
            for (int i = 0; i < items.length; i++)
                if ((mask & (1 << i)) != 0) subset.append(items[i]);
            System.out.printf("  mask %s (%d) -> %s}%n",
                    String.format("%3s", Integer.toBinaryString(mask)).replace(' ', '0'),
                    mask, subset);
        }
        System.out.println("This mapping (int <-> subset) is the basis of bitmask DP.\n");
    }

    // ------------------------------------------------------------------
    // EXAMPLE 4: Java-specific traps interviewers poke at.
    // ------------------------------------------------------------------
    static void example4_javaGotchas() {
        System.out.println("=== Example 4: Java gotchas ===");
        System.out.println("-8 >> 1  = " + (-8 >> 1) + "   (arithmetic shift keeps the sign)");
        System.out.println("-8 >>> 1 = " + (-8 >>> 1) + "   (logical shift fills 0s)");
        System.out.println("1 << 32  = " + (1 << 32) + "   (NOT 0! shift amount is mod 32)");
        System.out.println("1L << 32 = " + (1L << 32) + "   (longs shift mod 64 — use L!)");
        System.out.println("Math.abs(Integer.MIN_VALUE) = " + Math.abs(Integer.MIN_VALUE)
                + "  (still negative — no positive counterpart!)");
        System.out.println("Precedence: 'x & 1 == 0' compiles as 'x & (1==0)' -> ALWAYS parenthesize.");
    }
}
