# 15 — Bit Manipulation

## Why It Matters

A smaller topic, but it appears in phone screens (Single Number, counting bits),
and bit fluency signals systems depth. Real systems use bits everywhere: permission
flags, feature toggles, bloom filters, bitmap indexes, network masks, compression.

---

## 1. The Operators (Java)

| Op | Symbol | Example | Notes |
|----|--------|---------|-------|
| AND | `&` | 1100 & 1010 = 1000 | masking: keep selected bits |
| OR | `\|` | 1100 \| 1010 = 1110 | setting bits |
| XOR | `^` | 1100 ^ 1010 = 0110 | difference; self-inverse |
| NOT | `~` | ~0000 = 1111 | flips all bits |
| Left shift | `<<` | 1 << 3 = 8 | ×2 per shift |
| Arithmetic right | `>>` | -8 >> 1 = -4 | keeps sign bit |
| Logical right | `>>>` | -8 >>> 1 = huge positive | fills with 0 (Java-specific!) |

Java ints are 32-bit **two's complement**: `-x == ~x + 1`. There are no unsigned
ints; `>>>` is how Java compensates — a favorite Java-specific probe.

### Real-life analogy
**Light-switch panel**: 32 switches in a row = an int. AND asks "are these
specific lights on?", OR turns lights on, XOR toggles them, shifting renumbers the
panel. Unix file permissions (`chmod 755`) are literally this: rwxr-xr-x = 111 101 101.

### Production use cases
- **Permission/feature flags**: `user.flags & CAN_EDIT` — one int instead of 32 booleans.
- **Bitmap indexes** in databases and bitsets for set operations (`java.util.BitSet`).
- **IP subnetting**: `ip & subnetMask` = network address.
- HyperLogLog, Bloom filters, compact ID packing (e.g., Twitter Snowflake packs
  timestamp + machine + sequence into one long).

---

## 2. The Essential Tricks (memorize all)

```java
int getBit(int x, int i)   { return (x >> i) & 1; }
int setBit(int x, int i)   { return x | (1 << i); }
int clearBit(int x, int i) { return x & ~(1 << i); }
int toggleBit(int x, int i){ return x ^ (1 << i); }

boolean isPowerOfTwo(int x) { return x > 0 && (x & (x - 1)) == 0; }
int clearLowestSetBit(int x){ return x & (x - 1); }     // Kernighan's trick
int lowestSetBit(int x)     { return x & (-x); }        // isolates rightmost 1 (Fenwick trees!)
```

Why `x & (x-1)` clears the lowest set bit: subtracting 1 flips the lowest 1 to 0
and all zeros below it to 1s; AND wipes them all.

```java
// Count set bits (Hamming weight) — Kernighan: loops once per SET bit
int hammingWeight(int n) {
    int count = 0;
    while (n != 0) {
        n &= (n - 1);
        count++;
    }
    return count;
}
// Production: Integer.bitCount(n) — intrinsic, single POPCNT instruction
```

---

## 3. XOR — the Interview Star

Properties: `a^a = 0`, `a^0 = a`, commutative, associative.
XOR is "toggle" and "difference detector" — RAID parity and network checksums
rely on it.

```java
// Single Number — every element appears twice except one. O(n)/O(1)
int singleNumber(int[] nums) {
    int result = 0;
    for (int n : nums) result ^= n;              // pairs annihilate
    return result;
}

// Missing Number in 0..n — XOR indices against values
int missingNumber(int[] nums) {
    int x = nums.length;                         // start with n
    for (int i = 0; i < nums.length; i++) x ^= i ^ nums[i];
    return x;
}

// Swap without temp (party trick; mention aliasing bug if i == j)
a ^= b; b ^= a; a ^= b;
```

Harder variants: Single Number II (every element ×3 except one → count bits mod 3),
Single Number III (two uniques → split by `diff & (-diff)`).

---

## 4. Bitmasking — Sets as Integers

A 32-bit int represents a subset of ≤32 items: bit i set = item i included.
This gives O(1) subset compare/union/intersect and array-indexable set states.

```java
// Enumerate ALL subsets of a set of n items (n ≤ ~25)
for (int mask = 0; mask < (1 << n); mask++) {
    for (int i = 0; i < n; i++)
        if ((mask & (1 << i)) != 0) {
            // item i is in this subset
        }
}
```

### Bitmask DP (senior-level pattern, n ≤ 20)
State = which items are used. E.g., Traveling Salesman:
`dp[mask][i]` = min cost having visited set `mask`, currently at city `i`.

```java
// TSP skeleton — O(2ⁿ · n²)
int n = dist.length;
int[][] dp = new int[1 << n][n];
for (int[] row : dp) Arrays.fill(row, Integer.MAX_VALUE / 2);
dp[1][0] = 0;                                    // start at city 0
for (int mask = 1; mask < (1 << n); mask++)
    for (int last = 0; last < n; last++) {
        if ((mask & (1 << last)) == 0 || dp[mask][last] >= Integer.MAX_VALUE / 2) continue;
        for (int next = 0; next < n; next++) {
            if ((mask & (1 << next)) != 0) continue;
            int nextMask = mask | (1 << next);
            dp[nextMask][next] = Math.min(dp[nextMask][next],
                                          dp[mask][last] + dist[last][next]);
        }
    }
```

**Real-life:** delivery-route optimization over a small set of stops; task
assignment (workers × jobs) — Assignment Problem via bitmask DP.

```java
// Counting Bits — DP + bits: dp[i] = dp[i >> 1] + (i & 1). O(n)
int[] countBits(int n) {
    int[] dp = new int[n + 1];
    for (int i = 1; i <= n; i++) dp[i] = dp[i >> 1] + (i & 1);
    return dp;
}
```

---

## 5. Interview Pitfalls

- Operator precedence: `==` binds tighter than `&` → `(x & 1) == 0`, ALWAYS parenthesize.
- `1 << 32` doesn't overflow to 0 — Java shifts use only the low 5 bits of the
  shift amount (`1 << 32 == 1`!). Use `1L << k` for longs.
- Negative numbers with `>>` vs `>>>` — know which you need.
- `Integer.MIN_VALUE` has no positive counterpart: `-MIN_VALUE == MIN_VALUE`;
  `Math.abs` can return negative! Edge case in many problems.
- Mixing logical (`&&`) and bitwise (`&`) operators.
- Forgetting XOR swap breaks when both references are the same element.

## Must-Solve List
1. Single Number I, II, III
2. Number of 1 Bits / Counting Bits / Reverse Bits
3. Missing Number
4. Power of Two / Power of Four
5. Sum of Two Integers (add without + : XOR + carry loop)
6. Subsets via bitmask enumeration
7. Maximum XOR of Two Numbers (with trie, folder 08)
8. Divide Two Integers (shift-based)
9. Bitwise AND of Numbers Range

---

## Deep Dive & Worked Examples

> Runnable examples: `BitsDemo.java` in this folder — every trick printed in binary, XOR classics, bitmask subset enumeration, and Java's shift gotchas demonstrated live (`1 << 32 == 1`!).

### Two's complement, from scratch (so negatives stop being scary)

An n-bit two's complement number gives the top bit weight −2^(n−1) instead of
+2^(n−1). For 8 bits: `1111 1111` = −128 + 127 = −1. Negation: `-x = ~x + 1` —
flip all bits, add one. Check: x=5 → `0000 0101` → flip `1111 1010` → +1
`1111 1011` = −128+64+32+16+8+2+1 = −5 ✓.

Why this representation won: addition hardware is identical for signed/unsigned,
and there's exactly one zero. Consequence to remember: the range is asymmetric
(−2³¹ .. 2³¹−1), so `Integer.MIN_VALUE` has no positive twin — `Math.abs` can
return a negative number. Real bug, real interview question.

### Why `x & (-x)` isolates the lowest set bit

`-x = ~x + 1`. Flipping turns the trailing `1000...0` tail of x into `0111...1`;
adding 1 carries through those 1s, landing a single 1 exactly where x's lowest set
bit was — everything above is the complement of x. AND-ing keeps only that shared
bit. Example: x = `0110 0100` → -x = `1001 1100` → x & -x = `0000 0100`. This
operation is the engine of Fenwick trees (folder 16): `i += i & (-i)` walks
"responsibility ranges" up the implicit tree.

### Single Number II (appears 3× except one) — the bit-counting generalization

XOR handles pairs because bits cancel mod 2. For triples, count each bit position
mod 3:

```java
int singleNumberII(int[] nums) {
    int result = 0;
    for (int bit = 0; bit < 32; bit++) {
        int count = 0;
        for (int n : nums) count += (n >> bit) & 1;
        if (count % 3 != 0) result |= (1 << bit);   // the unique number owns this bit
    }
    return result;
}
```

O(32n) time, O(1) space, and it generalizes to "appears k times except one" by
changing the modulus. Deriving this from "XOR is mod-2 counting" is the insight
interviewers hope to see.

### Bitmask subset iteration — two idioms to memorize

```java
// All subsets of an n-element set:
for (int mask = 0; mask < (1 << n); mask++) { ... }

// All subsets OF A GIVEN MASK (submasks) — the pro move:
for (int sub = mask; sub > 0; sub = (sub - 1) & mask) { ... }
```

The submask loop visits each submask exactly once in decreasing order; across all
masks it totals O(3ⁿ) — needed for "partition into groups" bitmask DPs. Knowing the
`(sub - 1) & mask` idiom marks you as someone who has actually done bitmask DP.

### Interviewer follow-ups you should expect

- *"Add two numbers without + or -"* — sum = a ^ b (no-carry add), carry =
  (a & b) << 1; loop until carry is 0. Works because XOR+AND decompose addition.
- *"Count bits of all numbers 0..n in O(n)"* — `dp[i] = dp[i >> 1] + (i & 1)`:
  i's bits are i/2's bits plus its lowest bit.
- *"Reverse the bits of an int"* — swap halves progressively (16, 8, 4, 2, 1) with
  masks, or loop 32 times building the result. Know the loop version cold.
- *"Why do HashMap capacities being powers of 2 matter here?"* `hash & (cap-1)`
  equals `hash % cap` only when cap = 2^k (cap−1 is an all-ones mask) — bitwise AND
  is far cheaper than division. Cross-topic connections like this impress.

**Next:** `16-advanced-structures`
