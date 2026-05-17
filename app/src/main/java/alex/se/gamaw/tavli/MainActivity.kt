package alex.se.gamaw.tavli

import alex.se.gamaw.tavli.composables.GameScreen
import alex.se.gamaw.tavli.connection.LocalConnection
import alex.se.gamaw.tavli.data.Player
import alex.se.gamaw.tavli.gamemode.PortesMode
import alex.se.gamaw.tavli.ui.theme.TavliTheme
import alex.se.gamaw.tavli.viewmodel.BoardViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TavliTheme {
                val vm: BoardViewModel = viewModel()
                vm.setGameMode(PortesMode())
                vm.setConnection(LocalConnection(listOf(Player("White", Color.White), Player("Black", Color.Black))))

                GameScreen(vm)
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
