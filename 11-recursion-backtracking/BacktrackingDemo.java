import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Runnable illustrations for Recursion & Backtracking.
 * Compile & run:  javac BacktrackingDemo.java && java BacktrackingDemo
 *
 * Example 1: subsets — the choose/explore/un-choose skeleton with trace
 * Example 2: permutations with a used[] mask
 * Example 3: N-Queens (n=6) with board rendering and pruning counters
 */
public class BacktrackingDemo {

    public static void main(String[] args) {
        example1_subsets();
        example2_permutations();
        example3_nQueens();
    }

    // ------------------------------------------------------------------
    // EXAMPLE 1: subsets of {1,2,3} with indentation showing the
    // recursion tree — watch choices being made and UNDONE.
    // ------------------------------------------------------------------
    static void example1_subsets() {
        System.out.println("=== Example 1: subsets of [1,2,3] (recursion trace) ===");
        List<List<Integer>> result = new ArrayList<>();
        dfs(new int[]{1, 2, 3}, 0, new ArrayList<>(), result, 0);
        System.out.println("all " + result.size() + " subsets: " + result);
        System.out.println("count = 2^n = 8 — every element is in or out.\n");
    }

    static void dfs(int[] nums, int start, List<Integer> path,
                    List<List<Integer>> result, int depth) {
        System.out.println("  ".repeat(depth) + "visit " + path);
        result.add(new ArrayList<>(path));                   // COPY — path keeps mutating
        for (int i = start; i < nums.length; i++) {
            path.add(nums[i]);                               // choose
            dfs(nums, i + 1, path, result, depth + 1);       // explore
            path.remove(path.size() - 1);                    // un-choose (backtrack)
        }
    }

    // ------------------------------------------------------------------
    // EXAMPLE 2: permutations of [1,2,3] — order matters, so we track
    // which elements are already used.
    // ------------------------------------------------------------------
    static void example2_permutations() {
        System.out.println("=== Example 2: permutations of [1,2,3] ===");
        List<List<Integer>> result = new ArrayList<>();
        permute(new int[]{1, 2, 3}, new boolean[3], new ArrayList<>(), result);
        System.out.println(result.size() + " permutations (3! = 6): " + result + "\n");
    }

    static void permute(int[] nums, boolean[] used, List<Integer> path,
                        List<List<Integer>> result) {
        if (path.size() == nums.length) { result.add(new ArrayList<>(path)); return; }
        for (int i = 0; i < nums.length; i++) {
            if (used[i]) continue;
            used[i] = true;  path.add(nums[i]);
            permute(nums, used, path, result);
            used[i] = false; path.remove(path.size() - 1);
        }
    }

    // ------------------------------------------------------------------
    // EXAMPLE 3: N-Queens for n=6. Diagonals identified by (row-col)
    // and (row+col). Counts how much pruning saves vs brute force.
    // ------------------------------------------------------------------
    static long nodesVisited = 0;

    static void example3_nQueens() {
        System.out.println("=== Example 3: N-Queens (n=6) ===");
        int n = 6;
        List<int[]> solutions = new ArrayList<>();
        place(0, n, new int[n], new HashSet<>(), new HashSet<>(), new HashSet<>(), solutions);

        System.out.println("solutions found: " + solutions.size() + " (expect 4)");
        System.out.println("first solution:");
        int[] sol = solutions.get(0);
        for (int r = 0; r < n; r++) {
            StringBuilder row = new StringBuilder("  ");
            for (int c = 0; c < n; c++) row.append(sol[r] == c ? " Q" : " .");
            System.out.println(row);
        }
        System.out.printf("nodes explored: %,d vs brute force 6^6 = %,d — pruning wins.%n",
                nodesVisited, (long) Math.pow(6, 6));
    }

    static void place(int row, int n, int[] queenCol, Set<Integer> cols,
                      Set<Integer> d1, Set<Integer> d2, List<int[]> solutions) {
        nodesVisited++;
        if (row == n) { solutions.add(queenCol.clone()); return; }
        for (int col = 0; col < n; col++) {
            if (cols.contains(col) || d1.contains(row - col) || d2.contains(row + col))
                continue;                                    // attacked — prune
            cols.add(col); d1.add(row - col); d2.add(row + col);
            queenCol[row] = col;
            place(row + 1, n, queenCol, cols, d1, d2, solutions);
            cols.remove(col); d1.remove(row - col); d2.remove(row + col);  // backtrack
        }
    }
}
