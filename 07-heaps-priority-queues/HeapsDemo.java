import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Runnable illustrations for Heaps & Priority Queues.
 * Compile & run:  javac HeapsDemo.java && java HeapsDemo
 *
 * Example 1: hand-built MinHeap with sift up/down traces
 * Example 2: Top-K pattern — Kth largest via size-K min-heap
 * Example 3: running median with two heaps (streaming)
 * Example 4: Meeting Rooms II — min rooms via heap of end times
 */
public class HeapsDemo {

    public static void main(String[] args) {
        example1_minHeap();
        example2_topK();
        example3_runningMedian();
        example4_meetingRooms();
    }

    // ------------------------------------------------------------------
    // EXAMPLE 1: build-your-own heap; print the array after each op to
    // see the implicit tree (parent i -> children 2i+1, 2i+2).
    // ------------------------------------------------------------------
    static void example1_minHeap() {
        System.out.println("=== Example 1: hand-built MinHeap ===");
        MinHeap heap = new MinHeap();
        for (int v : new int[]{7, 3, 9, 1, 5}) {
            heap.offer(v);
            System.out.printf("offer(%d) -> array %s  (root=min=%d)%n", v, heap.a, heap.peek());
        }
        System.out.print("draining: ");
        while (!heap.a.isEmpty()) System.out.print(heap.poll() + " ");
        System.out.println(" <- sorted! (this is heapsort's core)\n");
    }

    static class MinHeap {
        final List<Integer> a = new ArrayList<>();

        int peek() { return a.get(0); }

        void offer(int v) {
            a.add(v);
            int i = a.size() - 1;
            while (i > 0) {                                  // sift up
                int parent = (i - 1) / 2;
                if (a.get(parent) <= a.get(i)) break;
                Collections.swap(a, parent, i);
                i = parent;
            }
        }

        int poll() {
            int top = a.get(0);
            int last = a.remove(a.size() - 1);
            if (!a.isEmpty()) {
                a.set(0, last);
                int i = 0;                                   // sift down
                while (true) {
                    int l = 2 * i + 1, r = 2 * i + 2, smallest = i;
                    if (l < a.size() && a.get(l) < a.get(smallest)) smallest = l;
                    if (r < a.size() && a.get(r) < a.get(smallest)) smallest = r;
                    if (smallest == i) break;
                    Collections.swap(a, i, smallest);
                    i = smallest;
                }
            }
            return top;
        }
    }

    // ------------------------------------------------------------------
    // EXAMPLE 2: 3rd largest of a "stream" using a min-heap of size 3.
    // The heap holds the current top-3; its ROOT is the 3rd largest.
    // ------------------------------------------------------------------
    static void example2_topK() {
        System.out.println("=== Example 2: Kth largest (k=3) from a stream ===");
        int[] stream = {3, 2, 1, 5, 6, 4, 8, 7};
        Queue<Integer> minHeap = new PriorityQueue<>();
        for (int n : stream) {
            minHeap.offer(n);
            if (minHeap.size() > 3) minHeap.poll();          // evict smallest of elite
            System.out.printf("saw %d -> top-3 = %-12s 3rd largest = %d%n",
                    n, minHeap, minHeap.peek());
        }
        System.out.println("O(n log k) time, O(k) memory — works on infinite streams.\n");
    }

    // ------------------------------------------------------------------
    // EXAMPLE 3: running median. lo = max-heap (lower half),
    // hi = min-heap (upper half); sizes kept within 1.
    // ------------------------------------------------------------------
    static void example3_runningMedian() {
        System.out.println("=== Example 3: running median of a stream ===");
        Queue<Integer> lo = new PriorityQueue<>(Comparator.reverseOrder());
        Queue<Integer> hi = new PriorityQueue<>();
        for (int n : new int[]{5, 15, 1, 3, 8, 7, 9, 10, 20, 100}) {
            lo.offer(n);
            hi.offer(lo.poll());                             // guarantee lo max <= hi min
            if (hi.size() > lo.size()) lo.offer(hi.poll());  // rebalance
            double median = lo.size() > hi.size() ? lo.peek() : (lo.peek() + hi.peek()) / 2.0;
            System.out.printf("add %-4d -> median %.1f%n", n, median);
        }
        System.out.println();
    }

    // ------------------------------------------------------------------
    // EXAMPLE 4: Meeting Rooms II. Sort by start; heap of end times =
    // rooms in use; reuse a room when the earliest end <= next start.
    // ------------------------------------------------------------------
    static void example4_meetingRooms() {
        System.out.println("=== Example 4: Meeting Rooms II ===");
        int[][] meetings = {{0, 30}, {5, 10}, {15, 20}, {35, 40}, {38, 45}};
        Arrays.sort(meetings, Comparator.comparingInt(m -> m[0]));
        Queue<Integer> endTimes = new PriorityQueue<>();
        for (int[] m : meetings) {
            String action;
            if (!endTimes.isEmpty() && endTimes.peek() <= m[0]) {
                endTimes.poll();
                action = "reuse freed room";
            } else action = "allocate NEW room";
            endTimes.offer(m[1]);
            System.out.printf("meeting [%2d,%2d] -> %-18s rooms in use: %d%n",
                    m[0], m[1], action, endTimes.size());
        }
        System.out.println("Minimum rooms needed: 2 (peak concurrency)");
    }
}
