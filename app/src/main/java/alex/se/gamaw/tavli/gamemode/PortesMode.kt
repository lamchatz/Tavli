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
        boardState: Map<Int, List<Piece>>,
        selectedPiece: Piece,
        movePool: List<Int>
    ): Set<Int> {
        val myColor = selectedPiece.color
        val barIndex = if (myColor == Color.White) BarIndex.WHITE.value else BarIndex.BLACK.value

        val position = selectedPiece.position
        if (boardState[barIndex].orEmpty().isNotEmpty() && position != barIndex) {
            return emptySet()
        }

        val possibleRange = when {
            position == BarIndex.WHITE.value -> {
                movePool.map { BarIndex.WHITE.value - it }
            }

            position == BarIndex.BLACK.value -> {
                movePool.map { BarIndex.BLACK.value + it }
            }

            myColor == Color.White -> {
                movePool.map { position - it }
            }

            else -> {
                movePool.map { position + it }
            }
        }

        val singleMoves = possibleRange.filterTo(mutableSetOf()) { targetIndex ->
            isLegalMove(boardState[targetIndex].orEmpty(), myColor)
        }

        return singleMoves + getCombinationMoves(movePool, myColor, position, boardState)
    }

    private fun getCombinationMoves(
        movePool: List<Int>,
        myColor: Color,
        position: Int,
        boardState: Map<Int, List<Piece>>,
    ): Set<Int> {
        if (movePool.size < 2) return emptySet()

        val direction = if (myColor == Color.White) -1 else 1
        val combinedMoves = mutableSetOf<Int>()

        fun explore(currentPos: Int, remainingDice: List<Int>, stepsTaken: Int) {
            // If we've moved 2 or more steps combined, the final landing spot is valid
            if (stepsTaken >= 2) {
                combinedMoves.add(currentPos)
            }

            // Try using each available move as the next step
            // Using distinct() ensures we don't recalculate identical dice (doubles)
            val uniqueDice = remainingDice.distinct()
            for (die in uniqueDice) {
                val nextPos = currentPos + (die * direction)

                // If the landing spot is legal, continue moving down this path
                if (isLegalMove(boardState[nextPos].orEmpty(), myColor)) {
                    val nextRemaining = remainingDice.toMutableList().apply { remove(die) }
                    explore(nextPos, nextRemaining, stepsTaken + 1)
                }
            }
        }

        explore(position, movePool, 0)
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
        movePool: List<Int>
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
            for (move in movePool) {
                val targetIndex = pos + (direction * move)

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
        movePool: List<Int>,
    ): Map<Int, List<Piece>> {
        val movesLeft = movePool.size
        if (movesLeft == 0) return boardState

        val fromList = boardState[from].orEmpty()
        if (fromList.isEmpty()) return boardState

        val color = fromList.last().color
        var chosenIntermediate = to

        val direction = if (color == Color.White) -1 else 1

        if (movesLeft == 2) {
            if (abs(to - from) == movePool.sumOf { it }) {
                // check if an opponent's piece can be hit
                for (move in movePool) {
                    val s = from + (move * direction)
                    if (hitsOpponent(boardState[s].orEmpty(), color)) {
                        chosenIntermediate = s
                        break
                    }
                }

                if (chosenIntermediate != to) {
                    val intermediateState = resolveSingleMove(boardState, from, chosenIntermediate)
                    return resolveSingleMove(intermediateState, chosenIntermediate, to)
                }
                // if not found, just go to the original selected position, no worries
            }
        } else if (movesLeft > 2) {
            val steps = mutableSetOf<Int>()
            var step = from
            for (move in movePool) {
                step += (move * direction)
                if (step > to) break
                if (hitsOpponent(boardState[step].orEmpty(), color)) {
                    steps.add(step)
                }
            }

            if (steps.isEmpty()) {
                return resolveSingleMove(boardState, from, to)
            }

            var startingPosition = from
            var intermediateState = boardState
            for (step in steps) {
                intermediateState = resolveSingleMove(intermediateState, startingPosition, step)
                startingPosition = step
            }

            if (startingPosition == to) {
                return intermediateState
            }
            return resolveSingleMove(intermediateState, steps.last(), to)
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

    private fun hitsOpponent(positionToGo: List<Piece>, color: Color): Boolean {
        if (positionToGo.isNotEmpty() && positionToGo.size == 1) {
            return color != positionToGo.last().color
        }
        return false
    }
}