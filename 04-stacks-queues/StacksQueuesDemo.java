import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Map;

/**
 * Runnable illustrations for Stacks & Queues.
 * Compile & run:  javac StacksQueuesDemo.java && java StacksQueuesDemo
 *
 * Example 1: valid parentheses with trace
 * Example 2: monotonic stack — Daily Temperatures, step by step
 * Example 3: sliding window maximum with a monotonic deque
 * Example 4: queue built from two stacks (amortized O(1))
 */
public class StacksQueuesDemo {

    public static void main(String[] args) {
        example1_parentheses();
        example2_monotonicStack();
        example3_slidingWindowMax();
        example4_queueFromStacks();
    }

    // ------------------------------------------------------------------
    // EXAMPLE 1: bracket matching — the canonical stack use.
    // ------------------------------------------------------------------
    static void example1_parentheses() {
        System.out.println("=== Example 1: valid parentheses ===");
        for (String s : new String[]{"{[()]}", "([)]", "((("}) {
            System.out.printf("%-8s -> %s%n", s, isValid(s));
        }
        System.out.println();
    }

    static boolean isValid(String s) {
        Deque<Character> stack = new ArrayDeque<>();
        Map<Character, Character> pairs = Map.of(')', '(', ']', '[', '}', '{');
        for (char c : s.toCharArray()) {
            if (pairs.containsValue(c)) stack.push(c);
            else if (stack.isEmpty() || stack.pop() != pairs.get(c)) return false;
        }
        return stack.isEmpty();
    }

    // ------------------------------------------------------------------
    // EXAMPLE 2: Daily Temperatures. The stack holds indices whose answer
    // is still unknown; a warmer day "resolves" them. Each index is pushed
    // and popped at most once -> O(n) despite the nested while.
    // ------------------------------------------------------------------
    static void example2_monotonicStack() {
        System.out.println("=== Example 2: Daily Temperatures (monotonic stack trace) ===");
        int[] temps = {73, 74, 75, 71, 69, 72, 76, 73};
        int[] answer = new int[temps.length];
        Deque<Integer> stack = new ArrayDeque<>();

        System.out.println("temps: " + Arrays.toString(temps));
        for (int i = 0; i < temps.length; i++) {
            while (!stack.isEmpty() && temps[i] > temps[stack.peek()]) {
                int prev = stack.pop();
                answer[prev] = i - prev;
                System.out.printf("  day %d (%d°) resolves day %d (%d°): wait %d days%n",
                        i, temps[i], prev, temps[prev], i - prev);
            }
            stack.push(i);
        }
        System.out.println("answer: " + Arrays.toString(answer));
        System.out.println("(zeros = no warmer day ever found)\n");
    }

    // ------------------------------------------------------------------
    // EXAMPLE 3: sliding window maximum, k=3. The deque front always
    // holds the index of the current window's max.
    // ------------------------------------------------------------------
    static void example3_slidingWindowMax() {
        System.out.println("=== Example 3: sliding window maximum (k=3) ===");
        int[] nums = {1, 3, -1, -3, 5, 3, 6, 7};
        int k = 3;
        Deque<Integer> dq = new ArrayDeque<>();
        int[] result = new int[nums.length - k + 1];
        for (int i = 0; i < nums.length; i++) {
            if (!dq.isEmpty() && dq.peekFirst() <= i - k) dq.pollFirst();     // expired
            while (!dq.isEmpty() && nums[dq.peekLast()] < nums[i]) dq.pollLast(); // dominated
            dq.offerLast(i);
            if (i >= k - 1) result[i - k + 1] = nums[dq.peekFirst()];
        }
        System.out.println("input : " + Arrays.toString(nums));
        System.out.println("maxes : " + Arrays.toString(result));
        System.out.println("Each element enters/leaves the deque once -> O(n) total.\n");
    }

    // ------------------------------------------------------------------
    // EXAMPLE 4: FIFO queue from two LIFO stacks — lazy reversal.
    // ------------------------------------------------------------------
    static void example4_queueFromStacks() {
        System.out.println("=== Example 4: queue from two stacks ===");
        TwoStackQueue q = new TwoStackQueue();
        q.push(1); q.push(2); q.push(3);
        System.out.println("pushed 1,2,3; pop() = " + q.pop() + " (FIFO: expect 1)");
        q.push(4);
        System.out.println("pushed 4;     pop() = " + q.pop() + " (expect 2)");
        System.out.println("              pop() = " + q.pop() + " (expect 3)");
        System.out.println("              pop() = " + q.pop() + " (expect 4)");
        System.out.println("Each element moves in->out at most once: amortized O(1) per op.");
    }

    static class TwoStackQueue {
        private final Deque<Integer> in = new ArrayDeque<>();
        private final Deque<Integer> out = new ArrayDeque<>();

        void push(int x) { in.push(x); }

        int pop() {
            if (out.isEmpty())
                while (!in.isEmpty()) out.push(in.pop());   // reverse once, serve many
            return out.pop();
        }
    }
}
