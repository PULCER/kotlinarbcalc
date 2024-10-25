package net.pulcer.sportsarbcalc

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import net.pulcer.sportsarbcalc.ui.theme.SportsArbCalcTheme
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SportsArbCalcTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    var event1Odds by rememberSaveable { mutableStateOf("") }
                    var event2Odds by rememberSaveable { mutableStateOf("") }
                    var event3Odds by rememberSaveable { mutableStateOf("") }

                    NavHost(navController = navController, startDestination = "bettingArbCalc") {

                        composable("bettingArbCalc") {
                            BettingArbCalcScreen(navController, event1Odds, event2Odds, event3Odds,
                                onEvent1OddsChange = { event1Odds = it },
                                onEvent2OddsChange = { event2Odds = it },
                                onEvent3OddsChange = { event3Odds = it })
                        }

                        composable(
                            route = "arb/{event1Odds}/{event2Odds}/{event3Odds}",
                            arguments = listOf(
                                navArgument("event1Odds") { type = NavType.StringType },
                                navArgument("event2Odds") { type = NavType.StringType },
                                navArgument("event3Odds") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = "none"
                                }
                            )
                        ) { backStackEntry ->
                            ArbScreen(
                                onGoBack = { navController.navigateUp() },
                                event1OddsEncoded = backStackEntry.arguments?.getString("event1Odds") ?: "",
                                event2OddsEncoded = backStackEntry.arguments?.getString("event2Odds") ?: "",
                                event3OddsEncoded = backStackEntry.arguments?.getString("event3Odds")
                            )
                        }


                        composable("noArb") { NoArbScreen(onGoBack = { navController.navigateUp() }) }
                        composable("settings") {
                            SettingsScreen(onGoBackClicked = { navController.navigateUp() })
                        }
                    }
                }
            }
        }
    }
}