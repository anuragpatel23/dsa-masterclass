# 06 — Trees & Binary Search Trees

## Why It Matters

Trees are the heart of the interview loop: they test recursion, and recursion tests
whether you can decompose problems. They're also everywhere in real systems —
file systems, DOM, database indexes (B-trees), JSON, org charts, decision trees.


---


## 1. Vocabulary & Real-Life Model

**Real-life analogy:** a company org chart. CEO = root, managers = internal nodes,
individual contributors = leaves. "Height" = layers of management. The DOM in every
web page and your filesystem (`/home/user/docs`) are literal trees.

- **Binary tree** — ≤ 2 children per node.
- **Complete** — all levels full except possibly last, filled left→right (heaps).
- **Balanced** — height O(log n) (AVL, red-black).
- **BST** — left subtree < node < right subtree, recursively.

```java
class TreeNode {
    int val;
    TreeNode left, right;
    TreeNode(int val) { this.val = val; }
}
```

---

## 2. Traversals — the Foundation

### DFS (depth-first): preorder, inorder, postorder

```java
// Inorder (Left, Node, Right) — visits a BST in SORTED order (key fact!)
void inorder(TreeNode node, List<Integer> out) {
    if (node == null) return;
    inorder(node.left, out);
    out.add(node.val);
    inorder(node.right, out);
}
```

| Traversal | Order | Use case |
|-----------|-------|----------|
| Preorder | Node, L, R | copy/serialize a tree, prefix expressions |
| Inorder | L, Node, R | sorted output from BST, validate BST |
| Postorder | L, R, Node | delete/free tree, compute from children up (subtree sums) |

### Iterative inorder (interviewers ask to remove recursion)
```java
List<Integer> inorderIterative(TreeNode root) {
    List<Integer> out = new ArrayList<>();
    Deque<TreeNode> stack = new ArrayDeque<>();
    TreeNode cur = root;
    while (cur != null || !stack.isEmpty()) {
        while (cur != null) { stack.push(cur); cur = cur.left; }  // go far left
        cur = stack.pop();
        out.add(cur.val);
        cur = cur.right;
    }
    return out;
}
```

### BFS (level order) — queue

**Real-life:** notifying an org level by level: CEO first, then all VPs, then all
directors...

```java
List<List<Integer>> levelOrder(TreeNode root) {
    List<List<Integer>> levels = new ArrayList<>();
    if (root == null) return levels;
    Queue<TreeNode> queue = new ArrayDeque<>();
    queue.offer(root);
    while (!queue.isEmpty()) {
        int size = queue.size();                 // freeze current level size
        List<Integer> level = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            TreeNode n = queue.poll();
            level.add(n.val);
            if (n.left != null) queue.offer(n.left);
            if (n.right != null) queue.offer(n.right);
        }
        levels.add(level);
    }
    return levels;
}
```

The `int size = queue.size()` snapshot is the key trick — it separates levels.
Variants: zigzag order, right-side view (last element per level), level averages.

---

## 3. The Recursive Template (internalize this)

Almost every tree problem is: *"ask left, ask right, combine at this node."*

```java
// Max depth
int maxDepth(TreeNode n) {
    if (n == null) return 0;
    return 1 + Math.max(maxDepth(n.left), maxDepth(n.right));
}

// Diameter — longest path between any two nodes; global max of left+right depths
int diameter = 0;
int diameterOfBinaryTree(TreeNode root) { depth(root); return diameter; }
private int depth(TreeNode n) {
    if (n == null) return 0;
    int l = depth(n.left), r = depth(n.right);
    diameter = Math.max(diameter, l + r);        // path THROUGH this node
    return 1 + Math.max(l, r);                   // depth FOR the parent
}
```

The diameter pattern — *return one thing to the parent, update a global with
another* — also solves Max Path Sum (hard), Longest Univalue Path, etc.

```java
// Lowest Common Ancestor (binary tree) — the classic
TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
    if (root == null || root == p || root == q) return root;
    TreeNode left  = lowestCommonAncestor(root.left, p, q);
    TreeNode right = lowestCommonAncestor(root.right, p, q);
    if (left != null && right != null) return root;   // p and q straddle this node
    return (left != null) ? left : right;
}
```

**Real-life LCA:** the lowest common manager of two employees — who should mediate
their conflict; also: nearest common directory of two file paths.

---

## 4. Binary Search Trees

Invariant: **entire** left subtree < node < **entire** right subtree.

### Real-life analogy
A dictionary/phone book you navigate by "before or after this page?" Each look
halves the search space — if the tree is balanced.

```java
// Search — O(h): O(log n) balanced, O(n) degenerate
TreeNode search(TreeNode root, int target) {
    while (root != null && root.val != target)
        root = target < root.val ? root.left : root.right;
    return root;
}

// Insert
TreeNode insert(TreeNode root, int val) {
    if (root == null) return new TreeNode(val);
    if (val < root.val) root.left = insert(root.left, val);
    else                root.right = insert(root.right, val);
    return root;
}

// Delete — the tricky one: three cases
TreeNode delete(TreeNode root, int key) {
    if (root == null) return null;
    if (key < root.val)      root.left = delete(root.left, key);
    else if (key > root.val) root.right = delete(root.right, key);
    else {
        if (root.left == null)  return root.right;   // 0 or 1 child
        if (root.right == null) return root.left;
        TreeNode succ = root.right;                  // 2 children:
        while (succ.left != null) succ = succ.left;  // inorder successor
        root.val = succ.val;
        root.right = delete(root.right, succ.val);   // remove successor
    }
    return root;
}
```

### Validate BST — the #1 trap question
Checking only `left.val < node.val < right.val` is WRONG (a grandchild can violate
the range). Pass down min/max bounds:

```java
boolean isValidBST(TreeNode root) { return valid(root, null, null); }
private boolean valid(TreeNode n, Integer lo, Integer hi) {
    if (n == null) return true;
    if (lo != null && n.val <= lo) return false;
    if (hi != null && n.val >= hi) return false;
    return valid(n.left, lo, n.val) && valid(n.right, n.val, hi);
}
```

(Alternative: inorder traversal must be strictly increasing.)

### Kth smallest in BST — inorder, stop at k. Follow-up: with frequent inserts,
augment nodes with subtree size for O(log n) queries.

---

## 5. Balanced Trees & Real Systems (senior depth)

A BST fed sorted input degenerates into a linked list → O(n) ops. Fixes:

- **AVL tree** — strictly balanced (heights differ ≤ 1); faster reads, more rotation
  work on writes.
- **Red-black tree** — looser balance, fewer rotations; backs Java's `TreeMap`,
  `TreeSet`, Linux's CFS scheduler, and HashMap's treeified buckets.
- **B-tree / B+ tree** — high branching factor (hundreds of keys/node) to minimize
  disk reads; **the** database index structure (MySQL InnoDB, Postgres). B+ trees
  keep values in linked leaves → fast range scans (`WHERE id BETWEEN...`).
- **LSM trees** (log-structured merge) — write-optimized alternative used by
  Cassandra, RocksDB, LevelDB: batch writes in memory, flush sorted runs, compact.

You rarely implement rotations in interviews, but explaining **why** `TreeMap` is
O(log n) and why databases prefer B+ trees over binary trees (disk pages, fan-out,
fewer seeks) is prime senior material.

---

## 6. Serialization (design-flavored classic)

```java
// Serialize/deserialize via preorder with null markers
String serialize(TreeNode root) {
    StringBuilder sb = new StringBuilder();
    build(root, sb);
    return sb.toString();
}
private void build(TreeNode n, StringBuilder sb) {
    if (n == null) { sb.append("#,"); return; }
    sb.append(n.val).append(',');
    build(n.left, sb);
    build(n.right, sb);
}

TreeNode deserialize(String data) {
    Deque<String> tokens = new ArrayDeque<>(Arrays.asList(data.split(",")));
    return parse(tokens);
}
private TreeNode parse(Deque<String> tokens) {
    String t = tokens.poll();
    if (t.equals("#")) return null;
    TreeNode n = new TreeNode(Integer.parseInt(t));
    n.left = parse(tokens);
    n.right = parse(tokens);
    return n;
}
```

**Real-life:** exactly what JSON/protobuf marshalling of nested objects does; also
how DB snapshots and cache warm-ups persist tree state.

---

## 7. Complexity Summary

| Operation | Balanced BST | Degenerate BST | Notes |
|-----------|--------------|----------------|-------|
| search/insert/delete | O(log n) | O(n) | height is everything |
| inorder traversal | O(n) | O(n) | sorted output |
| BFS/DFS | O(n) time | | space: O(w) queue vs O(h) stack |

BFS worst-case queue width = n/2 (last level); DFS stack = height.
Deep skewed tree → DFS recursion may overflow; wide tree → BFS memory heavy.
Choosing between them **is** the senior answer.

## 8. Interview Pitfalls

- Validating BST with only parent-child comparison (see §4).
- Forgetting BSTs can have duplicate-value policies — ask.
- Not snapshotting `queue.size()` in level-order.
- Confusing depth (root=0 or 1? clarify) with height.
- Integer bounds in validate-BST: use `Integer` nulls or `long` bounds, since node
  values may equal `Integer.MIN/MAX_VALUE`.
- Recursion on 10⁵-deep skewed trees → StackOverflowError; offer iterative version.

## Must-Solve List
1. Max Depth / Invert Binary Tree / Same Tree / Symmetric Tree (fluency)
2. Level Order + Zigzag + Right Side View
3. Diameter of Binary Tree → Binary Tree Maximum Path Sum (hard)
4. Lowest Common Ancestor (BT and BST versions)
5. Validate BST
6. Kth Smallest in BST
7. Construct Tree from Preorder + Inorder
8. Serialize & Deserialize Binary Tree
9. Delete Node in BST
10. Count Good Nodes / Path Sum II & III

---

## Deep Dive & Worked Examples

> Runnable examples: `TreesDemo.java` in this folder — includes a live demonstration of BST degeneration (sorted inserts → height 15 vs shuffled → height 4) and the validate-BST trap tree.

### Dry run: the validate-BST trap

```
        5
       / \
      3   8
         / \
        4   9      ← 4 is in 5's RIGHT subtree but 4 < 5: INVALID
```

Naive parent-child check: 3<5 ✓, 8>5 ✓, 4<8 ✓, 9>8 ✓ → wrongly says valid.
Range check: node 4 arrives with bounds (5, 8) — "you're in the right subtree of 5
(so > 5) and left subtree of 8 (so < 8)". 4 ≤ 5 → invalid. The bounds narrow as you
descend; each node constrains its entire subtree, not just its children. Draw this
exact tree in interviews when explaining — it's the standard counterexample.

### Dry run: LCA(1, 5) on the sample tree

```
        4
      /   \
     2     6
    / \   / \
   1   3 5   7
```

`lca(4)` → recurse both sides. Left: `lca(2)` → `lca(1)` returns 1 (found p); right
`lca(3)` returns null → left subtree bubbles up 1. Right: `lca(6)` → `lca(5)` returns
5 (found q) → bubbles up 5. Back at 4: left ≠ null AND right ≠ null → **4 is the LCA**
(p and q straddle it). The elegance: "found one of them" and "found the LCA" use the
same return value — the first non-null convergence point IS the answer.

For the **BST version**, exploit ordering instead: walk down from the root; the first
node whose value is between p and q is the LCA — O(h), no recursion needed.

### Choosing traversal by what the problem needs

| Problem says | Traversal | Why |
|--------------|-----------|-----|
| "kth smallest in BST" | inorder | sorted order |
| "serialize / copy" | preorder | parent before children = rebuildable |
| "delete tree / subtree sums / height" | postorder | need children's answers first |
| "minimum depth / nearest X / by levels" | BFS | first hit = closest |
| "path from root with running state" | preorder DFS + backtrack | pass state down |

"Minimum depth" is a sneaky one: DFS must explore everything, BFS stops at the first
leaf — mention the early exit.

### Morris traversal (the O(1)-space flex)

Inorder without stack or recursion: thread each node's inorder predecessor's right
pointer back to the node, traverse, unthread. O(n) time, O(1) space, temporarily
mutates the tree. You'll rarely code it live, but knowing it exists (and its trade-off:
not safe under concurrent readers) is a nice answer to "can you do inorder in O(1) space?"

### Interviewer follow-ups you should expect

- *"Your recursion overflows on a skewed tree of 10⁶ nodes — fix it."* Iterative
  with explicit `ArrayDeque`, or Morris for inorder.
- *"Construct tree from preorder + inorder — why do you need both?"* Preorder gives
  the root; inorder splits left/right subtrees around it. Precompute a value→index
  HashMap for the inorder array to get O(n) instead of O(n²). (Preorder + postorder
  alone is ambiguous for single-child nodes — good trivia.)
- *"Why do databases use B+ trees instead of red-black trees?"* Disk reads happen in
  pages (~4–16KB); a B+ tree node fills a page with hundreds of keys → height 3–4 for
  billions of rows → 3–4 disk reads per lookup. A binary tree of the same data is ~30
  levels → 30 reads. Plus B+ leaves are linked for range scans. This answer alone can
  carry a senior interview segment.
- *"Kth smallest with frequent inserts/deletes?"* Augment each node with subtree
  size; kth-smallest becomes an O(log n) walk ("order statistics tree").

**Next:** `07-heaps-priority-queues`
