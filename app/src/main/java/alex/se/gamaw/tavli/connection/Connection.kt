package alex.se.gamaw.tavli.connection

import alex.se.gamaw.tavli.composables.GameState
import alex.se.gamaw.tavli.data.Die

interface Connection {
    fun getDice(): List<Die>

    fun nextRound(): GameState
}