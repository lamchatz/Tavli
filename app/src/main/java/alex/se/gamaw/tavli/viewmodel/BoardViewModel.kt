package alex.se.gamaw.tavli.viewmodel

import alex.se.gamaw.tavli.connection.Connection
import alex.se.gamaw.tavli.data.Die
import alex.se.gamaw.tavli.data.Piece
import alex.se.gamaw.tavli.data.Player
import alex.se.gamaw.tavli.gamemode.GameMode
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

class BoardViewModel : ViewModel() {

    private val _piecesByPosition = MutableStateFlow<Map<Int, List<Piece>>>(emptyMap())
    val piecesByPosition: StateFlow<Map<Int, List<Piece>>> = _piecesByPosition.asStateFlow()

    private val _selectedPoint = MutableStateFlow<Int?>(null)
    val selectedPoint = _selectedPoint.asStateFlow()

    private val _allowedMoves = MutableStateFlow<Set<Int>>(emptySet())
    val allowedMoves: StateFlow<Set<Int>> = _allowedMoves.asStateFlow()

    private val _player = MutableStateFlow<Player?>(null)
    val player = _player.asStateFlow()

    private lateinit var gameMode: GameMode
    private lateinit var connection: Connection

    private val _dice = MutableStateFlow(listOf(Die(4), Die(3)))
    val dice = _dice.asStateFlow()

    private val _movePool = MutableStateFlow<List<Int>>(emptyList())

    fun setGameMode(gameMode: GameMode) {
        this.gameMode = gameMode
        this.setInitialBoard(gameMode.initialBoard())
    }

    fun setConnection(connection: Connection) {
        this.connection = connection
        nextRound()
    }

    private fun setInitialBoard(pieces: List<Piece>) {
        _piecesByPosition.value = pieces.groupBy { it.position }
    }

    private fun cancelMove() {
        println("Never mind")
        _selectedPoint.value = null
        _allowedMoves.value = emptySet()
    }

    private fun moveTo(clickedPosition: Int?) {
        val from = _selectedPoint.value ?: return
        val to = clickedPosition ?: return

        if (!_allowedMoves.value.contains(to)) {
            cancelMove()
            return
        }

        println("We are going from ${_selectedPoint.value} to $to")

        _piecesByPosition.value =
            gameMode.resolveMove(_piecesByPosition.value, from, to, _movePool.value)

        markDieAsPlayed(abs(to - from))

        _selectedPoint.value = null
        _allowedMoves.value = emptySet()
    }

    private fun generateLegalMoves(clickedPosition: Int?) {
        val selectedPiece = _piecesByPosition.value[clickedPosition]?.lastOrNull()
        if (selectedPiece != null) {
            if (selectedPiece.color != _player.value?.color) {
                return
            }
            println("Selected: $clickedPosition. We have to go somewhere")

            _selectedPoint.value = clickedPosition

            _allowedMoves.value =
                gameMode.getLegalMoves( _piecesByPosition.value, selectedPiece, _movePool.value)
        }
    }

    fun hasLegalMove(): Boolean {
        return gameMode.hasLegalMove(_piecesByPosition.value, Color.Black, _movePool.value)
    }

    fun processInput(clickedPosition: Int?) {
        if (clickedPosition != null) {
            if (_selectedPoint.value == clickedPosition) {
                cancelMove()
                return
            }

            if (_selectedPoint.value == null) {
                generateLegalMoves(clickedPosition)
                return
            }

            moveTo(clickedPosition)
            if (roundCompleted()) {
                nextRound()
                while (!hasLegalMove()) {
                    nextRound()
                }
            }
        } else {
            cancelMove()
        }
    }

    private fun nextRound() {
        val nextState = connection.nextRound()
        _dice.value = nextState.dice
        _player.value = nextState.player
        _movePool.value = nextState.movePool
    }

    private fun areDiceDouble(): Boolean {
        return _dice.value[0].value == _dice.value[1].value
    }

    private fun roundCompleted(): Boolean {
        return _movePool.value.isEmpty()
    }

    private fun markDieAsPlayed(move: Int) {
        val doubleDice = areDiceDouble()
        val movesSum = _movePool.value.sumOf { it }

        if (doubleDice) {
            val d1 = _dice.value[0].value
            val diceUsed = move / d1

            _movePool.value = _movePool.value.drop(diceUsed)

            val movesLeft = _movePool.value.size
            if (movesLeft < 3) _dice.value[0].played = true
            if (movesLeft == 0) _dice.value[1].played = true

        } else {
            if (move == movesSum) {
                _dice.value.forEach { it.played = true }
                _movePool.value = emptyList()
            } else {
                _dice.value.find { it.value == move && !it.played }?.played = true

                val currentPool = _movePool.value.toMutableList()
                currentPool.remove(move)
                _movePool.value = currentPool
            }
        }
    }
}