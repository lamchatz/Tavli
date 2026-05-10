package alex.se.gamaw.tavli.viewmodel

import alex.se.gamaw.tavli.connection.Connection
import alex.se.gamaw.tavli.data.Die
import alex.se.gamaw.tavli.data.Piece
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

    private lateinit var gameMode: GameMode
    private lateinit var connection: Connection

    private val _dice = MutableStateFlow<List<Die>>(emptyList())
    val dice = _dice.asStateFlow()

    private var numberOfMoves = 0

    fun setGameMode(gameMode: GameMode) {
        this.gameMode = gameMode
        this.setInitialBoard(gameMode.initialBoard())
    }

    fun setConnection(connection: Connection) {
        this.connection = connection
        _dice.value = connection.getDice()
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

        _piecesByPosition.value = gameMode.resolveMove(_piecesByPosition.value, from, to)

        numberOfMoves++
        markDieAsPlayed(abs(to - from))

        _selectedPoint.value = null
        _allowedMoves.value = emptySet()
    }

    private fun generateLegalMoves(clickedPosition: Int?) {
        val selectedPiece = _piecesByPosition.value[clickedPosition]?.lastOrNull()
        if (selectedPiece != null) {
            println("Selected: $clickedPosition. We have to go somewhere")

            _selectedPoint.value = clickedPosition

            _allowedMoves.value =
                gameMode.getLegalMoves(selectedPiece, _piecesByPosition.value, _dice.value)
        }
    }

    fun hasLegalMove(): Boolean {
        return gameMode.hasLegalMove(_piecesByPosition.value, Color.Black, _dice.value)
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
            }
        } else {
            cancelMove()
        }
    }

    private fun nextRound() {
        numberOfMoves = 0
        _dice.value = connection.getDice()
    }

    private fun areDiceDouble(): Boolean {
        return _dice.value[0].value == _dice.value[1].value
    }

    private fun roundCompleted(): Boolean {
        return _dice.value[0].played && _dice.value[1].played
    }

    private fun markDieAsPlayed(move: Int) {
        if (areDiceDouble()) {
            if (numberOfMoves == 2) {
                _dice.value[0].played = true
            } else if (numberOfMoves == 4) {
                _dice.value[1].played = true
            }

        } else {
            _dice.value.find { it.value == move && !it.played }?.played = true
        }
    }
}