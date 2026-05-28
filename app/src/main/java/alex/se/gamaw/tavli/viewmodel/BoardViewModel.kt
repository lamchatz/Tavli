package alex.se.gamaw.tavli.viewmodel

import alex.se.gamaw.tavli.connection.Connection
import alex.se.gamaw.tavli.data.GameState
import alex.se.gamaw.tavli.data.Piece
import alex.se.gamaw.tavli.data.Player
import alex.se.gamaw.tavli.data.TurnData
import alex.se.gamaw.tavli.gamemode.GameMode
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

class BoardViewModel : ViewModel() {


    private val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    private var localPlayerProfile: Player? = null

    private lateinit var gameMode: GameMode
    private lateinit var connection: Connection

    fun setGameMode(gameMode: GameMode) {
        this.gameMode = gameMode
        this.setInitialBoard(gameMode.initialBoard())
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

    private fun setInitialBoard(pieces: List<Piece>) {
        _gameState.update { currentState ->
            currentState.copy(piecesByPosition = pieces.groupBy { it.position })
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

        _gameState.update { currentState ->
            currentState.copy(
                piecesByPosition = gameMode.resolveMove(
                    currentState.piecesByPosition,
                    from,
                    to,
                    currentState.movePool
                )
            )
        }

        markDieAsPlayed(abs(to - from))

        cancelMove()
        if (!hasLegalMove()) {
            viewModelScope.launch {
                connection.sendTurnData(turnData())
            }
        }
    }

    private fun generateLegalMoves(clickedPosition: Int?) {
        val selectedPiece = _gameState.value.piecesByPosition[clickedPosition]?.lastOrNull()
        if (selectedPiece != null) {
            if (isNotMyTurn(selectedPiece)) {
                return
            }
            println("Selected: $clickedPosition. We have to go somewhere")

            _gameState.update { currentState ->
                currentState.copy(
                    selectedPoint = clickedPosition,
                    allowedMoves = gameMode.getLegalMoves(
                        currentState.piecesByPosition,
                        selectedPiece,
                        currentState.movePool
                    )
                )
            }
        }
    }

    fun hasLegalMove(): Boolean {
        if (!::gameMode.isInitialized) return true

        return gameMode.hasLegalMove(
            _gameState.value.piecesByPosition,
            _gameState.value.currentPlayer!!.color,
            _gameState.value.movePool
        )
    }

    fun processInput(clickedPosition: Int?) {
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
            if (roundCompleted()) {
                cancelMove()
                viewModelScope.launch {
                    connection.sendTurnData(turnData())
                }
            }
        } else {
            cancelMove()
        }
    }

    private fun areDiceDouble(): Boolean {
        return _gameState.value.dice[0].value == _gameState.value.dice[1].value
    }

    private fun roundCompleted(): Boolean {
        return _gameState.value.movePool.isEmpty()
    }

    private fun markDieAsPlayed(move: Int) {
        val currentPool = _gameState.value.movePool
        val currentDice = _gameState.value.dice
        val doubleDice = areDiceDouble()
        val movesSum = currentPool.sumOf { it }

        var nextPool = currentPool
        var nextDice = currentDice

        if (doubleDice) {
            val d1 = currentDice[0].value
            val diceUsed = move / d1
            nextPool = currentPool.drop(diceUsed)

            nextDice = currentDice.mapIndexed { index, die ->
                when {
                    index == 0 && nextPool.size < 3 -> die.copy(played = true)
                    index == 1 && nextPool.isEmpty() -> die.copy(played = true)
                    else -> die
                }
            }
        } else {
            if (move == movesSum) {
                nextDice = currentDice.map { it.copy(played = true) }
                nextPool = emptyList()
            } else {
                val dieIndexToPlay = currentDice.indexOfFirst { it.value == move && !it.played }

                if (dieIndexToPlay != -1) {
                    nextDice = currentDice.mapIndexed { index, die ->
                        if (index == dieIndexToPlay) die.copy(played = true) else die
                    }
                    nextPool = currentPool.toMutableList().apply { remove(move) }
                }
            }
        }

        _gameState.update { currentState ->
            currentState.copy(movePool = nextPool, dice = nextDice)
        }
    }

    private fun turnData(): TurnData = TurnData(
        _gameState.value.dice,
        _gameState.value.currentPlayer!!,
        _gameState.value.movePool
    )

    private fun isNotMyTurn(selectedPiece: Piece): Boolean {
        return _gameState.value.currentPlayer?.color != selectedPiece.color
    }
}