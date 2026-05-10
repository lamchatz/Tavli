package alex.se.gamaw.tavli.viewmodel

import alex.se.gamaw.tavli.data.Piece
import alex.se.gamaw.tavli.gamemode.GameMode
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BoardViewModel : ViewModel() {

    private val _piecesByPosition = MutableStateFlow<Map<Int, List<Piece>>>(emptyMap())
    val piecesByPosition: StateFlow<Map<Int, List<Piece>>> = _piecesByPosition.asStateFlow()

    private val _selectedPoint = MutableStateFlow<Int?>(null)
    val selectedPoint = _selectedPoint.asStateFlow()

    private val _allowedMoves = MutableStateFlow<Set<Int>>(emptySet())
    val allowedMoves: StateFlow<Set<Int>> = _allowedMoves.asStateFlow()

    private lateinit var gameMode: GameMode

    fun setGameMode(gameMode: GameMode) {
        this.gameMode = gameMode
        this.setInitialBoard(gameMode.initialBoard())
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

        _piecesByPosition.update { currentMap ->
            val oldList = currentMap[from].orEmpty()

            if (oldList.isNotEmpty()) {
                val pieceToMove = oldList.last().copy(position = to)
                val updatedOldList = oldList.dropLast(1)
                val updatedNewList = currentMap[to].orEmpty() + pieceToMove

                currentMap + mapOf(
                    from to updatedOldList,
                    to to updatedNewList
                )
            } else {
                currentMap
            }
        }
        _selectedPoint.value = null
        _allowedMoves.value = emptySet()

    }

    private fun generateLegalMoves(clickedPosition: Int?) {
        val selectedPiece = _piecesByPosition.value[clickedPosition]?.lastOrNull()
        if (selectedPiece != null) {
            println("Selected: $clickedPosition. We have to go somewhere")

            _selectedPoint.value = clickedPosition

            _allowedMoves.value = gameMode.getLegalMoves(selectedPiece, _piecesByPosition.value)
            println(gameMode.hasLegalMove(_piecesByPosition.value, Color.White, listOf(6,6)))
        }
    }

    fun hasLegalMove(): Boolean {
        return gameMode.hasLegalMove(_piecesByPosition.value, Color.Black, listOf(5,5))
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
        } else {
            cancelMove()
        }
    }
}