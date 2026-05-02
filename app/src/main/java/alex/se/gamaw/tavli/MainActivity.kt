package alex.se.gamaw.tavli

import alex.se.gamaw.tavli.composables.TavliBoardView
import alex.se.gamaw.tavli.composables.TavliScreen
import alex.se.gamaw.tavli.ui.theme.TavliTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TavliTheme {
                TavliScreen()
//                TavliBoardView()
//                TavliBoard(points = listOf(BoardPoint(1,1,false, false)))
            }
        }
    }
}
