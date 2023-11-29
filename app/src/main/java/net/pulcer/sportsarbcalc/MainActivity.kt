package net.pulcer.sportsarbcalc

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
fun BettingArbCalcScreen(
    navController: NavController,
    event1Odds: String,
    event2Odds: String,
    event3Odds: String,
    onEvent1OddsChange: (String) -> Unit,
    onEvent2OddsChange: (String) -> Unit,
    onEvent3OddsChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        var showAlert by remember { mutableStateOf(false) }
        val context = LocalContext.current
        var selectedFormat by remember { mutableStateOf(loadSavedFormat(context)) }

        LaunchedEffect(selectedFormat) {
            saveFormatPreference(context, selectedFormat)
        }

        OutlinedTextField(
            value = event1Odds,
            onValueChange = onEvent1OddsChange,
            label = { Text("Event 1 Odds") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = event2Odds,
            onValueChange = onEvent2OddsChange,
            label = { Text("Event 2 Odds") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = event3Odds,
            onValueChange = onEvent3OddsChange,
            label = { Text("Event 3 Odds") },
            modifier = Modifier.fillMaxWidth()
        )


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
                onEvent1OddsChange("")
                onEvent2OddsChange("")
                onEvent3OddsChange("")
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

                    val (arbOpportunity, bettingPercentages) = isArbitrageOpportunity(decimalOdds)

                    if (arbOpportunity) {
                        println("Betting Percentages Array: $bettingPercentages")
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

private fun saveFormatPreference(context: Context, format: String) {
    val sharedPref = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putString("SelectedFormat", format)
        apply()
    }
}

private fun loadSavedFormat(context: Context): String {
    val sharedPref = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    return sharedPref.getString("SelectedFormat", "Decimal") ?: "Decimal"
}

