# 04 — Stacks & Queues

## Why It Matters

Stacks and queues are simple structures with deep pattern families. The **monotonic
stack** alone covers a whole class of medium/hard questions (Next Greater Element,
Largest Rectangle in Histogram, Daily Temperatures) that product companies ask
constantly. Queues are the backbone of BFS (folder 09).

---

## 1. Stack (LIFO)

### Real-life analogy
A stack of cafeteria trays: you take the top one, you put returns on top. Also:
the **Undo button** — every edit pushes; Ctrl+Z pops the latest.

### Production use cases
- **Call stack** — every method call/return in the JVM.
- **Undo/redo** — two stacks (undo stack, redo stack).
- **Browser back button** — back stack + forward stack.
- **Expression parsing / compilers** — matching brackets, evaluating expressions.
- **DFS** — explicit stack replaces recursion.

### Java choice
Use `ArrayDeque`, not the legacy `java.util.Stack` (which is a synchronized `Vector`
— slow and allows index access, breaking LIFO discipline). Saying this unprompted
is a good Java-depth signal.

```java
Deque<Integer> stack = new ArrayDeque<>();
stack.push(1);          // add to top
int top = stack.peek(); // look at top
int x = stack.pop();    // remove top
```

All operations O(1) (amortized, array-backed).

### Classic 1 — Valid Parentheses
```java
boolean isValid(String s) {
    Deque<Character> stack = new ArrayDeque<>();
    Map<Character, Character> pairs = Map.of(')', '(', ']', '[', '}', '{');
    for (char c : s.toCharArray()) {
        if (pairs.containsValue(c)) stack.push(c);              // opener
        else if (stack.isEmpty() || stack.pop() != pairs.get(c)) return false;
    }
    return stack.isEmpty();
}
```

### Classic 2 — Min Stack (O(1) getMin)
Trick: each entry also remembers the min *at the time it was pushed*.

```java
class MinStack {
    private final Deque<long[]> stack = new ArrayDeque<>();   // [value, minSoFar]

    void push(int val) {
        long min = stack.isEmpty() ? val : Math.min(val, stack.peek()[1]);
        stack.push(new long[]{val, min});
    }
    void pop()      { stack.pop(); }
    int top()       { return (int) stack.peek()[0]; }
    int getMin()    { return (int) stack.peek()[1]; }
}
```

---

## 2. Monotonic Stack — the Pattern That Wins Interviews

A stack kept in increasing (or decreasing) order; when a new element breaks the
order, pop and *resolve* the popped elements. Turns O(n²) "for each element, look
right/left" problems into O(n) — each element is pushed and popped at most once.

### Real-life analogy
People queueing for a skyline photo: a taller person arriving means everyone shorter
in front of them can never be "the next taller person" for anyone behind — they're
resolved and leave.

### Daily Temperatures — days until a warmer day
```java
int[] dailyTemperatures(int[] temps) {          // O(n) time
    int[] answer = new int[temps.length];
    Deque<Integer> stack = new ArrayDeque<>();  // indices, temps decreasing
    for (int i = 0; i < temps.length; i++) {
        while (!stack.isEmpty() && temps[i] > temps[stack.peek()]) {
            int prev = stack.pop();
            answer[prev] = i - prev;            // resolved: found its warmer day
        }
        stack.push(i);
    }
    return answer;
}
```

### Largest Rectangle in Histogram (hard, high-frequency)
For each bar, the widest rectangle with that bar's height spans until the first
shorter bar on each side — exactly what a monotonic increasing stack finds.

```java
int largestRectangleArea(int[] heights) {       // O(n)
    Deque<Integer> stack = new ArrayDeque<>();
    int best = 0;
    for (int i = 0; i <= heights.length; i++) {
        int h = (i == heights.length) ? 0 : heights[i];   // sentinel flushes stack
        while (!stack.isEmpty() && h < heights[stack.peek()]) {
            int height = heights[stack.pop()];
            int width = stack.isEmpty() ? i : i - stack.peek() - 1;
            best = Math.max(best, height * width);
        }
        stack.push(i);
    }
    return best;
}
```

Family members: Next Greater Element I/II, Trapping Rain Water (stack variant),
Remove K Digits, Stock Span.

---

## 3. Queue (FIFO)

### Real-life analogy
A ticket counter line — first come, first served. In production: **message queues**
(Kafka, SQS, RabbitMQ) decouple producers from consumers; **thread-pool task queues**
(`LinkedBlockingQueue` inside `ThreadPoolExecutor`); **BFS frontiers**.

```java
Queue<Integer> queue = new ArrayDeque<>();
queue.offer(1);          // enqueue at tail
int front = queue.peek();
int x = queue.poll();    // dequeue from head
```

### Deque (double-ended queue)
`ArrayDeque` supports O(1) push/pop at **both** ends — it's Java's stack AND queue.

### Sliding Window Maximum — monotonic deque (hard classic)
Keep deque of indices with values in decreasing order; front is always the window max.

```java
int[] maxSlidingWindow(int[] nums, int k) {     // O(n)
    Deque<Integer> dq = new ArrayDeque<>();     // indices, values decreasing
    int[] result = new int[nums.length - k + 1];
    for (int i = 0; i < nums.length; i++) {
        if (!dq.isEmpty() && dq.peekFirst() <= i - k) dq.pollFirst(); // out of window
        while (!dq.isEmpty() && nums[dq.peekLast()] < nums[i]) dq.pollLast(); // dominated
        dq.offerLast(i);
        if (i >= k - 1) result[i - k + 1] = nums[dq.peekFirst()];
    }
    return result;
}
```

### Circular queue / ring buffer
Fixed-size array + head/tail indices with modulo wraparound. Zero allocation in
steady state — used in Kafka log segments, audio buffers, LMAX Disruptor.

```java
class CircularQueue {
    private final int[] data;
    private int head = 0, size = 0;

    CircularQueue(int capacity) { data = new int[capacity]; }

    boolean enqueue(int v) {
        if (size == data.length) return false;
        data[(head + size) % data.length] = v;
        size++;
        return true;
    }
    Integer dequeue() {
        if (size == 0) return null;
        int v = data[head];
        head = (head + 1) % data.length;
        size--;
        return v;
    }
}
```

---

## 4. Design Questions (asked as-is)

```java
// Implement Queue using two Stacks — amortized O(1) per op
class MyQueue {
    private final Deque<Integer> in = new ArrayDeque<>();
    private final Deque<Integer> out = new ArrayDeque<>();

    void push(int x) { in.push(x); }

    int pop() {
        if (out.isEmpty())
            while (!in.isEmpty()) out.push(in.pop());  // reverse once, serve many
        return out.pop();
    }
    int peek() {
        if (out.isEmpty())
            while (!in.isEmpty()) out.push(in.pop());
        return out.peek();
    }
    boolean empty() { return in.isEmpty() && out.isEmpty(); }
}
```

Each element moves at most twice (in→out) → amortized O(1). This "lazy reversal"
idea also underlies functional/persistent queues.

---

## 5. Complexity Summary

| Structure | push/offer | pop/poll | peek | Notes |
|-----------|-----------|----------|------|-------|
| ArrayDeque (stack) | O(1)* | O(1) | O(1) | *amortized (resize) |
| ArrayDeque (queue) | O(1)* | O(1) | O(1) | no nulls allowed |
| Monotonic stack scan | O(n) total | — | — | each element pushed/popped once |
| PriorityQueue | O(log n) | O(log n) | O(1) | see folder 07 — NOT FIFO |

---

## 6. Interview Pitfalls

- Using `java.util.Stack` — mention `ArrayDeque` instead.
- Monotonic stack: store **indices**, not values — you almost always need positions.
- Forgetting the sentinel flush (the `h = 0` trick) in histogram-type problems.
- `ArrayDeque` rejects `null` — don't use null as a sentinel value.
- Amortized vs worst case for two-stack queue — a single `pop` can be O(n).
- Confusing `push/pop` (head) with `offer/poll` (tail) semantics on Deque — be deliberate.

## Must-Solve List
1. Valid Parentheses
2. Min Stack
3. Evaluate Reverse Polish Notation
4. Daily Temperatures / Next Greater Element I & II
5. Largest Rectangle in Histogram
6. Trapping Rain Water
7. Sliding Window Maximum
8. Implement Queue using Stacks / Stack using Queues
9. Design Circular Queue
10. Remove K Digits

---

## Deep Dive & Worked Examples

> Runnable examples: `StacksQueuesDemo.java` in this folder (`javac StacksQueuesDemo.java && java StacksQueuesDemo`)

### Dry run: Daily Temperatures [73, 74, 75, 71, 69, 72, 76, 73]

Stack holds **indices** of days still waiting for a warmer day (their temps are
decreasing top-to-bottom is impossible — the stack stays decreasing bottom-to-top):

| i | temp | action | stack (indices) | answers resolved |
|---|------|--------|------------------|------------------|
| 0 | 73 | push | [0] | — |
| 1 | 74 | 74>73 → pop 0 (wait 1), push | [1] | ans[0]=1 |
| 2 | 75 | 75>74 → pop 1 (wait 1), push | [2] | ans[1]=1 |
| 3 | 71 | push | [2,3] | — |
| 4 | 69 | push | [2,3,4] | — |
| 5 | 72 | pops 4 (wait 1), 3 (wait 2), push | [2,5] | ans[4]=1, ans[3]=2 |
| 6 | 76 | pops 5 (wait 1), 2 (wait 4), push | [6] | ans[5]=1, ans[2]=4 |
| 7 | 73 | push | [6,7] | — |

Left on the stack at the end → no warmer day → answer 0. Total pushes = n, total
pops ≤ n → **O(n)** despite the nested `while`. This "each element enters and leaves
once" argument is the standard proof for all monotonic-stack problems — rehearse
saying it.

### How to RECOGNIZE a monotonic stack problem

The tell: for each element you need **the nearest element to the left/right that is
bigger/smaller**. Four variants → four stack polarities:

| Need | Stack keeps | Pop when |
|------|-------------|----------|
| next greater to the right | decreasing values | new element is bigger |
| next smaller to the right | increasing values | new element is smaller |
| previous greater | decreasing, answer = new top after pops | — |
| previous smaller | increasing, answer = new top after pops | — |

Largest Rectangle in Histogram needs BOTH boundaries (first shorter bar left and
right) — one increasing stack finds both simultaneously: the pop moment reveals the
right boundary (current index) and the left boundary (new stack top).

### Why `ArrayDeque` beats `Stack` and `LinkedList`

`java.util.Stack` extends `Vector`: every method synchronized (uncontended locks
still cost), plus it exposes index-based access that violates LIFO. `LinkedList`
allocates a node per element. `ArrayDeque` is a circular array — no per-element
allocation, cache-friendly, amortized O(1) at both ends. The one trade-off:
`ArrayDeque` rejects null elements (it uses null internally as an empty marker).

### Interviewer follow-ups you should expect

- *"Implement max-stack with O(1) pop AND popMax"* — two stacks won't do; use a
  doubly linked list + TreeMap (O(log n) popMax) and discuss the trade-offs.
- *"Sliding window max: why store indices, not values?"* You must detect when the
  front element leaves the window (`dq.peekFirst() <= i - k`) — only indices tell you.
- *"Design a rate limiter for 100 req/min"* — a queue of timestamps: evict from the
  front while older than 60s, accept if size < 100. This is the sliding-window-log
  rate limiter; mention token bucket as the constant-memory alternative.
- *"Evaluate an expression with + - × ÷ and parentheses"* — two stacks (values,
  operators) with precedence handling: the shunting-yard idea. Practice Basic
  Calculator I–III if targeting companies that ask parsing questions.

**Next:** `05-hashing`
