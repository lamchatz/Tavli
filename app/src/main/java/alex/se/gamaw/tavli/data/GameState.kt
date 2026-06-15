package alex.se.gamaw.tavli.data

import alex.se.gamaw.tavli.data.enums.CollectionIndex
import alex.se.gamaw.tavli.data.enums.TOTAL_PIECES

data class GameState(
    val boardState: Map<Int, List<Piece>> = emptyMap(),
    val selectedPoint: Int? = null,
    val allowedMoves: Set<Int> = emptySet(),
    val currentPlayer: Player,
    val dice: List<Die> = listOf(Die(4), Die(3)),
    val movePool: List<Int> = emptyList(),
    val showRight: Boolean = false,
    val completed: Boolean = false
) {
    fun areDiceDouble(): Boolean {
        return dice[0].value == dice[1].value
    }

    fun updateGameState(moveResult: MoveResult): GameState {
        val newMovePool = moveResult.moves
        val nextDice = if (areDiceDouble()) {
            when (newMovePool.size) {
                0 -> dice.map { it.copy(played = true) }
                1, 2 -> dice.mapIndexed { index, die ->
                    if (index == 0) die.copy(played = true) else die
                }

                else -> dice
            }
        } else {
            val remainingPool = newMovePool.toMutableList()

            dice.map { die ->
                if (die.played) {
                    die
                } else if (remainingPool.remove(die.value)) {
                    die
                } else {
                    die.copy(played = true)
                }
            }
        }

        val newBoardState = moveResult.boardState
        return this.copy(
            boardState = newBoardState,
            dice = nextDice,
            movePool = newMovePool,
            completed = gameComplete(newBoardState)
        )
    }

    fun isNotMyTurn(selectedPiece: Piece): Boolean {
        return currentPlayer.color != selectedPiece.color
    }

    fun roundCompleted(): Boolean {
        return movePool.isEmpty()
    }

    fun gameComplete(boardState: Map<Int, List<Piece>>): Boolean {
        val collectionIndex = CollectionIndex.get(currentPlayer.color)
        return boardState[collectionIndex].orEmpty().size == TOTAL_PIECES
    }
}
