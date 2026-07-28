# 03 — Count of Subset Sum

## Problem
**Count** how many subsets sum to exactly `target`.

## How to Identify
- Subset-sum structure, but the question is **"how many ways"** → counting DP.
- Swap boolean OR for integer **addition**: include-count + exclude-count.

## Recurrence
```
solve(n, s):
    if (s == 0) return 1          # one way: the empty subset
    if (n == 0) return 0
    if (arr[n-1] <= s)
        return solve(n-1, s - arr[n-1]) + solve(n-1, s)   # include + exclude
    return solve(n-1, s)
```

```mermaid
graph TD
    S["count(n, s)"] --> Q{"arr[n-1] <= s?"}
    Q -->|yes| ADD["count(n-1,s-arr) + count(n-1,s)"]
    Q -->|no| EX["count(n-1, s)"]
```

> Note on zeros: each `0` in the array doubles the count (include/exclude both valid).
> Handle carefully if the array contains zeros.

## Complexity
O(n·target) time, O(target) space rolled.

Examples: basic count, count with a rolled 1-D array, and "number of ways to reach a
score".
