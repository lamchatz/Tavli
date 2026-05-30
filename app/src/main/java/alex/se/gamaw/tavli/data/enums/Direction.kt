package alex.se.gamaw.tavli.data.enums

import androidx.compose.ui.graphics.Color

enum class Direction(val value: Int) {
    WHITE(-1), BLACK(1);

    companion object {
        fun get(color: Color): Int {
            return get(Color.White == color)
        }

        fun get(isWhite: Boolean): Int {
            return if (isWhite) WHITE.value else BLACK.value
        }
    }
}