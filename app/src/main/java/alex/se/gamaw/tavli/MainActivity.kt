package alex.se.gamaw.tavli

import alex.se.gamaw.tavli.composables.Board
import alex.se.gamaw.tavli.data.Piece
import alex.se.gamaw.tavli.ui.theme.TavliTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val samplePieces = listOf(
                Piece(0, Color.White),
                Piece(1, Color.Gray),
                Piece(2, Color.Green),
                Piece(2, Color.Green),
                Piece(3, Color.Red),
                Piece(4, Color.Blue),
                Piece(5, Color.Magenta),
                Piece(6, Color.DarkGray),
                Piece(11, Color.Black),
                Piece(12, Color.Red),
                Piece(18, Color.White),
            )
            TavliTheme {
                Board(samplePieces, onPieceClick = { i ->

                    println("lala");
                    println(i)
                })
            }
        }
    }

    @Preview
    @Composable
    fun prev() {
        val samplePieces = listOf(
            Piece(0, Color.White),
            Piece(1, Color.Gray),
            Piece(2, Color.Green),
            Piece(2, Color.Green),
            Piece(3, Color.Red),
            Piece(4, Color.Blue),
            Piece(5, Color.Magenta),
            Piece(6, Color.DarkGray),
            Piece(11, Color.Black),
            Piece(12, Color.Red),
            Piece(18, Color.White),
        )
        TavliTheme {
            Board(samplePieces, onPieceClick = { i ->
                println("lala")
                println(i)
            })
        }
    }
}
