package alex.se.gamaw.tavli.connection

import alex.se.gamaw.tavli.data.Die
import alex.se.gamaw.tavli.data.Player
import alex.se.gamaw.tavli.data.TurnData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.security.SecureRandom

class LocalConnection(private val players: List<Player>) : Connection {
    private val random = SecureRandom()
    private var currentPlayerIndex = 0

    private val _turnUpdates = MutableSharedFlow<TurnData>(replay = 1)
    override val turnUpdates = _turnUpdates.asSharedFlow()

    override suspend fun sendTurnData(turnData: TurnData) {
//        val diceVal1 = random.nextInt(6) + 1
//        val diceVal2 = random.nextInt(6) + 1
        val diceVal1 = 3
        val diceVal2 = 4
        val dice = listOf(Die(diceVal1), Die(diceVal2))

        // Calculate pool
        val pool = if (diceVal1 == diceVal2) listOf(diceVal1, diceVal1, diceVal1, diceVal1)
        else listOf(diceVal1, diceVal2)

        val nextPlayer = players[currentPlayerIndex]
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size

        _turnUpdates.tryEmit(TurnData(dice, nextPlayer, pool, currentPlayerIndex == 1))
    }
}