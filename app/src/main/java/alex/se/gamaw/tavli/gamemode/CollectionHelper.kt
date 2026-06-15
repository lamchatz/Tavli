package alex.se.gamaw.tavli.gamemode

import alex.se.gamaw.tavli.data.Piece
import alex.se.gamaw.tavli.data.enums.CollectionIndex
import alex.se.gamaw.tavli.data.enums.HOME_BASES
import alex.se.gamaw.tavli.data.enums.TOTAL_PIECES
import androidx.compose.ui.graphics.Color
import kotlin.collections.isNotEmpty
import kotlin.collections.last
import kotlin.collections.orEmpty

class CollectionHelper {
    companion object {
        const val WRONG_VALUE = -100
        val WRONG_LIST = listOf(WRONG_VALUE)

        fun canCollect(
            boardState: Map<Int, List<Piece>>,
            movePool: List<Int>,
            from: Int,
            color: Color
        ): Boolean {
            if (boardState[from].orEmpty().isEmpty()) return false

            if (isNotGathering(boardState, color)) return false

            if (movePool.isEmpty()) return false

            val actualValue = actualValue(from, color)
            if (movePool.contains(actualValue)) {
                return true
            }

            val maxIndex = maxIndex(boardState, color)
            if (from == maxIndex) {
                val maxMove = movePool.max()

                if (actualValue(maxIndex, color) <= maxMove) {
                    return true
                }
            }

            return false
        }

        fun resolveMove(
            boardState: Map<Int, List<Piece>>,
            movePool: List<Int>,
            from: Int
        ): List<Int> {
            val fromList = boardState[from] ?: return emptyList()

            if (fromList.isEmpty()) return WRONG_LIST

            if (movePool.isEmpty()) return WRONG_LIST

            val color = fromList.last().color

            val actualValue = actualValue(from, color)
            if (movePool.contains(actualValue)) {
                return movePool - actualValue
            }

            val maxIndex = maxIndex(boardState, color)
            if (from == maxIndex) {
                val maxMove = movePool.max()

                if (actualValue(maxIndex, color) <= maxMove) {
                    return movePool - maxMove
                }
            }
            return WRONG_LIST
        }

        private fun isNotGathering(boardState: Map<Int, List<Piece>>, color: Color): Boolean {
            val range = HOME_BASES[color] ?: return false

            val homeSum = range.sumOf { index ->
                val stack = boardState[index].orEmpty()
                if (stack.isNotEmpty() && stack.last().color == color) {
                    stack.size
                } else {
                    0
                }
            }

            return homeSum + boardState[CollectionIndex.get(color)].orEmpty().size != TOTAL_PIECES
        }

        private fun actualValue(position: Int, color: Color): Int {
            return if (Color.White == color) {
                position + 1
            } else {
                24 - position
            }
        }

        private fun maxIndex(boardState: Map<Int, List<Piece>>, color: Color): Int {
            val range = HOME_BASES[color] ?: return WRONG_VALUE

            return range.firstOrNull { index ->
                val stack = boardState[index].orEmpty()
                stack.isNotEmpty() && stack.last().color == color
            } ?: WRONG_VALUE
        }
    }
}