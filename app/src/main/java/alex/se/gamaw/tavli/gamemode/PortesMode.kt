package alex.se.gamaw.tavli.gamemode

import alex.se.gamaw.tavli.data.Move
import alex.se.gamaw.tavli.data.MoveResult
import alex.se.gamaw.tavli.data.Piece
import alex.se.gamaw.tavli.data.enums.BarIndex
import alex.se.gamaw.tavli.data.enums.CollectionIndex
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
                movePool.map { calculateTarget(position, it, true) }
            }

            position == BarIndex.BLACK.value -> {
                movePool.map { BarIndex.BLACK.value + it }
            }

            else -> {
                movePool.map { calculateTarget(position, it, false) }
            }
        }

        val singleMoves = possibleRange.distinct().filterTo(mutableSetOf()) { targetIndex ->
            isLegalMove(boardState, targetIndex, myColor)
        }

        return singleMoves + getCombinationMoves(
            boardState,
            position,
            myColor,
            movePool,
            singleMoves
        )
    }

    private fun getCombinationMoves(
        boardState: Map<Int, List<Piece>>,
        from: Int,
        myColor: Color,
        movePool: List<Int>,
        allowedMoves: Set<Int>,
    ): Set<Int> {
        val movesLeft = movePool.size
        if (movesLeft < 2) return emptySet()

        if (boardState[BarIndex.get(myColor)].orEmpty().size > 1) return emptySet()

        val isWhite = myColor == Color.White
        val combinedMoves = mutableSetOf<Int>()

        when (movesLeft) {
            2 -> {
                var target = calculateTarget(from, movePool[0], isWhite)
                var destination = calculateTarget(target, movePool[1], isWhite)

                if (allowedMoves.contains(target) && isLegalMove(
                        boardState,
                        calculateTarget(target, movePool[1], isWhite),
                        myColor
                    )
                ) {
                    combinedMoves.add(destination)
                }

                target = calculateTarget(from, movePool[1], isWhite)
                destination = calculateTarget(target, movePool[0], isWhite)

                if (allowedMoves.contains(target) && isLegalMove(
                        boardState,
                        destination,
                        myColor
                    )
                ) {
                    combinedMoves.add(destination)
                }
            }

            3, 4 -> {
                var start = from
                for (move in movePool) {
                    val target = calculateTarget(start, move, isWhite)
                    if (isLegalMove(boardState, target, myColor)) {
                        combinedMoves.add(target)
                    } else {
                        return combinedMoves
                    }

                    start = target
                }
            }
        }

        return combinedMoves
    }

    // A move is legal if:
    // - Target is in bounds
    // - Target is the collection point and can collect
    // - The square is empty
    // - OR it's your own color
    // - OR it's an opponent's only 1 piece
    override fun isLegalMove(
        boardState: Map<Int, List<Piece>>,
        target: Int,
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
                if (isLegalMove(boardState, targetIndex, color)) {
                    return true
                }
            }
        }

        return false
    }

    override fun resolveMove(
        move: Move
    ): MoveResult {
        var (boardState, from, to, movePool) = move

        val movesLeft = movePool.size
        if (movesLeft == 0) return MoveResult.noMove(move)

        val fromList = boardState[from].orEmpty()
        if (fromList.isEmpty()) return MoveResult.noMove(move)

        if (CollectionIndex.isCollectionPoint(to)) {
            //doSomething else

            return resolveSingleMove(move)
        }

        if (movesLeft == 1) {
            return resolveSingleMove(move)
        }

        val color = fromList.last().color
        val isWhite = color == Color.White
        val moveValue = abs(to - from)

        if (movePool.any { it == moveValue }) {
            return resolveSingleMove(move)
        }

        var intermediateMove = MoveResult(boardState, movePool)

        if (movePool.distinct().size <= 1) {
            do {
                val s = calculateTarget(
                    from,
                    movePool.drop(1).first(),
                    isWhite
                )
                intermediateMove = resolveSingleMove(Move(intermediateMove, from, s))
                from = s
            } while (s != to && movePool.isNotEmpty())

            return intermediateMove
        } else {
            if (moveValue == movePool.sum()) {
                for (step in movePool) {
                    val s = calculateTarget(from, step, isWhite)
                    if (hitsOpponent(boardState[s].orEmpty(), color)) {
                        intermediateMove = resolveSingleMove(Move(intermediateMove, from, s))

                        val moveLeft = calculateTarget(s, movePool.single { it != step }, isWhite)
                        return resolveSingleMove(Move(intermediateMove, s, moveLeft))
                    }
                }

                return resolveSingleMove(move).copy(moves = emptyList())
            }

            // possible collection point weirdness
        }

        return MoveResult.noMove(move)
    }

    fun resolveSingleMove(
        move: Move
    ): MoveResult {
        val (boardState, from, to, movePool) = move

        val fromList = boardState[from].orEmpty()
        if (fromList.isEmpty()) {
            return MoveResult.noMove(move)
        }

        val color = fromList.last().color

        if (!isLegalMove(boardState, to, color)) {
            return MoveResult.noMove(move)
        }

        val newFromList = fromList.drop(1)
        val newBoardState = if (newFromList.isEmpty()) {
            boardState - from
        } else {
            boardState + mapOf(from to newFromList)
        }

        if (CollectionIndex.isCollectionPoint(to)) {
            //handle here

            return MoveResult.noMove(boardState)
        }

        if (BarIndex.isBarIndexPoint(to)) {
            //handle here

            return MoveResult.noMove(boardState)
        }

        val move = abs(to - from)
        val pieceToMove = fromList.last().copy(position = to)

        val toList = boardState[to].orEmpty()
        if (toList.isEmpty() || toList.last().color == color) {
            return MoveResult(newBoardState + (to to toList + pieceToMove), movePool - move)
        }

        val pieceToLand = toList.last()
        val barIndex = BarIndex.get(pieceToLand.color)

        return MoveResult(
            newBoardState + mapOf(
                to to toList.dropLast(1) + pieceToMove,
                barIndex to newBoardState[barIndex].orEmpty() + pieceToLand.copy(position = barIndex)
            ), movePool - move
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