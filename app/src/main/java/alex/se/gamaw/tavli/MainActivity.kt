package alex.se.gamaw.tavli

import alex.se.gamaw.tavli.composables.GameScreen
import alex.se.gamaw.tavli.composables.menu.ConnectionMenu
import alex.se.gamaw.tavli.composables.menu.GameModeMenu
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TavliTheme {
                val navHost = rememberNavController()
                val vm: BoardViewModel = viewModel()

                NavHost(navHost, "connectionMenu") {
                    composable("connectionMenu") {
                        ConnectionMenu(
                            selectOnline = {

                            },
                            selectLan = {

                            },
                            selectBluetooth = {

                            },
                            selectLocal = {
                                val player1 = Player("White", Color.White)
                                vm.setConnection(
                                    LocalConnection(
                                        listOf(
                                            player1,
                                            Player("Black", Color.Black)
                                        )
                                    ),
                                    player1
                                )
                                navHost.navigate("modeMenu")
                            }
                        )
                    }

                    composable("modeMenu") {
                        GameModeMenu(

                            selectPortes = {
                                vm.setGameMode(PortesMode())
                                navHost.navigate("board")
                            },
                            selectPlakoto = {

                            },
                            selectFevga = {

                            }
                        )
                    }

                    composable("board") {
                        GameScreen(vm)
                    }
                }
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
