# 09 — Graphs

## Why It Matters

Graphs are the highest-signal interview topic: they separate levels. Senior loops
almost always include one graph question — BFS/DFS on grids, topological sort, or
shortest path. Real systems ARE graphs: social networks, road maps, service
dependency graphs, build systems, recommendation engines.

---

## 1. Modeling & Representation

A graph = vertices + edges. Directed or undirected; weighted or unweighted;
cyclic or acyclic (DAG).

### Real-life analogies
- **Undirected**: Facebook friendships (mutual).
- **Directed**: Twitter follows, web links, package dependencies.
- **Weighted**: road map with distances; network links with latency.
- **DAG**: course prerequisites, build pipelines (Maven/Gradle), spreadsheet cell deps.

### Representations

| Representation | Space | Edge lookup | Iterate neighbors | Use when |
|----------------|-------|-------------|-------------------|----------|
| Adjacency list | O(V+E) | O(degree) | O(degree) | default — most graphs are sparse |
| Adjacency matrix | O(V²) | O(1) | O(V) | dense graphs, small V |
| Edge list | O(E) | O(E) | — | Kruskal, union-find problems |

```java
// Adjacency list from an edge list
Map<Integer, List<Integer>> buildGraph(int n, int[][] edges) {
    Map<Integer, List<Integer>> graph = new HashMap<>();
    for (int i = 0; i < n; i++) graph.put(i, new ArrayList<>());
    for (int[] e : edges) {
        graph.get(e[0]).add(e[1]);
        graph.get(e[1]).add(e[0]);   // omit for directed graphs
    }
    return graph;
}
```

**Interview tip:** grids ARE graphs (cell = vertex, 4-neighbors = edges). Many
"matrix" questions are secretly BFS/DFS.

---

## 2. BFS — shortest path in unweighted graphs

Explores level by level with a queue. First time you reach a node = shortest
edge-count path. **Real-life:** LinkedIn "2nd-degree connection" is literally BFS
depth 2; also word-ladder puzzles, minimum moves in games.

```java
// BFS shortest distance from start to target — O(V + E)
int bfs(Map<Integer, List<Integer>> graph, int start, int target) {
    Queue<Integer> queue = new ArrayDeque<>();
    Set<Integer> visited = new HashSet<>();
    queue.offer(start);
    visited.add(start);                          // mark WHEN ENQUEUING, not when polling
    int dist = 0;
    while (!queue.isEmpty()) {
        int size = queue.size();
        for (int i = 0; i < size; i++) {
            int node = queue.poll();
            if (node == target) return dist;
            for (int nb : graph.get(node))
                if (visited.add(nb)) queue.offer(nb);
        }
        dist++;
    }
    return -1;
}
```

### Multi-source BFS (great senior pattern)
Start BFS from **all** sources at once — e.g., Rotting Oranges (all rotten cells
day 0), 01-Matrix (distance to nearest zero), Walls and Gates. Just seed the queue
with every source.

---

## 3. DFS — reachability, components, cycles

Goes deep first (recursion or explicit stack). **Real-life:** exploring a maze
with a ball of string; crawling all pages reachable from a homepage.

```java
// Number of Islands — DFS flood fill on a grid, O(rows × cols)
public int numIslands(char[][] grid) {
    int count = 0;
    for (int r = 0; r < grid.length; r++)
        for (int c = 0; c < grid[0].length; c++)
            if (grid[r][c] == '1') { count++; sink(grid, r, c); }
    return count;
}
private void sink(char[][] g, int r, int c) {
    if (r < 0 || r >= g.length || c < 0 || c >= g[0].length || g[r][c] != '1') return;
    g[r][c] = '0';                               // mark visited in-place
    sink(g, r + 1, c); sink(g, r - 1, c);
    sink(g, r, c + 1); sink(g, r, c - 1);
}
```

### Cycle detection
- **Undirected:** DFS; a visited neighbor that isn't the parent → cycle. (Or Union-Find.)
- **Directed:** 3 colors — white (unvisited), gray (in current path), black (done).
  Reaching a **gray** node → back edge → cycle. This is deadlock detection in
  databases and "circular dependency" errors in build tools.

```java
// Directed cycle detection (course schedule feasibility)
boolean hasCycle(Map<Integer, List<Integer>> g, int n) {
    int[] color = new int[n];                    // 0 white, 1 gray, 2 black
    for (int i = 0; i < n; i++)
        if (color[i] == 0 && dfsCycle(g, i, color)) return true;
    return false;
}
private boolean dfsCycle(Map<Integer, List<Integer>> g, int u, int[] color) {
    color[u] = 1;                                // gray: on current path
    for (int v : g.getOrDefault(u, List.of())) {
        if (color[v] == 1) return true;          // back edge
        if (color[v] == 0 && dfsCycle(g, v, color)) return true;
    }
    color[u] = 2;                                // black: fully processed
    return false;
}
```

---

## 4. Topological Sort — ordering a DAG

**Real-life:** deciding course order given prerequisites; Maven resolving build
order; Airflow scheduling DAG tasks; `npm install` dependency ordering.

### Kahn's algorithm (BFS, in-degrees) — preferred in interviews
```java
// Course Schedule II — return a valid order or empty if cyclic. O(V + E)
public int[] findOrder(int numCourses, int[][] prerequisites) {
    List<List<Integer>> graph = new ArrayList<>();
    int[] indegree = new int[numCourses];
    for (int i = 0; i < numCourses; i++) graph.add(new ArrayList<>());
    for (int[] p : prerequisites) {              // p[1] → p[0]
        graph.get(p[1]).add(p[0]);
        indegree[p[0]]++;
    }

    Queue<Integer> queue = new ArrayDeque<>();
    for (int i = 0; i < numCourses; i++)
        if (indegree[i] == 0) queue.offer(i);    // no prerequisites

    int[] order = new int[numCourses];
    int idx = 0;
    while (!queue.isEmpty()) {
        int course = queue.poll();
        order[idx++] = course;
        for (int next : graph.get(course))
            if (--indegree[next] == 0) queue.offer(next);
    }
    return idx == numCourses ? order : new int[0];   // idx < n ⇒ cycle
}
```

Alternative: DFS postorder reversed. Kahn also *detects cycles* for free and can
compute "parallel levels" (which tasks can run concurrently) — a great systems tie-in.

---

## 5. Shortest Paths — weighted graphs

### Dijkstra (non-negative weights) — BFS + priority queue

**Real-life:** Google Maps fastest route; network routing (OSPF is
Dijkstra-based); cheapest-flight style problems.

```java
// Dijkstra — O((V + E) log V) with a binary heap
int[] dijkstra(List<List<int[]>> graph, int src) {   // graph.get(u) = list of [v, weight]
    int n = graph.size();
    int[] dist = new int[n];
    Arrays.fill(dist, Integer.MAX_VALUE);
    dist[src] = 0;

    Queue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1])); // [node, dist]
    pq.offer(new int[]{src, 0});

    while (!pq.isEmpty()) {
        int[] cur = pq.poll();
        int u = cur[0], d = cur[1];
        if (d > dist[u]) continue;               // stale entry — lazy deletion
        for (int[] edge : graph.get(u)) {
            int v = edge[0], w = edge[1];
            if (dist[u] + w < dist[v]) {
                dist[v] = dist[u] + w;
                pq.offer(new int[]{v, dist[v]});
            }
        }
    }
    return dist;
}
```

### The decision table (memorize)

| Scenario | Algorithm | Complexity |
|----------|-----------|------------|
| Unweighted | BFS | O(V+E) |
| Non-negative weights | Dijkstra | O((V+E) log V) |
| Negative weights allowed | Bellman-Ford | O(V·E) |
| All-pairs, small V | Floyd-Warshall | O(V³) |
| ≤ K stops constraint | Bellman-Ford variant / BFS on (node, stops) | — |
| DAG only | topo order + relax | O(V+E) |

Why Dijkstra fails with negative edges: it finalizes a node greedily, but a
negative edge later could undercut the "final" distance. Bellman-Ford relaxes all
edges V-1 times, and a V-th improvement reveals a **negative cycle** — the classic
currency-arbitrage detection question.

### Minimum Spanning Tree (know both)
- **Kruskal**: sort edges, add if it doesn't form a cycle (Union-Find, folder 16).
- **Prim**: grow the tree with a heap of frontier edges (Dijkstra-like).
- **Real-life:** cheapest cable/fiber layout connecting all offices; cluster analysis.

---

## 6. Choosing BFS vs DFS (say this out loud)

| Need | Choose | Why |
|------|--------|-----|
| Shortest path (unweighted) | BFS | levels = distance |
| Any path / connectivity / components | DFS | simpler, less memory on wide graphs |
| Ordering with dependencies | Topo sort | DAG structure |
| All configurations / backtracking | DFS | natural recursion |
| Deep narrow graph | BFS risk: fine; DFS risk: stack overflow | mention iterative DFS |
| Wide bushy graph | DFS | BFS queue explodes to O(V) |

---

## 7. Interview Pitfalls

- Marking visited on POLL instead of OFFER in BFS → duplicates in queue → TLE/wrong.
- Forgetting disconnected components — loop over all vertices as potential starts.
- Undirected cycle detection without excluding the parent edge.
- Grid DFS without bounds checks first (order the guard clauses).
- Dijkstra: forgetting the stale-entry check (`d > dist[u]`).
- Not stating V and E in complexity: "O(V + E)" — never just "O(n)".
- Clone Graph: HashMap old→new BEFORE recursing on neighbors (handles cycles).

## Must-Solve List
1. Number of Islands / Max Area of Island / Surrounded Regions
2. Clone Graph
3. Rotting Oranges / 01-Matrix / Walls and Gates (multi-source BFS)
4. Course Schedule I & II (cycle detection + topo sort)
5. Pacific Atlantic Water Flow (reverse DFS from borders)
6. Word Ladder (BFS on implicit graph)
7. Network Delay Time (Dijkstra)
8. Cheapest Flights Within K Stops (Bellman-Ford flavor)
9. Graph Valid Tree / Number of Connected Components (Union-Find, folder 16)
10. Alien Dictionary (topo sort — hard; a senior favorite)
11. Min Cost to Connect All Points (MST)

---

## Deep Dive & Worked Examples

> Runnable examples: `GraphsDemo.java` in this folder — BFS degrees of separation, island counting, semester-by-semester topological sort, and a Dijkstra relaxation trace.

### Dry run: Kahn's algorithm on course prerequisites

Courses: intro(0), datastruct(1, needs 0), algorithms(2, needs 1), databases(3,
needs 1), distributed(4, needs 2+3).

```
indegrees: [0, 1, 1, 1, 2]
queue = [intro]                          (indegree 0)
poll intro      → datastruct indegree 1→0 → enqueue
poll datastruct → algorithms 1→0, databases 1→0 → enqueue both
poll algorithms → distributed 2→1        (not ready yet!)
poll databases  → distributed 1→0 → enqueue
poll distributed → done. order: intro, datastruct, algorithms, databases, distributed
```

Two bonus insights the demo prints: nodes that enter the queue **in the same batch
can run in parallel** (semester 3 = algorithms + databases together) — this is how
build systems parallelize; and if the poll count < n, the leftover nodes form a
cycle — you get cycle detection for free.

### Why "visited on enqueue" matters in BFS (the classic bug)

If you mark visited when POLLING, a node can be enqueued many times before its
first poll (once per incoming edge). On dense graphs that's O(E) queue entries and
duplicated work — on a grid problem it's the difference between passing and TLE.
Mark the moment you `offer`. In the Dijkstra variant the analogue is the
`if (d > dist[u]) continue;` stale-entry check.

### Dijkstra intuition: why greedy finalization is safe (and when it isn't)

The heap always pops the closest unfinalized node u. Could a shorter path to u still
exist? It would have to leave the finalized set through some other node v with
dist[v] ≥ dist[u], then travel ≥ 0 further (non-negative weights!) — so it can't be
shorter. Negative edges break exactly this "≥ 0 further" step → Bellman-Ford.
Deliver this argument in 3 sentences and the interviewer stops probing.

### The implicit-graph mindset (unlocks many hard problems)

Many problems never say "graph". The state space IS the graph:

| Problem | Vertex | Edge |
|---------|--------|------|
| Word Ladder | word | one-letter change that's in the dictionary |
| Open the Lock | 4-digit combo | one wheel turn |
| Water Jugs | (amountA, amountB) | pour/fill/empty |
| Sliding Puzzle | board layout | one tile slide |
| Cheapest flights ≤ K stops | (city, stopsUsed) | flight |

"Minimum number of moves" on any of these = BFS on the implicit graph. The senior
skill is *naming the state* — including extra dimensions like `stopsUsed` when the
constraint demands it.

### Interviewer follow-ups you should expect

- *"Bidirectional BFS?"* Search from both ends, expand the smaller frontier, stop on
  intersection: O(b^(d/2)) vs O(b^d). Word Ladder's intended optimization.
- *"Detect a cycle in an undirected graph with Union-Find?"* For each edge, if both
  endpoints already share a root → cycle. See folder 16.
- *"Topological sort: DFS vs Kahn?"* DFS: postorder reversed, natural if you're
  already DFS-ing, recursion depth risk. Kahn: iterative, detects cycles by count,
  exposes parallel batches. Prefer Kahn in interviews — easier to get right live.
- *"Why O(V+E) and not O(V·E)?"* Each vertex enqueued once, each edge relaxed once
  (adjacency list). With an adjacency matrix it becomes O(V²) — representation
  changes complexity; say which one you assumed.
- *"Print the actual shortest path, not just the distance"* — keep a `parent[]`
  array, walk it back from the target. Trivial but frequently requested; practice it.

**Next:** `10-sorting-searching`
