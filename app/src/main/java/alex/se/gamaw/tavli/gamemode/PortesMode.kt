package alex.se.gamaw.tavli.gamemode

import alex.se.gamaw.tavli.data.Piece
import androidx.compose.ui.graphics.Color

class PortesMode() : GameMode {
    override fun initialBoard(): List<Piece> {
        return listOf(
            Piece(0, 5, Color.White),
            Piece(1, 5, Color.White),
            Piece(2, 5, Color.White),
            Piece(3, 5, Color.White),
            Piece(4, 5, Color.White),
            Piece(5, 7, Color.White),
            Piece(6, 7, Color.White),
            Piece(7, 7, Color.White),
            Piece(8, 12, Color.White),
            Piece(9, 12, Color.White),
            Piece(10, 12, Color.White),
            Piece(11, 12, Color.White),
            Piece(12, 12, Color.White),
            Piece(13, 23, Color.White),
            Piece(14, 23, Color.White),


            Piece(15, 18, Color.Black),
            Piece(16, 18, Color.Black),
            Piece(17, 18, Color.Black),
            Piece(18, 18, Color.Black),
            Piece(19, 18, Color.Black),
//
//            Piece(15, 18, Color.Black),
//            Piece(16, 18, Color.Black),
//            Piece(17, 18, Color.Black),
//            Piece(18, 18, Color.Black),
//            Piece(19, 18, Color.Black),
//            Piece(20, 16, Color.Black),
//            Piece(21, 16, Color.Black),
//            Piece(22, 16, Color.Black),
//            Piece(23, 11, Color.Black),
//            Piece(24, 11, Color.Black),
//            Piece(25, 11, Color.Black),
//            Piece(26, 11, Color.Black),
//            Piece(27, 11, Color.Black),
//            Piece(28, 0, Color.Black),
//            Piece(29, 0, Color.Black),
        )
    }

    override fun getLegalMoves(
        selectedPiece: Piece,
        boardState: Map<Int, List<Piece>>
    ): Set<Int> {
        val isWhite = selectedPiece.id < 15
        val possibleRange = if (isWhite) {
            (selectedPiece.position - 1 downTo 0)
        } else {
            (selectedPiece.position + 1..23)
        }

        return possibleRange.filterTo(mutableSetOf()) { targetIndex ->
            isLegalMove(boardState[targetIndex].orEmpty(), selectedPiece.color)
        }
    }


    // A move is legal if:
    // - The square is empty
    // - OR it's your own color
    // - OR it's an opponent's only 1 piece
    private fun isLegalMove(
        stackAtTarget: List<Piece>,
        myColor: Color
    ): Boolean = stackAtTarget.isEmpty() ||
            stackAtTarget.last().color == myColor ||
            stackAtTarget.size == 1

    override fun hasLegalMove(
        boardState: Map<Int, List<Piece>>,
        color: Color,
        dices: List<Int>
    ): Boolean {
        val moveFactor = if (color == Color.White) -1 else 1

        val playerPositions = boardState.filterValues { stack ->
            stack.lastOrNull()?.color == color
        }

        for ((pos, _) in playerPositions) {
            for (dice in dices) {
                val targetIndex = pos + (moveFactor * dice)

                if (targetIndex in 0..23) {
                    val targetStack = boardState[targetIndex].orEmpty()

                    if (isLegalMove(targetStack, color)) {
                        return true
                    }
                }
            }
        }

        return false
    }
}