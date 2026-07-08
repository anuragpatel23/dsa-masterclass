import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Runnable illustrations for Advanced Structures.
 * Compile & run:  javac AdvancedDemo.java && java AdvancedDemo
 *
 * Example 1: Union-Find — friend circles merging, with component count
 * Example 2: Fenwick tree — live leaderboard range sums
 * Example 3: merge intervals + sweep line max-concurrency
 */
public class AdvancedDemo {

    public static void main(String[] args) {
        example1_unionFind();
        example2_fenwick();
        example3_intervals();
    }

    // ------------------------------------------------------------------
    // EXAMPLE 1: 6 people, friendships arrive one by one. Watch circles
    // merge; the redundant edge (already-connected pair) is detected.
    // ------------------------------------------------------------------
    static void example1_unionFind() {
        System.out.println("=== Example 1: Union-Find (friend circles) ===");
        String[] people = {"Ann", "Ben", "Cat", "Dan", "Eli", "Fay"};
        int[][] friendships = {{0,1},{2,3},{4,5},{1,2},{0,3}};   // last one is redundant

        UnionFind uf = new UnionFind(people.length);
        for (int[] f : friendships) {
            boolean merged = uf.union(f[0], f[1]);
            System.out.printf("  %s + %s : %-28s circles now: %d%n",
                    people[f[0]], people[f[1]],
                    merged ? "circles merged" : "ALREADY connected (cycle edge!)",
                    uf.count());
        }
        System.out.println("Ann connected to Dan? " + uf.connected(0, 3));
        System.out.println("Ann connected to Eli? " + uf.connected(0, 4));
        System.out.println("'Already connected' detection = Redundant Connection / Kruskal cycle check.\n");
    }

    static class UnionFind {
        private final int[] parent, rank;
        private int components;

        UnionFind(int n) {
            parent = new int[n];
            rank = new int[n];
            components = n;
            for (int i = 0; i < n; i++) parent[i] = i;
        }

        int find(int x) {
            while (parent[x] != x) {
                parent[x] = parent[parent[x]];               // path compression
                x = parent[x];
            }
            return x;
        }

        boolean union(int a, int b) {
            int ra = find(a), rb = find(b);
            if (ra == rb) return false;
            if (rank[ra] < rank[rb]) { int t = ra; ra = rb; rb = t; }
            parent[rb] = ra;
            if (rank[ra] == rank[rb]) rank[ra]++;
            components--;
            return true;
        }

        boolean connected(int a, int b) { return find(a) == find(b); }
        int count() { return components; }
    }

    // ------------------------------------------------------------------
    // EXAMPLE 2: Fenwick tree — point updates + O(log n) range sums.
    // Leaderboard: 8 players, scores change, query rank ranges live.
    // ------------------------------------------------------------------
    static void example2_fenwick() {
        System.out.println("=== Example 2: Fenwick tree (live leaderboard) ===");
        int[] scores = {10, 20, 15, 30, 5, 25, 40, 8};       // players 1..8
        Fenwick fw = new Fenwick(scores.length);
        for (int i = 0; i < scores.length; i++) fw.update(i + 1, scores[i]);

        System.out.println("scores       : " + Arrays.toString(scores));
        System.out.println("sum players 1-4  : " + fw.rangeSum(1, 4) + "  (10+20+15+30)");
        System.out.println("sum players 5-8  : " + fw.rangeSum(5, 8) + "  (5+25+40+8)");

        System.out.println("player 3 scores +50 ...");
        fw.update(3, 50);                                    // delta
        System.out.println("sum players 1-4  : " + fw.rangeSum(1, 4) + "  (updated in O(log n))");
        System.out.println("Both update and query O(log n) — prefix arrays can't do that.\n");
    }

    static class Fenwick {
        private final long[] tree;                           // 1-indexed

        Fenwick(int n) { tree = new long[n + 1]; }

        void update(int i, long delta) {
            for (; i < tree.length; i += i & (-i)) tree[i] += delta;
        }

        long prefixSum(int i) {
            long s = 0;
            for (; i > 0; i -= i & (-i)) s += tree[i];
            return s;
        }

        long rangeSum(int l, int r) { return prefixSum(r) - prefixSum(l - 1); }
    }

    // ------------------------------------------------------------------
    // EXAMPLE 3: intervals — merge overlapping bookings, then sweep line
    // to find peak simultaneous meetings.
    // ------------------------------------------------------------------
    static void example3_intervals() {
        System.out.println("=== Example 3: merge intervals + sweep line ===");
        int[][] bookings = {{9, 11}, {10, 12}, {13, 14}, {11, 13}, {15, 17}, {16, 18}};
        System.out.println("bookings: " + Arrays.deepToString(bookings));

        // Merge
        Arrays.sort(bookings, Comparator.comparingInt(b -> b[0]));
        List<int[]> merged = new ArrayList<>();
        int[] cur = bookings[0].clone();
        for (int i = 1; i < bookings.length; i++) {
            if (bookings[i][0] <= cur[1]) cur[1] = Math.max(cur[1], bookings[i][1]);
            else { merged.add(cur); cur = bookings[i].clone(); }
        }
        merged.add(cur);
        System.out.print("merged busy blocks: ");
        for (int[] m : merged) System.out.print(Arrays.toString(m) + " ");
        System.out.println();

        // Sweep line: +1 at start, -1 at end
        int[][] events = new int[bookings.length * 2][2];
        for (int i = 0; i < bookings.length; i++) {
            events[2 * i]     = new int[]{bookings[i][0], +1};
            events[2 * i + 1] = new int[]{bookings[i][1], -1};
        }
        Arrays.sort(events, (a, b) -> a[0] != b[0] ? a[0] - b[0] : a[1] - b[1]); // ends first
        int concurrent = 0, peak = 0, peakTime = 0;
        for (int[] e : events) {
            concurrent += e[1];
            if (concurrent > peak) { peak = concurrent; peakTime = e[0]; }
        }
        System.out.println("peak concurrency: " + peak + " meetings at time " + peakTime
                + " -> need " + peak + " rooms");
        System.out.println("Sweep line = sort events, keep running count. Powers Meeting Rooms II & Skyline.");
    }
}
