import java.util.ArrayList;
import java.util.List;

/**
 * Runnable illustrations for Tries.
 * Compile & run:  javac TrieDemo.java && java TrieDemo
 *
 * Example 1: insert/search/startsWith — the standard trie
 * Example 2: autocomplete — list all words under a prefix
 * Example 3: wildcard search ('.' matches any char)
 */
public class TrieDemo {

    static class Node {
        Node[] children = new Node[26];
        boolean isWord;
    }

    static final Node root = new Node();

    public static void main(String[] args) {
        example1_basics();
        example2_autocomplete();
        example3_wildcard();
    }

    static void insert(String word) {
        Node cur = root;
        for (char c : word.toCharArray()) {
            int i = c - 'a';
            if (cur.children[i] == null) cur.children[i] = new Node();
            cur = cur.children[i];
        }
        cur.isWord = true;
    }

    static Node find(String s) {
        Node cur = root;
        for (char c : s.toCharArray()) {
            cur = cur.children[c - 'a'];
            if (cur == null) return null;
        }
        return cur;
    }

    // ------------------------------------------------------------------
    // EXAMPLE 1: the "app" vs "apple" distinction — prefix vs word.
    // ------------------------------------------------------------------
    static void example1_basics() {
        System.out.println("=== Example 1: trie basics ===");
        for (String w : new String[]{"apple", "app", "application", "apt", "banana", "band"})
            insert(w);

        System.out.println("search(\"app\")        = " + isWord("app") + "   (inserted as a word)");
        System.out.println("search(\"appl\")       = " + isWord("appl") + "  (prefix only, isWord=false)");
        System.out.println("startsWith(\"appl\")   = " + (find("appl") != null));
        System.out.println("search(\"apply\")      = " + isWord("apply"));
        System.out.println("Cost of each op = O(word length), regardless of dictionary size.\n");
    }

    static boolean isWord(String w) {
        Node n = find(w);
        return n != null && n.isWord;
    }

    // ------------------------------------------------------------------
    // EXAMPLE 2: autocomplete — walk to the prefix node, DFS below it.
    // DFS in child order gives LEXICOGRAPHIC results for free.
    // ------------------------------------------------------------------
    static void example2_autocomplete() {
        System.out.println("=== Example 2: autocomplete ===");
        for (String prefix : new String[]{"app", "ban", "z"}) {
            List<String> results = new ArrayList<>();
            Node start = find(prefix);
            if (start != null) collect(start, new StringBuilder(prefix), results);
            System.out.printf("suggest(\"%s\") -> %s%n", prefix, results);
        }
        System.out.println("Production twist: cache top-K per node for ranked suggestions.\n");
    }

    static void collect(Node node, StringBuilder path, List<String> out) {
        if (node.isWord) out.add(path.toString());
        for (int i = 0; i < 26; i++)
            if (node.children[i] != null) {
                path.append((char) ('a' + i));
                collect(node.children[i], path, out);
                path.deleteCharAt(path.length() - 1);        // backtrack
            }
    }

    // ------------------------------------------------------------------
    // EXAMPLE 3: wildcard search — '.' forces branching (DFS).
    // ------------------------------------------------------------------
    static void example3_wildcard() {
        System.out.println("=== Example 3: wildcard search ===");
        for (String pattern : new String[]{"b.nd", "a..le", "a.t", "..."}) {
            System.out.printf("search(\"%s\") = %s%n", pattern, wildcardSearch(pattern, 0, root));
        }
        System.out.println("Each '.' fans out to up to 26 branches — worst case O(26^dots · L).");
    }

    static boolean wildcardSearch(String w, int idx, Node node) {
        if (node == null) return false;
        if (idx == w.length()) return node.isWord;
        char c = w.charAt(idx);
        if (c == '.') {
            for (Node child : node.children)
                if (wildcardSearch(w, idx + 1, child)) return true;
            return false;
        }
        return wildcardSearch(w, idx + 1, node.children[c - 'a']);
    }
}
