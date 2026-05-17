package alex.se.gamaw.tavli.connection

import alex.se.gamaw.tavli.composables.GameState
import alex.se.gamaw.tavli.data.Die
import alex.se.gamaw.tavli.data.Player
import kotlinx.coroutines.flow.MutableStateFlow
import java.security.SecureRandom

class LocalConnection(val players: List<Player>) : Connection {
    private val _currentPlayer = MutableStateFlow(0)
    private val random = SecureRandom()
    override fun getDice(): List<Die> {
        return listOf(Die(random.nextInt(6) + 1), Die(random.nextInt(6) + 1))
    }

    override fun nextRound(): GameState {
        _currentPlayer.value = (_currentPlayer.value + 1) % players.size
        return GameState(getDice(), players[_currentPlayer.value])
    }
}