package alex.se.gamaw.tavli.gamemode

import alex.se.gamaw.tavli.data.BarIndex
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
            Piece(20, 16, Color.Black),
            Piece(21, 16, Color.Black),
            Piece(22, 16, Color.Black),
            Piece(23, 11, Color.Black),
            Piece(24, 11, Color.Black),
            Piece(25, 11, Color.Black),
            Piece(26, 11, Color.Black),
            Piece(27, 11, Color.Black),
            Piece(28, 0, Color.Black),
            Piece(29, 0, Color.Black),
        )
    }

    override fun getLegalMoves(
        selectedPiece: Piece,
        boardState: Map<Int, List<Piece>>
    ): Set<Int> {
        val myColor = selectedPiece.color
        val barIndex = if (myColor == Color.White) BarIndex.WHITE.value else BarIndex.BLACK.value

        if (boardState[barIndex].orEmpty().isNotEmpty() && selectedPiece.position != barIndex) {
            return emptySet()
        }

        val possibleRange = when {
            selectedPiece.position == BarIndex.WHITE.value -> (18..23)
            selectedPiece.position == BarIndex.BLACK.value -> (0..5)
            myColor == Color.White -> (selectedPiece.position - 1 downTo 0)
            else -> (selectedPiece.position + 1..23)
        }

        return possibleRange.filterTo(mutableSetOf()) { targetIndex ->
            isLegalMove(boardState[targetIndex].orEmpty(), myColor)
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
        val barIndex = if (color == Color.White) BarIndex.WHITE.value else BarIndex.BLACK.value
        val piecesOnBar = boardState[barIndex].orEmpty()

        val startingPositions = if (piecesOnBar.isNotEmpty()) {
            listOf(barIndex)
        } else {
            boardState.filterValues { stack ->
                stack.lastOrNull()?.color == color
            }.keys
        }

        val moveFactor = if (color == Color.White) -1 else 1

        for (pos in startingPositions) {
            for (dice in dices) {
                val targetIndex = pos + (moveFactor * dice)

                if (targetIndex in 0..23) {
                    if (isLegalMove(boardState[targetIndex].orEmpty(), color)) {
                        return true
                    }
                }
            }
        }

        return false
    }

    override fun resolveMove(
        boardState: Map<Int, List<Piece>>,
        from: Int,
        to: Int
    ): Map<Int, List<Piece>> {
        val fromList = boardState[from].orEmpty()
        if (fromList.isEmpty()) return boardState

        val toList = boardState[to].orEmpty()
        val pieceToMove = fromList.last().copy(position = to)

        if (!isLegalMove(boardState[from].orEmpty(), pieceToMove.color)) return boardState

        if (toList.isEmpty()) {
            return boardState + mapOf(
                from to fromList.dropLast(1),
                to to toList + pieceToMove
            )
        }

        val pieceToLand = toList.last()
        val barIndex =
            if (Color.White == pieceToMove.color) BarIndex.BLACK.value else BarIndex.WHITE.value

        if (pieceToLand.color != pieceToMove.color) {
            return boardState + mapOf(
                from to fromList.dropLast(1),
                to to toList.dropLast(1) + pieceToMove,
                barIndex to boardState[barIndex].orEmpty() + pieceToLand.copy(position = barIndex)
            )
        }

        return boardState + mapOf(
            from to fromList.dropLast(1),
            to to toList + pieceToMove
        )
    }
}