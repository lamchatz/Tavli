package alex.se.gamaw.tavli.viewmodel

import alex.se.gamaw.tavli.data.Piece
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BoardViewModel() : ViewModel() {

    private val _piecesByPosition = MutableStateFlow<Map<Int, List<Piece>>>(emptyMap())
    val piecesByPosition: StateFlow<Map<Int, List<Piece>>> = _piecesByPosition.asStateFlow()

    private val _selectedPoint = MutableStateFlow<Int?>(null)
    val selectedPoint = _selectedPoint.asStateFlow()

    fun setInitialBoard(pieces: List<Piece>) {
        _piecesByPosition.value = pieces.groupBy { it.position }
    }

    fun s(clickedPosition: Int?) {
        if (clickedPosition != null) {
            if (_selectedPoint.value == clickedPosition) {
                println("Never mind")
                _selectedPoint.value = null
            }

            if (_selectedPoint.value == null) {
                // Get the list at that index, then find the last element
                val selectedPiece = _piecesByPosition.value[clickedPosition]?.lastOrNull()
                if (selectedPiece != null) {
                    println("Selected: $clickedPosition. We have to go somewhere")

                    _selectedPoint.value = clickedPosition

                }
            } else {
                println("We are going from $selectedPoint to $clickedPosition")

                _piecesByPosition.update { currentMap ->
                    val fromIndex = selectedPoint.value ?: return@update currentMap // This is Int?

                    val oldList = currentMap[fromIndex].orEmpty()

                    if (oldList.isNotEmpty()) {
                        val pieceToMove = oldList.last().copy(position = clickedPosition)
                        val updatedOldList = oldList.dropLast(1)
                        val updatedNewList = currentMap[clickedPosition].orEmpty() + pieceToMove

                        // Use fromIndex here (which is now smart-cast to Int)
                        currentMap + mapOf(
                            fromIndex to updatedOldList,
                            clickedPosition to updatedNewList
                        )
                    } else {
                        currentMap
                    }
                }

                _selectedPoint.value = null
            }
        } else {
            _selectedPoint.value = null
        }

    }
}