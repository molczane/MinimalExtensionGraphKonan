package combinatorics

/**
 * Generates all permutations of the input list as a lazy Sequence.
 *
 * - Uses Heap's algorithm (in-place) which minimizes swaps.
 * - Returns permutations in a specific order defined by Heap's algorithm.
 * - Works for any element type T.
 *
 * Edge cases:
 * - Empty list -> yields one empty permutation
 * - Single element -> yields the element itself
 */
fun <T> permutations(items: List<T>): Sequence<List<T>> = sequence {
    val n = items.size
    if (n <= 1) {
        // Yield items as-is (including empty list)
        yield(items.toList())
        return@sequence
    }

    // Work on a mutable copy in-place
    val a = items.toMutableList()

    // c[i] is the stack of rotations for index i
    val c = IntArray(n) { 0 }

    // Emit the initial permutation
    yield(a.toList())

    var i = 0
    while (i < n) {
        if (c[i] < i) {
            // If i is even, swap a[0] and a[i]; else swap a[c[i]] and a[i]
            val swapIdx = if (i % 2 == 0) 0 else c[i]
            a.swap(swapIdx, i)

            // Emit the next permutation
            yield(a.toList())

            c[i] += 1
            i = 0
        } else {
            c[i] = 0
            i += 1
        }
    }
}

/** Small helper to swap two elements in a MutableList<T>. */
private fun <T> MutableList<T>.swap(i: Int, j: Int) {
    if (i == j) return
    val tmp = this[i]
    this[i] = this[j]
    this[j] = tmp
}