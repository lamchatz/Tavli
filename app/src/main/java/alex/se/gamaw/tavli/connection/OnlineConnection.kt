package alex.se.gamaw.tavli.connection

import alex.se.gamaw.tavli.data.TurnData

class OnlineConnection() {

    // Converts incoming network JSON text into TurnData and pushes it to ViewModel
//    override val turnUpdates: Flow<TurnData> = webSocket.incomingMessages()
//        .map { json -> parseJsonToTurnData(json) }

    suspend fun sendTurnData(turnData: TurnData) {
        // Send your finished turn to the server so the opponent gets it
//        val json = serializeToJson(turnData)
//        webSocket.send(json)
    }
}