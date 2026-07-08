import java.util.Arrays;

/**
 * Runnable illustrations for Sorting & Searching.
 * Compile & run:  javac SortingSearchingDemo.java && java SortingSearchingDemo
 *
 * Example 1: merge sort with per-level trace
 * Example 2: binary search boundary variants (first/last occurrence)
 * Example 3: QuickSelect — Kth largest without full sorting
 * Example 4: binary search on the ANSWER (Koko eating bananas)
 */
public class SortingSearchingDemo {

    public static void main(String[] args) {
        example1_mergeSort();
        example2_boundaries();
        example3_quickSelect();
        example4_searchOnAnswer();
    }

    // ------------------------------------------------------------------
    // EXAMPLE 1: merge sort — watch halves get merged back sorted.
    // ------------------------------------------------------------------
    static void example1_mergeSort() {
        System.out.println("=== Example 1: merge sort trace ===");
        int[] a = {38, 27, 43, 3, 9, 82, 10};
        System.out.println("input : " + Arrays.toString(a));
        mergeSort(a, 0, a.length, 0);
        System.out.println("sorted: " + Arrays.toString(a) + "\n");
    }

    static void mergeSort(int[] a, int lo, int hi, int depth) {
        if (hi - lo <= 1) return;
        int mid = (lo + hi) / 2;
        mergeSort(a, lo, mid, depth + 1);
        mergeSort(a, mid, hi, depth + 1);
        int[] tmp = new int[hi - lo];
        int i = lo, j = mid, k = 0;
        while (i < mid && j < hi) tmp[k++] = a[i] <= a[j] ? a[i++] : a[j++];
        while (i < mid) tmp[k++] = a[i++];
        while (j < hi)  tmp[k++] = a[j++];
        System.arraycopy(tmp, 0, a, lo, tmp.length);
        System.out.printf("%smerged [%d,%d): %s%n", "  ".repeat(depth), lo, hi, Arrays.toString(tmp));
    }

    // ------------------------------------------------------------------
    // EXAMPLE 2: lower/upper bound on an array with duplicates.
    // ------------------------------------------------------------------
    static void example2_boundaries() {
        System.out.println("=== Example 2: binary search boundaries ===");
        int[] a = {1, 2, 4, 4, 4, 4, 7, 9};
        int target = 4;
        int first = lowerBound(a, target);                   // first index with a[i] >= 4
        int last = lowerBound(a, target + 1) - 1;            // last index with a[i] <= 4
        System.out.println("array : " + Arrays.toString(a));
        System.out.printf("target %d occupies indices [%d, %d] (count=%d)%n",
                target, first, last, last - first + 1);
        System.out.println("Trick: 'last occurrence' = lowerBound(target+1) - 1.\n");
    }

    static int lowerBound(int[] a, int target) {             // first i with a[i] >= target
        int lo = 0, hi = a.length;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] >= target) hi = mid;                  // mid might be answer — keep
            else lo = mid + 1;                               // mid ruled out — skip
        }
        return lo;
    }

    // ------------------------------------------------------------------
    // EXAMPLE 3: QuickSelect — 3rd largest in O(n) average, no full sort.
    // ------------------------------------------------------------------
    static void example3_quickSelect() {
        System.out.println("=== Example 3: QuickSelect (3rd largest) ===");
        int[] a = {3, 2, 1, 5, 6, 4, 9, 7};
        System.out.println("input: " + Arrays.toString(a));
        int k = 3;
        int lo = 0, hi = a.length - 1, targetIdx = a.length - k;
        while (lo < hi) {
            int p = partition(a, lo, hi);
            System.out.printf("  pivot lands at %d -> %s%n", p, Arrays.toString(a));
            if (p == targetIdx) break;
            if (p < targetIdx) lo = p + 1; else hi = p - 1;
        }
        System.out.println("3rd largest = " + a[targetIdx]
                + "  (array only PARTIALLY sorted — that's the savings)\n");
    }

    static int partition(int[] a, int lo, int hi) {
        int pivot = a[hi], i = lo;
        for (int j = lo; j < hi; j++)
            if (a[j] <= pivot) { int t = a[i]; a[i] = a[j]; a[j] = t; i++; }
        int t = a[i]; a[i] = a[hi]; a[hi] = t;
        return i;
    }

    // ------------------------------------------------------------------
    // EXAMPLE 4: Koko Eating Bananas — the answer (speed) is binary
    // searched; feasibility is monotonic: faster always still works.
    // ------------------------------------------------------------------
    static void example4_searchOnAnswer() {
        System.out.println("=== Example 4: binary search on the answer ===");
        int[] piles = {30, 11, 23, 4, 20};
        int hours = 6;
        System.out.println("piles=" + Arrays.toString(piles) + ", deadline=" + hours + "h");

        int lo = 1, hi = Arrays.stream(piles).max().getAsInt();
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            boolean ok = canFinish(piles, mid, hours);
            System.out.printf("  try speed %2d -> %s%n", mid, ok ? "feasible, try slower" : "too slow, go faster");
            if (ok) hi = mid; else lo = mid + 1;
        }
        System.out.println("minimum speed = " + lo + " bananas/hour");
        System.out.println("Pattern: 'minimize the max / min feasible value' -> BS on answer.");
    }

    static boolean canFinish(int[] piles, int speed, int h) {
        long hoursNeeded = 0;
        for (int p : piles) hoursNeeded += (p + speed - 1) / speed;   // ceil
        return hoursNeeded <= h;
    }
}
