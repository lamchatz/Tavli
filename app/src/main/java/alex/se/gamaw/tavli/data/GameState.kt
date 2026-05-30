package alex.se.gamaw.tavli.data

import alex.se.gamaw.tavli.data.enums.CollectionIndex
import alex.se.gamaw.tavli.data.enums.Direction
import alex.se.gamaw.tavli.data.enums.TOTAL_PIECES
import androidx.compose.ui.graphics.Color
import kotlin.math.abs

data class GameState(
    val piecesByPosition: Map<Int, List<Piece>> = emptyMap(),
    val selectedPoint: Int? = null,
    val allowedMoves: Set<Int> = emptySet(),
    val currentPlayer: Player? = null,
    val dice: List<Die> = listOf(Die(4), Die(3)),
    val movePool: List<Int> = emptyList(),
    val showRight: Boolean = false,
    val completed: Boolean = false
) {
    fun areDiceDouble(): Boolean {
        return dice[0].value == dice[1].value
    }

    fun calculateStateAfterMove(
        from: Int,
        to: Int,
        piecesByPosition: Map<Int, List<Piece>>
    ): GameState {
        val toList = piecesByPosition[to].orEmpty()
        if (toList.isEmpty()) {
            return this.copy()
        }

        val direction = Direction.get(toList.last().color == Color.White)
        val currentPool = movePool
        val currentDice = dice
        val doubleDice = areDiceDouble()
        val move = abs(to - from)
        val movesSum = currentPool.sumOf { it }

        var nextPool = currentPool
        var nextDice = currentDice


        if (doubleDice) {
            val d1 = currentDice[0].value
            val diceUsed = move / d1
            nextPool = currentPool.drop(diceUsed)

            nextDice = currentDice.mapIndexed { index, die ->
                when {
                    index == 0 && nextPool.size < 3 -> die.copy(played = true)
                    index == 1 && nextPool.isEmpty() -> die.copy(played = true)
                    else -> die
                }
            }
        } else {
            if (move == movesSum) {
                nextDice = currentDice.map { it.copy(played = true) }
                nextPool = emptyList()
            } else {
                val dieIndexToPlay = currentDice.indexOfFirst { it.value == move && !it.played }

                if (dieIndexToPlay != -1) {
                    nextDice = currentDice.mapIndexed { index, die ->
                        if (index == dieIndexToPlay) die.copy(played = true) else die
                    }
                    nextPool = currentPool.toMutableList().apply { remove(move) }
                }
            }
        }

        return this.copy(
            piecesByPosition = piecesByPosition,
            movePool = nextPool,
            dice = nextDice,
            completed = gameComplete()
        )
    }

    fun isNotMyTurn(selectedPiece: Piece): Boolean {
        return currentPlayer?.color != selectedPiece.color
    }

    fun roundCompleted(): Boolean {
        return movePool.isEmpty()
    }

    fun gameComplete(): Boolean {
        val color = currentPlayer?.color ?: return false

        val collectionIndex = CollectionIndex.get(color)
        return piecesByPosition[collectionIndex].orEmpty().size == TOTAL_PIECES
    }
}
