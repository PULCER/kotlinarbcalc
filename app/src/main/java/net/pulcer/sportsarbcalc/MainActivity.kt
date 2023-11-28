package net.pulcer.sportsarbcalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import net.pulcer.sportsarbcalc.ui.theme.SportsArbCalcTheme
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

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
                    NavHost(navController = navController, startDestination = "bettingArbCalc") {
                        composable("bettingArbCalc") { BettingArbCalcScreen(navController) }
                        composable("arb") { ArbScreen(onGoBack = { navController.navigateUp() }) }
                        composable("noArb") { NoArbScreen(onGoBack = { navController.navigateUp() }) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BettingArbCalcScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var event1Odds by remember { mutableStateOf("") }
        var event2Odds by remember { mutableStateOf("") }
        var event3Odds by remember { mutableStateOf("") }
        var showAlert by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = event1Odds,
            onValueChange = { event1Odds = it },
            label = { Text("Event 1 Odds") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = event2Odds,
            onValueChange = { event2Odds = it },
            label = { Text("Event 2 Odds") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = event3Odds,
            onValueChange = { event3Odds = it },
            label = { Text("Event 3 Odds") },
            modifier = Modifier.fillMaxWidth()
        )

        var selectedFormat by remember { mutableStateOf("Decimal") }
        val formats = listOf("Decimal", "Money Line", "Fractional")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            formats.forEach { format ->
                OutlinedButton(
                    onClick = { selectedFormat = format },
                    border = if (format == selectedFormat) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(format)
                }
            }
        }

        Button(
            onClick = {
                event1Odds = ""
                event2Odds = ""
                event3Odds = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear All")
        }


        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val eventOddsArray = EventOdds(listOf(event1Odds, event2Odds, event3Odds))
                if (!verifyInputs(eventOddsArray, selectedFormat)) {
                    showAlert = true
                } else {
                    println("Event Input Array: $eventOddsArray")
                    val decimalOdds = convertToDecimalOdds(eventOddsArray, selectedFormat)

                    if (isArbitrageOpportunity(decimalOdds)) {
                        navController.navigate("arb")
                    } else {
                        navController.navigate("noArb")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calculate")
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { /* About Logic */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("About")
        }
        if (showAlert) {
            AlertDialog(
                onDismissRequest = { showAlert = false },
                title = { Text(text = "Please check your inputs!") },
                confirmButton = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { showAlert = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("OK")
                        }
                    }
                }
            )
        }
    }
}

