package alex.se.gamaw.tavli.gamemode

import alex.se.gamaw.tavli.data.enums.BarIndex
import alex.se.gamaw.tavli.data.enums.CollectionIndex
import alex.se.gamaw.tavli.data.Piece
import alex.se.gamaw.tavli.data.enums.Direction
import alex.se.gamaw.tavli.data.enums.TOTAL_PIECES
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
        val barIndex = BarIndex.get(myColor)

        val position = selectedPiece.position
        if (boardState[barIndex].orEmpty().isNotEmpty() && position != barIndex) {
            return emptySet()
        }

        val possibleRange = when {
            position == BarIndex.WHITE.value -> {
                movePool.map { BarIndex.WHITE.value - it }
            }

            myColor == Color.White -> {
                movePool.map { calculateTarget(position, it, true)}
            }

            position == BarIndex.BLACK.value -> {
                movePool.map { BarIndex.BLACK.value + it }
            }

            else -> {
                movePool.map { calculateTarget(position, it, false) }
            }
        }

        val singleMoves = possibleRange.distinct().filterTo(mutableSetOf()) { targetIndex ->
            isLegalMove(targetIndex, boardState, myColor)
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

        val isWhite = myColor == Color.White
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
//                val nextPos = currentPos + (die * direction)
                val nextPos = calculateTarget(currentPos, die, isWhite)

                // If the landing spot is legal, continue moving down this path
                if (isLegalMove(nextPos, boardState, myColor)) {
                    val nextRemaining = remainingDice.toMutableList().apply { remove(die) }
                    explore(nextPos, nextRemaining, stepsTaken + 1)
                }
            }
        }

        explore(position, movePool, 0)
        return combinedMoves
    }

    // A move is legal if:
    // - Target is in bounds
    // - Target is the collection point and can collect
    // - The square is empty
    // - OR it's your own color
    // - OR it's an opponent's only 1 piece
    override fun isLegalMove(
        target: Int,
        boardState: Map<Int, List<Piece>>,
        color: Color
    ): Boolean {
        if (!inBounds(target)) return false

        if (target == CollectionIndex.get(color)) {
            return canCollect(boardState, color)
        }

        val stackAtTarget = boardState[target].orEmpty()

        return stackAtTarget.isEmpty() ||
                color == stackAtTarget.last().color ||
                stackAtTarget.size == 1
    }

    private fun inBounds(target: Int): Boolean =
        target in 0..23 || (target == CollectionIndex.WHITE.value || target == CollectionIndex.BLACK.value)

    override fun hasLegalMove(
        boardState: Map<Int, List<Piece>>,
        color: Color,
        movePool: List<Int>
    ): Boolean {
        val isWhite = color == Color.White
        val barIndex = BarIndex.get(color)
        val piecesOnBar = boardState[barIndex].orEmpty()

        val startingPositions = if (piecesOnBar.isNotEmpty()) {
            listOf(barIndex)
        } else {
            boardState.filterValues { stack ->
                stack.lastOrNull()?.color == color
            }.keys
        }

        for (pos in startingPositions) {
            for (move in movePool) {
//                val targetIndex = pos + (direction * move)
                val targetIndex = calculateTarget(pos, move, isWhite)
                if (isLegalMove(targetIndex, boardState, color)) {
                    return true
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

        val isWhite = color == Color.White

        if (movesLeft == 2) {
            if (abs(to - from) == movePool.sumOf { it }) {
                // check if an opponent's piece can be hit
                for (move in movePool) {
//                    val s = from + (move * direction)
                    val s = calculateTarget(from, move, isWhite)
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
//                step += (move * direction)
                step = calculateTarget(step, move, isWhite)
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

        if (!isLegalMove(to, boardState, pieceToMove.color)) return boardState

        if (toList.isEmpty()) {
            return boardState + mapOf(
                from to fromList.dropLast(1),
                to to toList + pieceToMove
            )
        }

        val pieceToLand = toList.last()
        val barIndex = BarIndex.get(pieceToLand.color)

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

    private fun canCollect(piecesByPosition: Map<Int, List<Piece>>, color: Color): Boolean {
        val isWhite = Color.White == color
        val range = if (isWhite) 0..5 else 18..23
        val collectionIndex = CollectionIndex.get(color)
        var sum = 0
        for (i in range) {
            sum += piecesByPosition[i].orEmpty().count { piece -> piece.color == color }
        }

        return sum + piecesByPosition[collectionIndex].orEmpty().size == TOTAL_PIECES
    }

    private fun calculateTarget(start: Int, plus: Int, isWhite: Boolean): Int {
        val direction = Direction.get(isWhite)
        val target = start + (direction * plus)

        return if (isWhite) {
            if (target < 0) {
                CollectionIndex.WHITE.value
            } else {
                target
            }
        } else {
            if (target > 23) {
                CollectionIndex.BLACK.value
            } else {
                target
            }
        }
    }
}