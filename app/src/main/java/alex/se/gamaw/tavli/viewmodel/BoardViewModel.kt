package alex.se.gamaw.tavli.viewmodel

import alex.se.gamaw.tavli.connection.Connection
import alex.se.gamaw.tavli.data.GameState
import alex.se.gamaw.tavli.data.Move
import alex.se.gamaw.tavli.data.Player
import alex.se.gamaw.tavli.data.TurnData
import alex.se.gamaw.tavli.gamemode.GameMode
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BoardViewModel : ViewModel() {
    private val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    private var localPlayerProfile: Player? = null

    private lateinit var gameMode: GameMode
    private lateinit var connection: Connection

    fun setGameMode(gameMode: GameMode) {
        this.gameMode = gameMode
        _gameState.update { currentState ->
            currentState.copy(boardState = gameMode.initialBoard().groupBy { it.position })
        }
    }

    fun setConnection(connection: Connection, myProfile: Player) {
        this.connection = connection
        this.localPlayerProfile = myProfile

        viewModelScope.launch {
            connection.sendTurnData(
                TurnData(
                    _gameState.value.dice,
                    myProfile,
                    _gameState.value.movePool
                )
            )

            connection.turnUpdates.collect { turnData ->
                _gameState.update { currentState ->
                    currentState.copy(
                        dice = turnData.dice,
                        currentPlayer = turnData.player,
                        movePool = turnData.movePool,
                        showRight = turnData.showRight
                    )
                }

                if (!hasLegalMove()) {
                    connection.sendTurnData(turnData())
                }
            }

        }

    }

    private fun cancelMove() {
        println("Never mind")
        _gameState.update { currentState ->
            currentState.copy(selectedPoint = null, allowedMoves = emptySet())
        }
    }

    private fun moveTo(clickedPosition: Int?) {
        val from = _gameState.value.selectedPoint ?: return
        val to = clickedPosition ?: return

        if (!_gameState.value.allowedMoves.contains(to)) {
            cancelMove()
            return
        }

        println("We are going from ${_gameState.value.selectedPoint} to $to")

        var updatedSnapshot: GameState? = null
        _gameState.update { currentState ->
            currentState.updateGameState(
                gameMode.resolveMove(
                    Move(
                        currentState.boardState,
                        from,
                        to,
                        currentState.movePool
                    )
                )
            ).also { updatedSnapshot = it }
        }

        cancelMove()
        updatedSnapshot?.let { freshState ->
            if (!hasLegalMove(freshState) || freshState.roundCompleted()) {
                viewModelScope.launch {
                    connection.sendTurnData(turnData(freshState))
                }
            }
        }
    }

    private fun generateLegalMoves(clickedPosition: Int?) {
        val selectedPiece = _gameState.value.boardState[clickedPosition]?.lastOrNull()
        if (selectedPiece != null) {
            if (_gameState.value.isNotMyTurn(selectedPiece)) {
                return
            }
            println("Selected: $clickedPosition. We have to go somewhere")

            _gameState.update { currentState ->
                currentState.copy(
                    selectedPoint = clickedPosition,
                    allowedMoves = gameMode.getLegalMoves(
                        currentState.boardState,
                        selectedPiece,
                        currentState.movePool
                    )
                )
            }
        }
    }

    fun hasLegalMove(state: GameState = _gameState.value): Boolean {
        if (!::gameMode.isInitialized) return true

        val activeColor = state.currentPlayer?.color ?: return false

        return gameMode.hasLegalMove(
            state.boardState,
            activeColor,
            state.movePool
        )
    }

    fun processInput(clickedPosition: Int?) {
        println("Clicked on: $clickedPosition")
        if (clickedPosition != null) {
            if (_gameState.value.selectedPoint == clickedPosition) {
                cancelMove()
                return
            }

            if (_gameState.value.selectedPoint == null) {
                generateLegalMoves(clickedPosition)
                return
            }

            moveTo(clickedPosition)
        } else {
            cancelMove()
        }
    }

    private fun turnData(state: GameState = _gameState.value): TurnData = TurnData(
        state.dice,
        state.currentPlayer!!, //handle Player?
        state.movePool
    )
}