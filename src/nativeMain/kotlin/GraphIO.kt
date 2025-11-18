@file:OptIn(ExperimentalForeignApi::class)

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import kotlinx.cinterop.CPointer
import platform.posix.FILE
import platform.posix.fclose
import platform.posix.ferror
import platform.posix.fgets
import platform.posix.fopen

/**
 * Czyta plik tekstowy za pomocą POSIX i zwraca jego zawartość jako String.
 * Brak zależności od `java.*`, działa w Kotlin/Native.
 */
private fun readAllTextPosix(path: String): String {
  val file: CPointer<FILE>? = fopen(path, "r")
  if (file == null) {
    throw IllegalArgumentException("Cannot open file: '$path'")
  }
  try {
    val sb = StringBuilder()
    memScoped {
      val bufSize = 4096
      val buffer = allocArray<ByteVar>(bufSize)
      while (true) {
        val res = fgets(buffer, bufSize, file) ?: break
        sb.append(res.toKString())
      }
    }
    // prosty check błędu odczytu
    if (ferror(file) != 0) {
      throw IllegalStateException("I/O error while reading: '$path'")
    }
    return sb.toString()
  } finally {
    fclose(file)
  }
}

/**
 * Wczytuje dwa grafy zapisane jako dwie macierze sąsiedztwa w jednym pliku tekstowym.
 * Format (linia po linii, puste linie ignorowane):
 *  n1
 *  row1 (n1 ints)
 *  ...
 *  row_n1
 *  n2
 *  row1 (n2 ints)
 *  ...
 *  row_n2
 *
 * Zwraca Pair(matrix1, matrix2) gdzie każda macierz to Array<Array<Int>>.
 * Rzuca IllegalArgumentException w przypadku błędnego formatu.
 */
fun readTwoAdjacencyMatrices(path: String): Pair<Array<Array<Int>>, Array<Array<Int>>> {
  val content = readAllTextPosix(path)
  val lines = content.lineSequence()
    .map { it.trim() }
    .filter { it.isNotEmpty() }
    .toList()
  val it = lines.iterator()

  fun nextLineOrError(reason: String = "Unexpected end of file"): String {
    if (!it.hasNext()) throw IllegalArgumentException(reason)
    return it.next()
  }

  fun readMatrix(n: Int): Array<Array<Int>> {
    val mat = Array(n) { Array(n) { 0 } }
    for (i in 0 until n) {
      val rowLine = nextLineOrError("Unexpected end of file while reading matrix rows (expected $n rows)")
      val parts = rowLine.split(Regex("\\s+")).filter { it.isNotEmpty() }
      if (parts.size != n) {
        throw IllegalArgumentException("Expected $n elements in row $i but got ${parts.size}: '$rowLine'")
      }
      for (j in 0 until n) {
        val v = parts[j].toIntOrNull()
          ?: throw IllegalArgumentException("Invalid integer at row $i col $j: '${parts[j]}'")
        mat[i][j] = v
      }
    }
    return mat
  }

  val n1Line = nextLineOrError("Missing size for first graph")
  val n1 = n1Line.toIntOrNull()
    ?: throw IllegalArgumentException("Invalid integer for number of vertices (first graph): '$n1Line'")
  if (n1 < 0) throw IllegalArgumentException("Number of vertices must be non-negative: $n1")

  val m1 = readMatrix(n1)

  val n2Line = nextLineOrError("Missing size for second graph")
  val n2 = n2Line.toIntOrNull()
    ?: throw IllegalArgumentException("Invalid integer for number of vertices (second graph): '$n2Line'")
  if (n2 < 0) throw IllegalArgumentException("Number of vertices must be non-negative: $n2")

  val m2 = readMatrix(n2)

  return Pair(m1, m2)
}