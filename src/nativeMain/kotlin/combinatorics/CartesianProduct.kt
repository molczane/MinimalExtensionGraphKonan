package combinatorics

/**
 * Enumerate all m-length sequences built from elements of `items`, lazily.
 * Equivalent to the Cartesian product items^m, allowing repetitions and order matters.
 *
 * @param items source set/list (size k)
 * @param m length of sequences
 * @return Sequence of lists of length m
 *
 * Edge cases:
 * - m == 0 -> yields one empty sequence
 * - items.isEmpty() && m > 0 -> yields nothing
 */
fun <T> productSequences(items: List<T>, m: Int): Sequence<List<T>> = sequence {
  require(m >= 0) { "m must be non-negative" }
  val k = items.size

  if (m == 0) {
    yield(emptyList())
    return@sequence
  }
  if (k == 0) {
    // No sequences possible if we have no items and need positive length
    return@sequence
  }

  // We treat positions as a base-k counter: digits[0..m-1]
  val digits = IntArray(m) { 0 }

  while (true) {
    // Map current digits to items
    val seq = List(m) { pos -> items[digits[pos]] }
    yield(seq)

    // Increment base-k counter (least significant digit at pos 0)
    var i = 0
    while (i < m) {
      if (digits[i] + 1 < k) {
        digits[i] += 1
        // lower positions already emitted; stop carry propagation
        break
      } else {
        digits[i] = 0
        i += 1
      }
    }
    if (i == m) {
      // overflow -> finished
      break
    }
  }
}

/** Example usage */
fun main() {
  val items = listOf('A', 'B')
  val m = 3
  // Expected 2^3 = 8 sequences: AAA, AAB, ABA, ABB, BAA, BAB, BBA, BBB
  for (seq in productSequences(items, m)) {
    println(seq)
  }
}
