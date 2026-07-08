import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Runnable illustrations for Graphs.
 * Compile & run:  javac GraphsDemo.java && java GraphsDemo
 *
 * Example 1: BFS levels = degrees of separation (social network)
 * Example 2: Number of Islands — DFS flood fill on a grid
 * Example 3: Course Schedule — Kahn's topological sort with trace
 * Example 4: Dijkstra shortest paths with relaxation trace
 */
public class GraphsDemo {

    public static void main(String[] args) {
        example1_bfsLevels();
        example2_islands();
        example3_topoSort();
        example4_dijkstra();
    }

    // ------------------------------------------------------------------
    // EXAMPLE 1: friendships as an undirected graph; BFS from Alice
    // prints who is 1 hop away, 2 hops away, ... (LinkedIn degrees).
    // ------------------------------------------------------------------
    static void example1_bfsLevels() {
        System.out.println("=== Example 1: BFS = degrees of separation ===");
        String[] people = {"Alice", "Bob", "Carol", "Dave", "Eve", "Frank"};
        int[][] friendships = {{0,1},{0,2},{1,3},{2,3},{3,4},{4,5}};

        List<List<Integer>> graph = new ArrayList<>();
        for (int i = 0; i < people.length; i++) graph.add(new ArrayList<>());
        for (int[] e : friendships) { graph.get(e[0]).add(e[1]); graph.get(e[1]).add(e[0]); }

        boolean[] visited = new boolean[people.length];
        Queue<Integer> queue = new ArrayDeque<>();
        queue.offer(0);
        visited[0] = true;                                   // mark when ENQUEUING
        int degree = 0;
        while (!queue.isEmpty()) {
            int size = queue.size();
            StringBuilder level = new StringBuilder();
            for (int i = 0; i < size; i++) {
                int person = queue.poll();
                level.append(people[person]).append(' ');
                for (int friend : graph.get(person))
                    if (!visited[friend]) { visited[friend] = true; queue.offer(friend); }
            }
            System.out.println("degree " + degree++ + ": " + level);
        }
        System.out.println();
    }

    // ------------------------------------------------------------------
    // EXAMPLE 2: islands — every '1' region sunk by DFS counts once.
    // ------------------------------------------------------------------
    static void example2_islands() {
        System.out.println("=== Example 2: Number of Islands ===");
        char[][] grid = {
            {'1','1','0','0','1'},
            {'1','0','0','1','1'},
            {'0','0','1','0','0'},
            {'1','0','0','0','1'},
        };
        for (char[] row : grid) System.out.println("  " + new String(row));
        int count = 0;
        for (int r = 0; r < grid.length; r++)
            for (int c = 0; c < grid[0].length; c++)
                if (grid[r][c] == '1') { count++; sink(grid, r, c); }
        System.out.println("islands: " + count + " (expect 5)\n");
    }

    static void sink(char[][] g, int r, int c) {
        if (r < 0 || r >= g.length || c < 0 || c >= g[0].length || g[r][c] != '1') return;
        g[r][c] = '0';
        sink(g, r + 1, c); sink(g, r - 1, c); sink(g, r, c + 1); sink(g, r, c - 1);
    }

    // ------------------------------------------------------------------
    // EXAMPLE 3: Kahn's algorithm on course prerequisites.
    // 0:intro  1:datastructures(needs 0)  2:algorithms(needs 1)
    // 3:databases(needs 1)  4:distributed(needs 2 and 3)
    // ------------------------------------------------------------------
    static void example3_topoSort() {
        System.out.println("=== Example 3: topological sort (course schedule) ===");
        String[] courses = {"intro", "datastruct", "algorithms", "databases", "distributed"};
        int n = courses.length;
        int[][] prereqs = {{1,0},{2,1},{3,1},{4,2},{4,3}};   // {course, requires}

        List<List<Integer>> graph = new ArrayList<>();
        int[] indegree = new int[n];
        for (int i = 0; i < n; i++) graph.add(new ArrayList<>());
        for (int[] p : prereqs) { graph.get(p[1]).add(p[0]); indegree[p[0]]++; }

        Queue<Integer> queue = new ArrayDeque<>();
        for (int i = 0; i < n; i++) if (indegree[i] == 0) queue.offer(i);

        int taken = 0;
        while (!queue.isEmpty()) {
            int size = queue.size();                         // batch = parallelizable!
            StringBuilder semester = new StringBuilder();
            for (int i = 0; i < size; i++) {
                int c = queue.poll();
                taken++;
                semester.append(courses[c]).append(' ');
                for (int next : graph.get(c))
                    if (--indegree[next] == 0) queue.offer(next);
            }
            System.out.println("semester: " + semester);
        }
        System.out.println(taken == n ? "all courses schedulable (no cycle)\n"
                                      : "CYCLE detected — impossible!\n");
    }

    // ------------------------------------------------------------------
    // EXAMPLE 4: Dijkstra on a small weighted city map.
    // ------------------------------------------------------------------
    static void example4_dijkstra() {
        System.out.println("=== Example 4: Dijkstra (cheapest routes from A) ===");
        String[] cities = {"A", "B", "C", "D", "E"};
        // edges: from, to, weight (bidirectional roads)
        int[][] roads = {{0,1,4},{0,2,1},{2,1,2},{1,3,5},{2,3,8},{3,4,3}};

        List<List<int[]>> graph = new ArrayList<>();
        for (int i = 0; i < cities.length; i++) graph.add(new ArrayList<>());
        for (int[] r : roads) {
            graph.get(r[0]).add(new int[]{r[1], r[2]});
            graph.get(r[1]).add(new int[]{r[0], r[2]});
        }

        int[] dist = new int[cities.length];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[0] = 0;
        Queue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        pq.offer(new int[]{0, 0});

        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int u = cur[0], d = cur[1];
            if (d > dist[u]) continue;                       // stale entry — skip
            for (int[] edge : graph.get(u)) {
                int v = edge[0], w = edge[1];
                if (dist[u] + w < dist[v]) {
                    System.out.printf("  relax %s->%s: %s%n", cities[u], cities[v],
                            (dist[v] == Integer.MAX_VALUE ? "inf" : dist[v]) + " -> " + (dist[u] + w));
                    dist[v] = dist[u] + w;
                    pq.offer(new int[]{v, dist[v]});
                }
            }
        }
        System.out.println("final distances from A:");
        for (int i = 0; i < cities.length; i++)
            System.out.println("  A -> " + cities[i] + " = " + dist[i]);
        System.out.println("Note A->B goes via C (1+2=3), beating the direct road (4).");
    }
}
