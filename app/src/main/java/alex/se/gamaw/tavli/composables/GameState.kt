package alex.se.gamaw.tavli.composables

import alex.se.gamaw.tavli.data.Die
import alex.se.gamaw.tavli.data.Player

data class GameState(
    var dice: List<Die>,
    var player: Player,
    val movePool: MutableList<Int> = calculateMovePool(dice)
) {
    companion object {
        fun calculateMovePool(dice: List<Die>): MutableList<Int> {
            if (areDoubleDice(dice)) return dice.flatMap { listOf(it.value, it.value) }
                .toMutableList()

            return dice.map { it.value }.toMutableList()
        }

        fun areDoubleDice(dice: List<Die>): Boolean {
            return dice[0].value == dice[1].value
        }
    }

}
