package combinatorics

/**
 * Generates all k-sized subsets (combinations) of the input list iteratively, in lexicographic order by index.
 *
 * @param items the source list
 * @param k desired subset size
 * @return sequence of lists representing each k-combination
 *
 * Edge cases:
 * - k == 0 -> yields one empty list
 * - k > items.size -> yields nothing
 */
fun <T> combinationsK(items: List<T>, k: Int): Sequence<List<T>> = sequence {
    val n = items.size
    require(k >= 0) { "k must be non-negative" }

    if (k == 0) {
        yield(emptyList())
        return@sequence
    }
    if (k > n) {
        // no combinations
        return@sequence
    }

    // Initial combination: [0, 1, 2, ..., k-1]
    val c = IntArray(k) { it }

    while (true) {
        // Emit current combination
        yield(List(k) { idx -> items[c[idx]] })

        // Generate next combination
        var i = k - 1
        // Find rightmost index we can increment
        while (i >= 0 && c[i] == n - k + i) {
            i--
        }
        if (i < 0) {
            // Finished
            break
        }
        // Increment c[i]
        c[i]++
        // Reset tail to strictly increasing sequence
        for (j in i + 1 until k) {
            c[j] = c[j - 1] + 1
        }
    }
}


