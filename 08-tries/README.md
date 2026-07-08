# 08 — Tries (Prefix Trees)

## Why It Matters

Tries appear in a small but reliable set of interview problems (implement trie,
word search II, autocomplete, add-and-search with wildcards) and in a LOT of real
systems. If a problem involves many strings sharing prefixes, think trie.

---

## 1. What a Trie Is

A tree where each edge is a character; a path from the root spells a prefix.
Nodes mark whether a complete word ends there. Lookup cost depends on **word
length L, not dictionary size n** — searching 1 word among 10 million costs O(L).

### Real-life analogy
**Phone contact search**: type "Jo" and instantly see John, Joe, Joanna. Your phone
walks two edges (J→o) and every name below that node matches. Same mechanism:
Google autocomplete, IDE symbol completion, spell checkers, T9 keyboards.

### Production use cases
- Autocomplete / typeahead (with per-node top-K caching for ranking).
- **IP routing** — longest-prefix match on binary tries (radix tries) in routers.
- Spell checkers & dictionaries (often as compressed DAWGs).
- Search-engine query suggestion; blocked-word filtering.

### Trade-offs vs HashMap

| Aspect | Trie | HashSet of strings |
|--------|------|--------------------|
| exact lookup | O(L) | O(L) (hashing reads the string too) |
| **prefix queries** | O(prefix) then subtree | O(n·L) — must scan everything |
| ordered iteration | free (DFS = lexicographic) | no |
| memory | can be heavy (26 pointers/node) | compact |
| wildcard search | natural (branch on '.') | hard |

The one-line justification interviewers want: *"HashMap can't answer prefix
questions; a trie makes prefix cost independent of dictionary size."*

---

## 2. Implementation (memorize — asked verbatim)

```java
class Trie {
    private static class Node {
        Node[] children = new Node[26];   // lowercase a-z; use HashMap for Unicode
        boolean isWord;
    }

    private final Node root = new Node();

    public void insert(String word) {                  // O(L)
        Node cur = root;
        for (char c : word.toCharArray()) {
            int i = c - 'a';
            if (cur.children[i] == null) cur.children[i] = new Node();
            cur = cur.children[i];
        }
        cur.isWord = true;
    }

    public boolean search(String word) {               // O(L)
        Node n = find(word);
        return n != null && n.isWord;
    }

    public boolean startsWith(String prefix) {         // O(L)
        return find(prefix) != null;
    }

    private Node find(String s) {
        Node cur = root;
        for (char c : s.toCharArray()) {
            cur = cur.children[c - 'a'];
            if (cur == null) return null;
        }
        return cur;
    }
}
```

Design choices to narrate: array vs HashMap children (speed/memory vs flexibility);
`isWord` flag vs storing values (trie as a map); optional `count` fields for
frequency-ranked autocomplete.

---

## 3. Wildcard Search (Add & Search Words)

'.' matches any character → DFS branching at dots.

```java
class WordDictionary {
    private static class Node {
        Node[] children = new Node[26];
        boolean isWord;
    }
    private final Node root = new Node();

    public void addWord(String word) { /* same as Trie.insert */
        Node cur = root;
        for (char c : word.toCharArray()) {
            int i = c - 'a';
            if (cur.children[i] == null) cur.children[i] = new Node();
            cur = cur.children[i];
        }
        cur.isWord = true;
    }

    public boolean search(String word) { return dfs(word, 0, root); }

    private boolean dfs(String w, int idx, Node node) {
        if (node == null) return false;
        if (idx == w.length()) return node.isWord;
        char c = w.charAt(idx);
        if (c == '.') {
            for (Node child : node.children)
                if (dfs(w, idx + 1, child)) return true;
            return false;
        }
        return dfs(w, idx + 1, node.children[c - 'a']);
    }
}
```

---

## 4. Word Search II — Trie + Backtracking (hard classic)

Find dictionary words in a letter grid. Naive: run grid-DFS per word — O(words ×
grid). Trie fix: insert ALL words, then ONE DFS over the grid walking the trie in
lockstep; prune the instant a prefix isn't in the trie.

```java
public List<String> findWords(char[][] board, String[] words) {
    Node root = buildTrie(words);
    List<String> found = new ArrayList<>();
    for (int r = 0; r < board.length; r++)
        for (int c = 0; c < board[0].length; c++)
            dfs(board, r, c, root, found);
    return found;
}

private void dfs(char[][] b, int r, int c, Node node, List<String> found) {
    if (r < 0 || r >= b.length || c < 0 || c >= b[0].length) return;
    char ch = b[r][c];
    if (ch == '#') return;                        // visited
    Node next = node.children[ch - 'a'];
    if (next == null) return;                     // prefix pruning — the whole point
    if (next.word != null) {                      // store word at end node
        found.add(next.word);
        next.word = null;                         // dedupe
    }
    b[r][c] = '#';                                // mark visited
    dfs(b, r + 1, c, next, found);
    dfs(b, r - 1, c, next, found);
    dfs(b, r, c + 1, next, found);
    dfs(b, r, c - 1, next, found);
    b[r][c] = ch;                                 // backtrack
}

private static class Node {
    Node[] children = new Node[26];
    String word;                                  // non-null only at word ends
}
private Node buildTrie(String[] words) {
    Node root = new Node();
    for (String w : words) {
        Node cur = root;
        for (char c : w.toCharArray()) {
            int i = c - 'a';
            if (cur.children[i] == null) cur.children[i] = new Node();
            cur = cur.children[i];
        }
        cur.word = w;
    }
    return root;
}
```

This problem is beloved because it combines three topics: tries, DFS, backtracking.

---

## 5. Variants Worth Knowing by Name

- **Radix / Patricia trie** — compress single-child chains into one edge; used in
  IP routing tables and Linux kernel data structures.
- **Suffix trie/tree & suffix array** — all substrings of a text; powers full-text
  search and bioinformatics. (Know the concept; implementation rarely asked.)
- **DAWG** — trie with shared suffixes; minimal-memory dictionaries.
- **Binary trie over bits** — Maximum XOR of Two Numbers (walk bits preferring the
  opposite bit) — a favorite "surprising trie" question.

```java
// Max XOR pair using a binary trie over 32-bit numbers — O(32n)
// Insert all numbers bit by bit; for each number greedily walk opposite bits.
```

---

## 6. Complexity Summary

| Operation | Cost | Space |
|-----------|------|-------|
| insert / search / startsWith | O(L) | total O(sum of word lengths × alphabet factor) |
| autocomplete (list all under prefix) | O(prefix + matches) | — |
| Word Search II | O(cells × 4^maxLen) worst, heavily pruned | trie size |

## 7. Interview Pitfalls

- Forgetting `isWord`: "app" being a prefix of "apple" doesn't make "app" a word.
- Memory blowups with `new Node[26]` per node for large alphabets — switch to
  `HashMap<Character, Node>` and say why.
- In grid DFS: forgetting to restore the cell after backtracking, or marking
  visited too late (allows reuse of the same cell).
- Deleting from a trie (follow-up): unlink nodes bottom-up only when childless
  and not word-ends — walk through it verbally before coding.
- Assuming lowercase — clarify alphabet up front.

## Must-Solve List
1. Implement Trie (Prefix Tree)
2. Design Add and Search Words (wildcard)
3. Word Search II
4. Longest Word in Dictionary
5. Replace Words (shortest-root replacement)
6. Maximum XOR of Two Numbers in an Array (binary trie)
7. Design Search Autocomplete System (trie + top-K ranking — design flavored)

---

## Deep Dive & Worked Examples

> Runnable examples: `TrieDemo.java` in this folder — basics, lexicographic autocomplete, and wildcard search.

### Visualizing the trie after inserting app, apple, apt, band

```
root
 └── a
     └── p
         ├── p (isWord: "app")
         │   └── l
         │       └── e (isWord: "apple")
         └── t (isWord: "apt")
 └── b
     └── a
         └── n
             └── d (isWord: "band")
```

Notes worth making while drawing this in an interview: shared prefixes ("ap") are
stored ONCE — that's the memory win for dictionaries with heavy prefix overlap; the
`isWord` flag at 'p' is what distinguishes the word "app" from the mere prefix "appl";
and DFS visiting children in a–z order emits words lexicographically sorted for free.

### Memory math (the senior consideration)

Array-of-26 nodes: each node = 26 refs × 8 bytes + header ≈ 220+ bytes even when
only 1–2 children exist. One million words × avg 8 chars could mean millions of
nodes → gigabytes if naive. Mitigations, in the order you'd offer them:
1. `HashMap<Character, Node>` children — pay only for edges that exist (slower, ~2–3×).
2. **Radix/Patricia compression** — collapse single-child chains ("pple" as one edge).
3. **DAWG** — also share suffixes; minimal automaton for a fixed dictionary.
4. Offline/static: sorted array + binary search on prefixes may beat all of them.

Choosing between these based on read/write mix and alphabet size is exactly the
kind of trade-off discussion senior interviews reward.

### Autocomplete at production scale (design tie-in)

A naive "DFS the subtree per keystroke" doesn't survive real traffic. The standard
design: precompute and **cache the top-K completions at every node** (by search
frequency), refreshed offline; each keystroke is then O(prefix) + O(K). Add:
frequency decay for trending queries, a separate personalization layer, and sharding
the trie by prefix range across servers. If an interviewer extends the trie question
into "design typeahead," this paragraph is the skeleton of your answer.

### Interviewer follow-ups you should expect

- *"Delete 'apple' from the trie"* — unset `isWord` at the 'e'; then walk back up
  removing nodes that are now childless AND not word-ends. Do it iteratively with a
  parent stack or recursively returning "am I deletable".
- *"Why is trie search O(L) when HashMap is also O(L)?"* Both read all L chars, but
  the trie ALSO answers prefix/ordered queries; the HashMap hash is one pass with no
  structure. Equal exact-lookup cost, unequal capabilities — the trie's price is memory.
- *"Case sensitivity? Unicode?"* Ask first. Unicode → HashMap children or work on
  code points; never assume `- 'a'` silently.
- *"Word Search II: why does the trie beat running Word Search per word?"* One grid
  DFS serves ALL words simultaneously, and absent prefixes prune instantly — the
  dictionary's size stops multiplying the grid work.

**Next:** `09-graphs`
