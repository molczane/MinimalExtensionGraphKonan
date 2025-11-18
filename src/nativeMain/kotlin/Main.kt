import combinatorics.combinationsK
import combinatorics.permutations
import combinatorics.productSequences
import kotlin.math.max
import kotlin.system.measureTimeMillis
import kotlin.time.measureTime

/** Example usage */
fun main() {
  val (bigGraph, smallGraph)
          = readTwoAdjacencyMatrices("/Users/ernestmolczan/IdeaProjects/MinimalExtensionGraph/src/sample_graphs1.txt")

  val n = bigGraph.size
  val bigGraphVertices = (0 until n).toList()
  val k = smallGraph.size
  val smallGraphVertices = (0 until k).toList()
  val m = 1

  val elapsedTime = measureTime {

    // Generate all k-combinations of bigGraph vertices
    val combinations = combinationsK(bigGraphVertices, k)
    val indexToSubset = mutableMapOf<Int, List<Int>>()
    combinations.forEach {
      indexToSubset[indexToSubset.size] = it
    }
    println(indexToSubset.size)

    // Generate all permutations of smallGraph vertices
    val permutations = permutations(smallGraphVertices)
    val indexToPermutation = mutableMapOf<Int, List<Int>>()
    permutations.forEach {
      indexToPermutation[indexToPermutation.size] = it
    }
    println(indexToPermutation.size)

    // Create a 2D matrix to hold missing edges for each (permutation, combination) pair
    val missingEdgesMatrix: Array<Array<MutableList<Pair<Int, Int>>>> = Array(indexToPermutation.size) {
      Array(indexToSubset.size) {
        mutableListOf()
      }
    }

    // Now let's iterate over all combinations subsets and permutations and fill the missingEdgesMatrix
    indexToSubset.forEach { subsetJ ->
      indexToPermutation.forEach { permutationI ->

        // Create the trimmed big graph adjacency matrix for the current combination
        val trimmedBigMatrix = Array(k) { Array(k) { 0 } }
        for (row in 0 until k) {
          for (col in 0 until k) {
            trimmedBigMatrix[row][col] = bigGraph[subsetJ.value[row]][subsetJ.value[col]]
          }
        }

        // Create the permuted small graph adjacency matrix for the current permutation
        val permutedSmallMatrix = Array(k) { Array(k) { 0 } }
        for (row in 0 until k) {
          for (col in 0 until k) {
            permutedSmallMatrix[row][col] = smallGraph[permutationI.value[row]][permutationI.value[col]]
          }
        }

        // Create matrix of number of missing edges for each cell
        val missingEdges = Array(k) { Array(k) { 0 } }
        for (row in 0 until k) {
          for (col in 0 until k) {
            // Calculate missing edges, no negative values
            missingEdges[row][col] = max(0, permutedSmallMatrix[row][col] - trimmedBigMatrix[row][col])
            if (missingEdges[row][col] > 0) {
              for (times in 0 until missingEdges[row][col]) {
                missingEdgesMatrix[permutationI.key][subsetJ.key].add(Pair(subsetJ.value[row], subsetJ.value[col]))
              }
            }
          }
        }
      }
    }

    // Now let's extract minimal list of missing edges from missingEdgesMatrix
    var minimalListOfMissingEdges = missingEdgesMatrix[0][0]
    var permutation: List<Int>? = indexToPermutation[0]
    var combination: List<Int>? = indexToSubset[0]

    for (row in 0 until indexToPermutation.size) {
      for (col in 0 until indexToSubset.size) {
        if (missingEdgesMatrix[row][col].size < minimalListOfMissingEdges.size) {
          minimalListOfMissingEdges = missingEdgesMatrix[row][col]
          permutation = indexToPermutation[row]
          combination = indexToSubset[col]
        }
      }
    }

    println("Minimal number of missing edges: ${minimalListOfMissingEdges.size}")
    println("Missing edges (bigGraph vertex indices): $minimalListOfMissingEdges")
    println("Subset of bigGraph vertices: $combination")
    println("Permutation of smallGraph vertices: $permutation")

    // It seems to work correctly

    // Now, let's find minimal extension of bigGraph, that contains m copies of smallGraph
    // This is a much harder problem, we can try some greedy approach or backtracking

    // For now, let's just generate all m-combinations of bigGraph vertices subsets
    val mSizedCombinationsSequence = combinationsK(indexToSubset.keys.toList(), m)
    val indexToMSizedCombination = mutableMapOf<Int, List<Int>>()
    mSizedCombinationsSequence.forEach {
      indexToMSizedCombination[indexToMSizedCombination.size] = it
    }

    var minimalSetOfAddedEdges: Set<Pair<Int, Int>>? = null

    indexToMSizedCombination.forEach { mSizedCombinationOfSubsets ->
      productSequences(indexToPermutation.keys.toList(), m).forEach { mLengthSequence ->
        val subsetsNumbers = mSizedCombinationOfSubsets.value // columns in missingEdgesMatrix
        val permutationsNumbers = mLengthSequence // rows in missingEdgesMatrix
        val addedEdgesSet = mutableSetOf<Pair<Int, Int>>()
        for (i in 0 until m) {
          val subsetIndex = subsetsNumbers[i]
          val permutationIndex = permutationsNumbers[i]
          val missingEdgesForThisPair = missingEdgesMatrix[permutationIndex][subsetIndex]
          addedEdgesSet.addAll(missingEdgesForThisPair)
        }
        if (minimalSetOfAddedEdges == null || addedEdgesSet.size < minimalSetOfAddedEdges!!.size) {
          minimalSetOfAddedEdges = addedEdgesSet
        }
      }
    }
    println("Minimal set of added edges to get $m copies of smallGraph: ${minimalSetOfAddedEdges?.size}")
    println("Added edges (bigGraph vertex indices): $minimalSetOfAddedEdges")
  }.inWholeMilliseconds

    println("Elapsed time: $elapsedTime ms")
  // We can use first phase to test correctness of second phase
}