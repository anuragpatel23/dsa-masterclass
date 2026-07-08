# 05 — Hashing (HashMap, HashSet & Design)

## Why It Matters

The HashMap is the most used tool in coding interviews — the default answer to
"can we trade space for time?" Senior candidates are additionally expected to
explain **internals**: hashing, collisions, resizing, treeification, and the
`hashCode`/`equals` contract. This is one of the most common Java deep-dive areas.

---

## 1. How a HashMap Works Internally (Java 8+)

1. `put(key, value)` → compute `key.hashCode()`, spread it (`h ^ (h >>> 16)`), then
   index = `hash & (capacity - 1)` (capacity is a power of 2, so `&` replaces `%`).
2. Bucket empty → place entry. Bucket occupied → **collision**:
   - Java uses **separate chaining**: entries form a linked list in the bucket.
   - If a bucket's list exceeds **8 entries** (and table size ≥ 64), it converts to a
     **red-black tree** → worst case per-bucket O(log n) instead of O(n).
3. When `size > capacity × loadFactor` (default 0.75) → **resize**: double capacity,
   rehash all entries (O(n), amortized away).

### Real-life analogy
A **coat check** at a theatre. Your ticket number (hash) maps to a hook (bucket).
Two coats on one hook (collision) → they hang together and the attendant checks tags
(`equals`) to find yours. When hooks overflow regularly, the theatre installs double
the hooks and re-hangs everything (resize).

### Production use cases
- Every cache, index, deduplication, and session store.
- Database hash indexes and hash joins.
- **Consistent hashing** (DynamoDB, Cassandra, CDNs) — mention when asked how
  hashing scales across machines: keys map to a ring, so adding/removing a node
  remaps only ~1/n of the keys instead of nearly all of them.

---

## 2. The hashCode / equals Contract (senior must-know)

Rules:
1. `a.equals(b)` ⇒ `a.hashCode() == b.hashCode()` — equal objects MUST share a hash.
2. Same hash does NOT imply equals (collisions are legal).
3. Both must be consistent across calls, and consistent with each other.

**What breaks if you violate it:** put an object in a `HashSet`, then it "disappears"
— lookups probe the wrong bucket.

```java
// A correct value class
final class Point {
    private final int x, y;
    Point(int x, int y) { this.x = x; this.y = y; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point)) return false;
        Point p = (Point) o;
        return x == p.x && y == p.y;
    }
    @Override public int hashCode() { return Objects.hash(x, y); }
}
```

**Trap question:** *mutating a key after inserting it* — the entry is stranded in the
old bucket; `get` fails. That's why keys should be immutable (String, Integer, records).

---

## 3. Java Map/Set Landscape — Trade-off Table

| Class | Ordering | get/put | Nulls | Backing | Use when |
|-------|----------|---------|-------|---------|----------|
| HashMap | none | O(1) avg | 1 null key | array of bins | default choice |
| LinkedHashMap | insertion (or access) order | O(1) | yes | + doubly linked list | LRU cache, predictable iteration |
| TreeMap | sorted by key | O(log n) | no null key | red-black tree | range queries, floor/ceiling |
| ConcurrentHashMap | none | O(1) | no nulls | CAS + synchronized bins | thread-safe (never Hashtable) |
| HashSet / TreeSet / LinkedHashSet | same as map versions | — | — | backed by maps | membership |

**TreeMap superpowers** interviews use: `floorKey`, `ceilingKey`, `firstKey`,
`headMap/tailMap/subMap` — e.g., "find nearest booked slot" in calendar problems.

---

## 4. Core Interview Patterns

### Pattern A — Complement lookup (Two Sum)
```java
int[] twoSum(int[] nums, int target) {          // O(n) time / O(n) space
    Map<Integer, Integer> seen = new HashMap<>(); // value → index
    for (int i = 0; i < nums.length; i++) {
        Integer j = seen.get(target - nums[i]);
        if (j != null) return new int[]{j, i};
        seen.put(nums[i], i);
    }
    throw new IllegalArgumentException("no solution");
}
```

### Pattern B — Frequency map
```java
// Top K Frequent Elements — freq map + bucket sort, O(n)
int[] topKFrequent(int[] nums, int k) {
    Map<Integer, Integer> freq = new HashMap<>();
    for (int n : nums) freq.merge(n, 1, Integer::sum);

    List<Integer>[] buckets = new List[nums.length + 1];   // index = frequency
    freq.forEach((num, f) -> {
        if (buckets[f] == null) buckets[f] = new ArrayList<>();
        buckets[f].add(num);
    });

    int[] result = new int[k]; int idx = 0;
    for (int f = buckets.length - 1; f >= 0 && idx < k; f--)
        if (buckets[f] != null)
            for (int num : buckets[f]) { result[idx++] = num; if (idx == k) break; }
    return result;
}
```

### Pattern C — Set for O(1) membership
```java
// Longest Consecutive Sequence — O(n); only sequence-starts trigger inner loop
int longestConsecutive(int[] nums) {
    Set<Integer> set = new HashSet<>();
    for (int n : nums) set.add(n);
    int best = 0;
    for (int n : set) {
        if (set.contains(n - 1)) continue;      // not a sequence start
        int len = 1;
        while (set.contains(n + len)) len++;
        best = Math.max(best, len);
    }
    return best;
}
```

### Pattern D — Canonical key (group by signature)
Group Anagrams (folder 02): sorted string as key. Same idea: normalize phone
numbers, canonicalize URLs, dedupe near-identical records.

### Pattern E — Prefix-sum + HashMap
Subarray Sum Equals K (folder 02) — remember prefix frequencies.

---

## 5. Design Question — Insert/Delete/GetRandom O(1)

**Real-life:** serving a random ad/song from a rotating pool with fast add/remove.
Trick: pair an `ArrayList` (random access) with a `HashMap` (value → index);
delete via swap-with-last.

```java
class RandomizedSet {
    private final List<Integer> list = new ArrayList<>();
    private final Map<Integer, Integer> indexOf = new HashMap<>();
    private final Random rand = new Random();

    boolean insert(int val) {
        if (indexOf.containsKey(val)) return false;
        indexOf.put(val, list.size());
        list.add(val);
        return true;
    }

    boolean remove(int val) {
        Integer i = indexOf.get(val);
        if (i == null) return false;
        int last = list.get(list.size() - 1);
        list.set(i, last);                       // move last element into the hole
        indexOf.put(last, i);
        list.remove(list.size() - 1);            // O(1) removal from end
        indexOf.remove(val);
        return true;
    }

    int getRandom() { return list.get(rand.nextInt(list.size())); }
}
```

---

## 6. Beyond the Basics (name-drop with understanding)

- **Bloom filter** — probabilistic set: "definitely not present" or "maybe present",
  in tiny memory. Used by Cassandra/HBase to skip disk reads, by browsers for
  malicious-URL checks. No false negatives, tunable false positives, no deletes (basic form).
- **Consistent hashing** — distributing keys across servers; virtual nodes smooth the load.
- **Open addressing vs chaining** — Python dicts use open addressing; Java chains.
  Open addressing is cache-friendlier; chaining degrades more gracefully at high load.
- **Cryptographic vs non-crypto hashes** — SHA-256 for integrity/security; Murmur/xxHash
  for speed in data structures. Never use `hashCode()` for security.

---

## 7. Interview Pitfalls

- Autoboxing: `Map<Integer,Integer>` comparisons with `==` fail beyond the [-128,127]
  cache — use `.equals()` or unbox.
- Mutating keys after insertion (see §2).
- Iterating while modifying → `ConcurrentModificationException`; use `Iterator.remove`
  or collect keys first.
- Assuming HashMap iteration order — it's undefined AND can change after rehash.
- `getOrDefault` / `merge` / `computeIfAbsent` — idiomatic Java saves lines and bugs.
- Worst-case adversarial keys: pre-Java-8 HashMaps were DoS-able via collision floods
  — treeification was the fix. Great senior anecdote.

## Must-Solve List
1. Two Sum / 3Sum (3Sum uses sort + two pointers, contrast the approaches)
2. Group Anagrams
3. Top K Frequent Elements
4. Longest Consecutive Sequence
5. Subarray Sum Equals K
6. Valid Sudoku
7. Insert Delete GetRandom O(1)
8. First Missing Positive (array-as-hashmap trick, O(1) space)
9. LRU Cache (folder 03) / LFU Cache (folder 16)
10. Design HashMap (implement chaining from scratch)

---

## Deep Dive & Worked Examples

> Runnable examples: `HashingDemo.java` in this folder (`javac HashingDemo.java && java HashingDemo`) — including a live demonstration of a key "disappearing" when `hashCode` is missing.

### Walking one `put()` through HashMap internals

`map.put("cat", 1)` with default capacity 16:

1. `"cat".hashCode()` → 98262 (cached in the String after first call).
2. Spread: `h ^ (h >>> 16)` — mixes high bits into low bits. Why: the index mask
   only looks at low bits; without spreading, keys differing only in high bits
   (common with Float keys, sequential IDs × table size) would all collide.
3. Index: `hash & (16 - 1)` = `hash & 0b1111` → bucket 6 (say). Power-of-two capacity
   makes the mask trick work — this is why HashMap capacities are always 2^k even if
   you request 100 (it rounds to 128).
4. Bucket empty → store Node(hash, "cat", 1). If occupied → walk the chain comparing
   `hash` first (cheap) then `equals` (expensive) — this is why hashCode should be
   fast AND well-distributed.
5. `size > 16 × 0.75 = 12` → resize to 32, rehash. Java 8 optimization: on resize,
   each entry goes to either its old index or old index + oldCapacity (one bit
   decides) — no full rehash of the key.

### Load factor: why 0.75?

Expected chain length under uniform hashing follows a Poisson distribution: at load
0.75, ~80% of buckets have 0–1 entries and chains of length 8 are astronomically
rare (~1 in 10⁷) — which is exactly why 8 was chosen as the treeify threshold: if
you ever see 8, your hash function (or an adversary) is broken, so switch to a tree.
Higher load factor = less memory, more collisions; lower = the reverse. You can tune
it in the constructor, and pre-sizing (`new HashMap<>(expectedSize * 4 / 3 + 1)`)
avoids rehash pauses — a real production tip worth saying.

### Choosing the canonical key (Pattern D expanded)

The art of hashing problems is choosing WHAT to hash:

| Problem | Canonical key |
|---------|--------------|
| group anagrams | sorted string, or char-count string "1a1e1t" |
| detect duplicate boards/states | serialized state string |
| points on the same line through P | reduced fraction slope `(dy/g, dx/g)` with sign normalization |
| duplicate subtrees | postorder serialization with null markers |
| rows with same pattern | normalized first-element-relative pattern |

If your key needs floating point (slopes!), normalize to integer pairs — doubles as
keys invite precision bugs; say this before the interviewer does.

### Interviewer follow-ups you should expect

- *"What happens if two keys have the same hashCode but aren't equal?"* Chaining:
  they share a bucket, `equals` disambiguates. Performance degrades, correctness doesn't.
- *"Why must you not mutate a HashMap key?"* The entry lives in the bucket chosen by
  its OLD hash; lookups compute the NEW hash → wrong bucket → key unreachable
  (memory leak + logic bug). Demo file shows the effect.
- *"HashMap vs ConcurrentHashMap vs Collections.synchronizedMap?"* CHM: lock-free
  reads, per-bin locking on writes, no null keys/values, weakly consistent iterators
  (no ConcurrentModificationException). synchronizedMap: one lock, still fails fast
  on iteration. Never Hashtable (legacy).
- *"Design a hash function for a Point class"* — `31 * x + y` and why 31: odd prime,
  multiplication by 31 is `(x << 5) - x` (JIT optimizes), good empirical spread.
- *"When would you NOT use a HashMap?"* Need ordering/range queries (TreeMap), need
  cache-friendly iteration over small fixed keys (array), memory-constrained sets of
  ints (BitSet/bloom filter), need stable iteration (LinkedHashMap).

**Next:** `06-trees-bst`
