package alex.se.gamaw.tavli.data.enums

import androidx.compose.ui.graphics.Color


enum class CollectionIndex(val value: Int) {
    WHITE(-2), BLACK(25);

    companion object {
        fun get(color: Color): Int {
            return if (color == Color.White) WHITE.value else BLACK.value
        }

        fun isCollectionPoint(target: Int): Boolean = WHITE.value == target || BLACK.value == target
    }
}