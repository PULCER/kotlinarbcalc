package net.pulcer.sportsarbcalc

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.net.URLEncoder
import java.nio.charset.StandardCharsets



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

        val viewModel: ArbViewModel = viewModel(LocalContext.current as ViewModelStoreOwner)
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
                val encodedEvent1Odds = URLEncoder.encode(event1Odds.trim(), StandardCharsets.UTF_8.toString())
                val encodedEvent2Odds = URLEncoder.encode(event2Odds.trim(), StandardCharsets.UTF_8.toString())
                val encodedEvent3Odds = URLEncoder.encode(if (event3Odds.trim().isNotEmpty()) event3Odds.trim() else "none", StandardCharsets.UTF_8.toString())

                val eventOddsArray = EventOdds(listOf(event1Odds.trim(), event2Odds.trim(), event3Odds.trim()))
                if (!verifyInputs(eventOddsArray, selectedFormat)) {
                    showAlert = true
                } else {
                    val decimalOdds = convertToDecimalOdds(eventOddsArray, selectedFormat)

                    viewModel.setDecimalOdds(decimalOdds)

                    val (arbOpportunity, bettingPercentages) = isArbitrageOpportunity(decimalOdds)

                    if (arbOpportunity) {
                        viewModel.setBettingPercentages(bettingPercentages!!.percentages)
                        navController.navigate("arb/$encodedEvent1Odds/$encodedEvent2Odds/$encodedEvent3Odds")
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
            onClick = { navController.navigate("settings") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Settings")
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

