import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Queue;

/**
 * Runnable illustrations for Trees & BSTs.
 * Compile & run:  javac TreesDemo.java && java TreesDemo
 *
 * Example 1: all four traversals of one tree (pre/in/post/level)
 * Example 2: BST insert of sorted vs shuffled data — degeneration in action
 * Example 3: validate BST — why the naive parent-child check is wrong
 * Example 4: lowest common ancestor + serialize/deserialize round trip
 */
public class TreesDemo {

    static class TreeNode {
        int val;
        TreeNode left, right;
        TreeNode(int val) { this.val = val; }
    }

    public static void main(String[] args) {
        example1_traversals();
        example2_degeneration();
        example3_validateBst();
        example4_lcaAndSerialization();
    }

    // Build:        4
    //             /   \
    //            2     6
    //           / \   / \
    //          1   3 5   7
    static TreeNode sampleTree() {
        TreeNode root = new TreeNode(4);
        root.left = new TreeNode(2);
        root.right = new TreeNode(6);
        root.left.left = new TreeNode(1);
        root.left.right = new TreeNode(3);
        root.right.left = new TreeNode(5);
        root.right.right = new TreeNode(7);
        return root;
    }

    // ------------------------------------------------------------------
    // EXAMPLE 1: traversals. Note inorder of a BST comes out SORTED.
    // ------------------------------------------------------------------
    static void example1_traversals() {
        System.out.println("=== Example 1: traversals of a balanced BST rooted at 4 ===");
        TreeNode root = sampleTree();
        List<Integer> pre = new ArrayList<>(), in = new ArrayList<>(), post = new ArrayList<>();
        preorder(root, pre); inorder(root, in); postorder(root, post);
        System.out.println("preorder  (N,L,R): " + pre + "  <- serialization order");
        System.out.println("inorder   (L,N,R): " + in + "  <- SORTED (BST property)");
        System.out.println("postorder (L,R,N): " + post + "  <- children before parent");

        System.out.print("level order      : ");
        Queue<TreeNode> queue = new ArrayDeque<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            int size = queue.size();                        // snapshot = one level
            List<Integer> level = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                TreeNode n = queue.poll();
                level.add(n.val);
                if (n.left != null) queue.offer(n.left);
                if (n.right != null) queue.offer(n.right);
            }
            System.out.print(level + " ");
        }
        System.out.println("\n");
    }

    static void preorder(TreeNode n, List<Integer> out)  { if (n == null) return; out.add(n.val); preorder(n.left, out); preorder(n.right, out); }
    static void inorder(TreeNode n, List<Integer> out)   { if (n == null) return; inorder(n.left, out); out.add(n.val); inorder(n.right, out); }
    static void postorder(TreeNode n, List<Integer> out) { if (n == null) return; postorder(n.left, out); postorder(n.right, out); out.add(n.val); }

    // ------------------------------------------------------------------
    // EXAMPLE 2: insert 1..15 sorted vs shuffled. Sorted input builds a
    // "linked list" of height 15; shuffled stays near log2(15) ≈ 4.
    // ------------------------------------------------------------------
    static void example2_degeneration() {
        System.out.println("=== Example 2: BST height — sorted vs shuffled inserts ===");
        int[] sorted = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
        int[] shuffled = {8,4,12,2,6,10,14,1,3,5,7,9,11,13,15};

        TreeNode a = null, b = null;
        for (int v : sorted)   a = insert(a, v);
        for (int v : shuffled) b = insert(b, v);

        System.out.println("height after sorted inserts   : " + height(a) + "  (degenerate — every op O(n))");
        System.out.println("height after shuffled inserts : " + height(b) + "  (healthy — ops O(log n))");
        System.out.println("This is why TreeMap self-balances and databases use B-trees.\n");
    }

    static TreeNode insert(TreeNode root, int val) {
        if (root == null) return new TreeNode(val);
        if (val < root.val) root.left = insert(root.left, val);
        else                root.right = insert(root.right, val);
        return root;
    }

    static int height(TreeNode n) {
        return n == null ? 0 : 1 + Math.max(height(n.left), height(n.right));
    }

    // ------------------------------------------------------------------
    // EXAMPLE 3: the famous validate-BST trap.
    //        5
    //       / \
    //      3   8
    //         / \
    //        4   9     <- 4 < 5 violates BST, but 4 < 8 passes naive check!
    // ------------------------------------------------------------------
    static void example3_validateBst() {
        System.out.println("=== Example 3: validate BST (naive vs range check) ===");
        TreeNode trap = new TreeNode(5);
        trap.left = new TreeNode(3);
        trap.right = new TreeNode(8);
        trap.right.left = new TreeNode(4);                  // the violation
        trap.right.right = new TreeNode(9);

        System.out.println("naive (parent-child only): " + naiveCheck(trap) + "   <- WRONG, says valid");
        System.out.println("range-based              : " + isValidBST(trap, null, null) + "  <- correct");
        System.out.println("Lesson: every node constrains its ENTIRE subtree, not just children.\n");
    }

    static boolean naiveCheck(TreeNode n) {
        if (n == null) return true;
        if (n.left != null && n.left.val >= n.val) return false;
        if (n.right != null && n.right.val <= n.val) return false;
        return naiveCheck(n.left) && naiveCheck(n.right);
    }

    static boolean isValidBST(TreeNode n, Integer lo, Integer hi) {
        if (n == null) return true;
        if (lo != null && n.val <= lo) return false;
        if (hi != null && n.val >= hi) return false;
        return isValidBST(n.left, lo, n.val) && isValidBST(n.right, n.val, hi);
    }

    // ------------------------------------------------------------------
    // EXAMPLE 4: LCA + serialization round trip.
    // ------------------------------------------------------------------
    static void example4_lcaAndSerialization() {
        System.out.println("=== Example 4: LCA and serialize/deserialize ===");
        TreeNode root = sampleTree();
        TreeNode one = root.left.left, three = root.left.right, five = root.right.left;

        System.out.println("LCA(1, 3) = " + lca(root, one, three).val + "  (their parent 2)");
        System.out.println("LCA(1, 5) = " + lca(root, one, five).val + "  (straddle the root 4)");

        StringBuilder sb = new StringBuilder();
        serialize(root, sb);
        String data = sb.toString();
        System.out.println("serialized (preorder + # for null): " + data);

        Deque<String> tokens = new ArrayDeque<>(Arrays.asList(data.split(",")));
        TreeNode copy = deserialize(tokens);
        List<Integer> original = new ArrayList<>(), restored = new ArrayList<>();
        inorder(root, original); inorder(copy, restored);
        System.out.println("round-trip inorder matches original: " + original.equals(restored));
    }

    static TreeNode lca(TreeNode root, TreeNode p, TreeNode q) {
        if (root == null || root == p || root == q) return root;
        TreeNode left = lca(root.left, p, q), right = lca(root.right, p, q);
        if (left != null && right != null) return root;
        return left != null ? left : right;
    }

    static void serialize(TreeNode n, StringBuilder sb) {
        if (n == null) { sb.append("#,"); return; }
        sb.append(n.val).append(',');
        serialize(n.left, sb);
        serialize(n.right, sb);
    }

    static TreeNode deserialize(Deque<String> tokens) {
        String t = tokens.poll();
        if (t.equals("#")) return null;
        TreeNode n = new TreeNode(Integer.parseInt(t));
        n.left = deserialize(tokens);
        n.right = deserialize(tokens);
        return n;
    }
}
