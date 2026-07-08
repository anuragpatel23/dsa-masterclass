import java.util.HashMap;
import java.util.Map;

/**
 * Runnable illustrations for Linked Lists.
 * Compile & run:  javac LinkedListDemo.java && java LinkedListDemo
 *
 * Example 1: build + reverse a list (iterative, with trace)
 * Example 2: fast/slow pointers — middle and cycle detection (+ cycle start)
 * Example 3: merge two sorted lists with a dummy head
 * Example 4: LRU cache (HashMap + doubly linked list) exercised step by step
 */
public class LinkedListDemo {

    static class ListNode {
        int val;
        ListNode next;
        ListNode(int val) { this.val = val; }
    }

    public static void main(String[] args) {
        example1_reverse();
        example2_fastSlow();
        example3_merge();
        example4_lruCache();
    }

    static ListNode build(int... vals) {
        ListNode dummy = new ListNode(0), tail = dummy;
        for (int v : vals) { tail.next = new ListNode(v); tail = tail.next; }
        return dummy.next;
    }

    static String render(ListNode head) {
        StringBuilder sb = new StringBuilder();
        for (ListNode n = head; n != null; n = n.next)
            sb.append(n.val).append(n.next != null ? " -> " : "");
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // EXAMPLE 1: iterative reversal — the 4-step dance (save, flip,
    // advance prev, advance cur), shown after each iteration.
    // ------------------------------------------------------------------
    static void example1_reverse() {
        System.out.println("=== Example 1: reverse 1->2->3->4->5 (trace) ===");
        ListNode head = build(1, 2, 3, 4, 5);
        ListNode prev = null, cur = head;
        int step = 0;
        while (cur != null) {
            ListNode next = cur.next;   // save
            cur.next = prev;            // flip
            prev = cur;                 // advance prev
            cur = next;                 // advance cur
            System.out.printf("step %d: reversed part = %-22s remaining = %s%n",
                    ++step, render(prev), cur == null ? "(none)" : render(cur));
        }
        System.out.println("result: " + render(prev) + "\n");
    }

    // ------------------------------------------------------------------
    // EXAMPLE 2: fast/slow pointers. Two demos:
    //   (a) find the middle of an odd/even list
    //   (b) detect a cycle and locate its entry node (Floyd phase 2)
    // ------------------------------------------------------------------
    static void example2_fastSlow() {
        System.out.println("=== Example 2: fast/slow pointers ===");
        System.out.println("middle of 1..5 : " + middle(build(1, 2, 3, 4, 5)).val + " (expect 3)");
        System.out.println("middle of 1..6 : " + middle(build(1, 2, 3, 4, 5, 6)).val
                + " (expect 4 — second middle)");

        // Build 1->2->3->4->5 then loop 5 back to 3
        ListNode head = build(1, 2, 3, 4, 5);
        ListNode three = head.next.next, five = three.next.next;
        five.next = three;                                  // create cycle

        ListNode slow = head, fast = head, meet = null;
        while (fast != null && fast.next != null) {
            slow = slow.next; fast = fast.next.next;
            if (slow == fast) { meet = slow; break; }
        }
        System.out.println("cycle detected : " + (meet != null));

        ListNode p = head;                                   // phase 2: find entry
        while (p != meet) { p = p.next; meet = meet.next; }
        System.out.println("cycle starts at node with value " + p.val + " (expect 3)\n");
    }

    static ListNode middle(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) { slow = slow.next; fast = fast.next.next; }
        return slow;
    }

    // ------------------------------------------------------------------
    // EXAMPLE 3: merge two sorted lists — dummy head removes edge cases.
    // ------------------------------------------------------------------
    static void example3_merge() {
        System.out.println("=== Example 3: merge sorted lists ===");
        ListNode a = build(1, 3, 8, 9), b = build(2, 3, 7, 10, 11);
        System.out.println("a: " + render(a));
        System.out.println("b: " + render(b));
        ListNode dummy = new ListNode(0), tail = dummy;
        while (a != null && b != null) {
            if (a.val <= b.val) { tail.next = a; a = a.next; }
            else                { tail.next = b; b = b.next; }
            tail = tail.next;
        }
        tail.next = (a != null) ? a : b;
        System.out.println("merged: " + render(dummy.next) + "\n");
    }

    // ------------------------------------------------------------------
    // EXAMPLE 4: LRU cache, capacity 3. Watch the eviction order.
    // ------------------------------------------------------------------
    static void example4_lruCache() {
        System.out.println("=== Example 4: LRU cache (capacity 3) ===");
        LRUCache cache = new LRUCache(3);
        cache.put(1, 100); System.out.println("put(1)          -> " + cache);
        cache.put(2, 200); System.out.println("put(2)          -> " + cache);
        cache.put(3, 300); System.out.println("put(3)          -> " + cache);
        cache.get(1);      System.out.println("get(1)          -> " + cache + "   (1 is now most recent)");
        cache.put(4, 400); System.out.println("put(4) evicts 2 -> " + cache + "   (2 was least recent)");
        System.out.println("get(2) = " + cache.get(2) + "  (evicted, -1 expected)");
    }

    static class LRUCache {
        class Node { int key, value; Node prev, next; Node(int k, int v) { key = k; value = v; } }

        private final int capacity;
        private final Map<Integer, Node> map = new HashMap<>();
        private final Node head = new Node(0, 0), tail = new Node(0, 0);

        LRUCache(int capacity) {
            this.capacity = capacity;
            head.next = tail; tail.prev = head;
        }

        int get(int key) {
            Node n = map.get(key);
            if (n == null) return -1;
            unlink(n); insertAfterHead(n);
            return n.value;
        }

        void put(int key, int value) {
            Node n = map.get(key);
            if (n != null) { n.value = value; unlink(n); insertAfterHead(n); return; }
            if (map.size() == capacity) {
                Node lru = tail.prev;
                unlink(lru);
                map.remove(lru.key);
            }
            Node fresh = new Node(key, value);
            map.put(key, fresh);
            insertAfterHead(fresh);
        }

        private void unlink(Node n) { n.prev.next = n.next; n.next.prev = n.prev; }
        private void insertAfterHead(Node n) {
            n.next = head.next; n.prev = head;
            head.next.prev = n; head.next = n;
        }

        @Override public String toString() {                 // most→least recent
            StringBuilder sb = new StringBuilder("[MRU ");
            for (Node n = head.next; n != tail; n = n.next) sb.append(n.key).append(' ');
            return sb.append("LRU]").toString();
        }
    }
}
