import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * Runnable illustrations for Hashing.
 * Compile & run:  javac HashingDemo.java && java HashingDemo
 *
 * Example 1: Two Sum — the canonical HashMap optimization (O(n^2) -> O(n))
 * Example 2: the hashCode/equals contract — watch a key "disappear"
 * Example 3: Longest Consecutive Sequence with a HashSet
 * Example 4: RandomizedSet — insert/delete/getRandom all O(1)
 */
public class HashingDemo {

    public static void main(String[] args) {
        example1_twoSum();
        example2_brokenHashCode();
        example3_longestConsecutive();
        example4_randomizedSet();
    }

    // ------------------------------------------------------------------
    // EXAMPLE 1: Two Sum. For each element ask "have I already seen my
    // complement?" — one pass, one map.
    // ------------------------------------------------------------------
    static void example1_twoSum() {
        System.out.println("=== Example 1: Two Sum ===");
        int[] nums = {2, 7, 11, 15, 3};
        int target = 14;                                    // 11 + 3
        Map<Integer, Integer> seen = new HashMap<>();       // value -> index
        for (int i = 0; i < nums.length; i++) {
            Integer j = seen.get(target - nums[i]);
            if (j != null) {
                System.out.printf("nums=%s target=%d -> indices [%d, %d] (values %d + %d)%n%n",
                        Arrays.toString(nums), target, j, i, nums[j], nums[i]);
                return;
            }
            seen.put(nums[i], i);
        }
    }

    // ------------------------------------------------------------------
    // EXAMPLE 2: violate the contract and watch lookups break.
    // BadPoint implements equals but NOT hashCode.
    // ------------------------------------------------------------------
    static void example2_brokenHashCode() {
        System.out.println("=== Example 2: hashCode/equals contract ===");

        Set<BadPoint> bad = new HashSet<>();
        bad.add(new BadPoint(1, 2));
        System.out.println("BadPoint  (equals only) : contains(new (1,2))? "
                + bad.contains(new BadPoint(1, 2)) + "   <- FALSE: wrong bucket probed");

        Set<GoodPoint> good = new HashSet<>();
        good.add(new GoodPoint(1, 2));
        System.out.println("GoodPoint (both methods): contains(new (1,2))? "
                + good.contains(new GoodPoint(1, 2)));
        System.out.println("Rule: equal objects MUST have equal hash codes.\n");
    }

    static class BadPoint {
        final int x, y;
        BadPoint(int x, int y) { this.x = x; this.y = y; }
        @Override public boolean equals(Object o) {
            return o instanceof BadPoint && ((BadPoint) o).x == x && ((BadPoint) o).y == y;
        }
        // hashCode NOT overridden — inherits identity hash — contract broken!
    }

    static class GoodPoint {
        final int x, y;
        GoodPoint(int x, int y) { this.x = x; this.y = y; }
        @Override public boolean equals(Object o) {
            return o instanceof GoodPoint && ((GoodPoint) o).x == x && ((GoodPoint) o).y == y;
        }
        @Override public int hashCode() { return Objects.hash(x, y); }
    }

    // ------------------------------------------------------------------
    // EXAMPLE 3: Longest Consecutive Sequence in O(n).
    // Key insight: only start counting from sequence STARTS (n-1 absent).
    // ------------------------------------------------------------------
    static void example3_longestConsecutive() {
        System.out.println("=== Example 3: Longest Consecutive Sequence ===");
        int[] nums = {100, 4, 200, 1, 3, 2, 101, 102, 103, 104};
        Set<Integer> set = new HashSet<>();
        for (int n : nums) set.add(n);

        int best = 0, bestStart = 0;
        for (int n : set) {
            if (set.contains(n - 1)) continue;              // not a start — skip (this
            int len = 1;                                    //  guard makes it O(n))
            while (set.contains(n + len)) len++;
            if (len > best) { best = len; bestStart = n; }
        }
        System.out.println("input: " + Arrays.toString(nums));
        System.out.printf("longest run: %d..%d (length %d)%n%n", bestStart, bestStart + best - 1, best);
    }

    // ------------------------------------------------------------------
    // EXAMPLE 4: RandomizedSet — ArrayList + HashMap, delete by
    // swap-with-last. All three ops O(1).
    // ------------------------------------------------------------------
    static void example4_randomizedSet() {
        System.out.println("=== Example 4: Insert/Delete/GetRandom O(1) ===");
        RandomizedSet rs = new RandomizedSet();
        rs.insert(10); rs.insert(20); rs.insert(30); rs.insert(40);
        System.out.println("after inserts       : " + rs.list);
        rs.remove(20);
        System.out.println("after remove(20)    : " + rs.list + "   (40 swapped into 20's slot)");
        System.out.print("five getRandom calls: ");
        for (int i = 0; i < 5; i++) System.out.print(rs.getRandom() + " ");
        System.out.println("\nWhy ArrayList+Map: list gives O(1) random index, map gives O(1) locate.");
    }

    static class RandomizedSet {
        final List<Integer> list = new ArrayList<>();
        final Map<Integer, Integer> indexOf = new HashMap<>();
        final Random rand = new Random(42);

        boolean insert(int val) {
            if (indexOf.containsKey(val)) return false;
            indexOf.put(val, list.size());
            list.add(val);
            return true;
        }

        boolean remove(int val) {
            Integer i = indexOf.get(val);
            if (i == null) return false;
            int last = list.get(list.size() - 1);
            list.set(i, last);                              // fill the hole with last
            indexOf.put(last, i);
            list.remove(list.size() - 1);
            indexOf.remove(val);
            return true;
        }

        int getRandom() { return list.get(rand.nextInt(list.size())); }
    }
}
