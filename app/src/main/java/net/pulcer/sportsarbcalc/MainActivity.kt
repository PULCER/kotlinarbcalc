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

                        composable("settings") {
                            SettingsScreen(onGoBackClicked = { navController.navigateUp() })
                        }
                    }
                }
            }
        }
    }
}