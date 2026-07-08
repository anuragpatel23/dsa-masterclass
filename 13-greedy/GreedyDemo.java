import java.util.Arrays;
import java.util.Comparator;

/**
 * Runnable illustrations for Greedy Algorithms.
 * Compile & run:  javac GreedyDemo.java && java GreedyDemo
 *
 * Example 1: activity selection — sort by END time (and why start fails)
 * Example 2: Jump Game II — greedy "window" trace
 * Example 3: Gas Station — restart-point elimination
 */
public class GreedyDemo {

    public static void main(String[] args) {
        example1_activitySelection();
        example2_jumpGame();
        example3_gasStation();
    }

    // ------------------------------------------------------------------
    // EXAMPLE 1: one meeting room, maximize meetings hosted.
    // Compares the CORRECT greedy (earliest end) with the WRONG one
    // (earliest start) on the same input.
    // ------------------------------------------------------------------
    static void example1_activitySelection() {
        System.out.println("=== Example 1: max non-overlapping meetings ===");
        int[][] meetings = {{1, 10}, {2, 3}, {4, 5}, {6, 7}, {8, 9}};
        System.out.println("meetings: " + Arrays.deepToString(meetings));

        // WRONG: earliest start first -> picks [1,10], blocks everything else
        int[][] byStart = meetings.clone();
        Arrays.sort(byStart, Comparator.comparingInt(m -> m[0]));
        System.out.println("sort by START picks [1,10] first -> hosts only 1 meeting  (WRONG)");

        // RIGHT: earliest end first
        int[][] byEnd = meetings.clone();
        Arrays.sort(byEnd, Comparator.comparingInt(m -> m[1]));
        int count = 0, lastEnd = Integer.MIN_VALUE;
        StringBuilder chosen = new StringBuilder();
        for (int[] m : byEnd)
            if (m[0] >= lastEnd) { count++; lastEnd = m[1]; chosen.append(Arrays.toString(m)).append(' '); }
        System.out.println("sort by END   picks " + chosen + "-> hosts " + count + " meetings (RIGHT)");
        System.out.println("Exchange argument: earliest-ending meeting leaves maximum room.\n");
    }

    // ------------------------------------------------------------------
    // EXAMPLE 2: Jump Game II — min jumps. currentEnd is the frontier of
    // the current jump; farthest is the frontier of the NEXT one.
    // ------------------------------------------------------------------
    static void example2_jumpGame() {
        System.out.println("=== Example 2: Jump Game II (min jumps) ===");
        int[] nums = {2, 3, 1, 1, 4, 1, 1};
        System.out.println("input: " + Arrays.toString(nums));
        int jumps = 0, currentEnd = 0, farthest = 0;
        for (int i = 0; i < nums.length - 1; i++) {
            farthest = Math.max(farthest, i + nums[i]);
            if (i == currentEnd) {
                jumps++;
                System.out.printf("  jump %d taken at index %d -> can now reach index %d%n",
                        jumps, i, farthest);
                currentEnd = farthest;
            }
        }
        System.out.println("minimum jumps: " + jumps + "\n");
    }

    // ------------------------------------------------------------------
    // EXAMPLE 3: Gas Station. When the tank goes negative at i, no start
    // in [start..i] can work -> restart from i+1. Single pass.
    // ------------------------------------------------------------------
    static void example3_gasStation() {
        System.out.println("=== Example 3: Gas Station ===");
        int[] gas  = {1, 2, 3, 4, 5};
        int[] cost = {3, 4, 5, 1, 2};
        System.out.println("gas : " + Arrays.toString(gas));
        System.out.println("cost: " + Arrays.toString(cost));

        int total = 0, tank = 0, start = 0;
        for (int i = 0; i < gas.length; i++) {
            int gain = gas[i] - cost[i];
            total += gain;
            tank += gain;
            System.out.printf("  station %d: gain %+d, tank %d%s%n", i, gain, tank,
                    tank < 0 ? "  -> NEGATIVE, restart from " + (i + 1) : "");
            if (tank < 0) { start = i + 1; tank = 0; }
        }
        System.out.println(total >= 0
                ? "start at station " + start + " (total surplus " + total + " guarantees a lap)"
                : "impossible: total gas < total cost");
    }
}
