package alex.se.gamaw.tavli.gamemode

import alex.se.gamaw.tavli.data.BarIndex
import alex.se.gamaw.tavli.data.Die
import alex.se.gamaw.tavli.data.Piece
import androidx.compose.ui.graphics.Color
import kotlin.math.abs

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
        boardState: Map<Int, List<Piece>>,
        dice: List<Die>
    ): Set<Int> {
        val myColor = selectedPiece.color
        val barIndex = if (myColor == Color.White) BarIndex.WHITE.value else BarIndex.BLACK.value

        val position = selectedPiece.position
        if (boardState[barIndex].orEmpty().isNotEmpty() && position != barIndex) {
            return emptySet()
        }

        val availableDiceValues = dice.filter { !it.played }.map { it.value }

        val possibleRange = when {
            position == BarIndex.WHITE.value -> {
                availableDiceValues.map { BarIndex.WHITE.value - it }
            }

            position == BarIndex.BLACK.value -> {
                availableDiceValues.map { BarIndex.BLACK.value + it }
            }

            myColor == Color.White -> {
                availableDiceValues.map { position - it }
            }

            else -> {
                availableDiceValues.map { position + it }
            }
        }

        val singleMoves = possibleRange.filterTo(mutableSetOf()) { targetIndex ->
            isLegalMove(boardState[targetIndex].orEmpty(), myColor)
        }

        return singleMoves + getCombinationMoves(availableDiceValues, myColor, position, boardState)
    }

    private fun getCombinationMoves(
        availableDiceValues: List<Int>,
        myColor: Color,
        position: Int,
        boardState: Map<Int, List<Piece>>,
    ): Set<Int> {
        val combinedMoves = mutableSetOf<Int>()
        if (availableDiceValues.size == 2) {
            val direction = if (myColor == Color.White) -1 else 1

            val d1 = availableDiceValues[0]
            val d2 = availableDiceValues[1]
            val intermediate1 = position + (d1 * direction)
            val intermediate2 = position + (d2 * direction)
            val finalTarget = position + (d1 + d2) * direction
            val path1Valid = isLegalMove(boardState[intermediate1].orEmpty(), myColor) &&
                    isLegalMove(boardState[finalTarget].orEmpty(), myColor)

            val path2Valid = isLegalMove(boardState[intermediate2].orEmpty(), myColor) &&
                    isLegalMove(boardState[finalTarget].orEmpty(), myColor)

            if (path1Valid || path2Valid) {
                combinedMoves.add(finalTarget)
            }
        }

        return combinedMoves
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
        dice: List<Die>
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

        val direction = if (color == Color.White) -1 else 1

        for (pos in startingPositions) {
            for (dice in dice) {
                val targetIndex = pos + (direction * dice.value)

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
        to: Int,
        dice: List<Die>
    ): Map<Int, List<Piece>> {
        val fromList = boardState[from].orEmpty()
        if (fromList.isEmpty()) return boardState

        val color = fromList.last().color
        var chosenIntermediate = to

        if (abs(to - from) > 6) {
            val direction = if (color == Color.White) -1 else 1

            val intermediate0 = boardState[from + (dice[0].value * direction)].orEmpty()
            val intermediate1 = boardState[from + (dice[1].value * direction)].orEmpty()

            if (intermediate0.isNotEmpty() && intermediate0.size == 1) {
                val c = intermediate0.last().color
                if (color != c) {
                    chosenIntermediate = from + (dice[0].value * direction)
                }

            } else if (intermediate1.isNotEmpty() && intermediate1.size == 1) {
                val c = intermediate1.last().color
                if (color != c) {
                    chosenIntermediate = from + (dice[1].value * direction)
                }
            }

            if (chosenIntermediate != to) {
                val intermediateState = resolveSingleMove(boardState, from, chosenIntermediate)
                return resolveSingleMove(intermediateState, chosenIntermediate, to)
            }
        }

        return resolveSingleMove(boardState, from, to)
    }

    private fun resolveSingleMove(
        boardState: Map<Int, List<Piece>>,
        from: Int,
        to: Int
    ): Map<Int, List<Piece>> {
        val fromList = boardState[from].orEmpty()
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