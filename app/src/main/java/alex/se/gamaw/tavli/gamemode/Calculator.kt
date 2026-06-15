package alex.se.gamaw.tavli.gamemode

import alex.se.gamaw.tavli.data.enums.CollectionIndex
import alex.se.gamaw.tavli.data.enums.Direction

class Calculator {
    companion object{
        fun calculateTarget(start: Int, plus: Int, isWhite: Boolean): Int {
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

        fun inBounds(target: Int): Boolean =
            target in 0..23
    }
}