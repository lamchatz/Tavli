package alex.se.gamaw.tavli.connection

import alex.se.gamaw.tavli.data.Die
import alex.se.gamaw.tavli.data.TurnData
import kotlinx.coroutines.flow.Flow

interface Connection {
    val turnUpdates: Flow<TurnData>

    suspend fun sendTurnData(turnData: TurnData)
}