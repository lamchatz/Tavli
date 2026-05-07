package alex.se.gamaw.tavli

import alex.se.gamaw.tavli.composables.GameScreen
import alex.se.gamaw.tavli.ui.theme.TavliTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TavliTheme {
                GameScreen()
            }
        }
    }

    @Preview
    @Composable
    fun Prev() {

        TavliTheme {
//            GameScreen()
        }
    }
}
