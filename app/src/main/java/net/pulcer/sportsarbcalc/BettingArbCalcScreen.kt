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
        var selectedFormat by remember { mutableStateOf(context.loadSavedFormat()) }
        var wagerAmount by remember { mutableStateOf(context.loadSavedWagerAmount()) }

        var event1WagerAmount by remember { mutableStateOf("0.00") }
        var event2WagerAmount by remember { mutableStateOf("0.00") }
        var event3WagerAmount by remember { mutableStateOf("0.00") }
        var profitAmount by remember { mutableStateOf("0.00") }

        LaunchedEffect(Unit) {
            onEvent1OddsChange(context.loadSavedEvent1Odds())
            onEvent2OddsChange(context.loadSavedEvent2Odds())
            onEvent3OddsChange(context.loadSavedEvent3Odds())
        }

        LaunchedEffect(event1Odds, event2Odds, event3Odds) {
            context.saveEventOdds(event1Odds, event2Odds, event3Odds)
        }

        LaunchedEffect(selectedFormat) {
            context.saveFormatPreference(selectedFormat)
        }

        LaunchedEffect(event1Odds, event2Odds, event3Odds, wagerAmount, selectedFormat) {
            val eventOddsArray = EventOdds(listOf(event1Odds.trim(), event2Odds.trim(), event3Odds.trim()))
            if (verifyInputs(eventOddsArray, selectedFormat)) {
                val decimalOdds = convertToDecimalOdds(eventOddsArray, selectedFormat)
                val (arbOpportunity, bettingPercentages) = isArbitrageOpportunity(decimalOdds)

                if (arbOpportunity && wagerAmount.isNotBlank()) {
                    val wagerAmountValue = wagerAmount.toDoubleOrNull() ?: 0.0
                    val percentages = bettingPercentages!!.percentages

                    event1WagerAmount = "$%.2f".format(wagerAmountValue * percentages[0])
                    event2WagerAmount = "$%.2f".format(wagerAmountValue * percentages[1])
                    if (percentages.size > 2) {
                        event3WagerAmount = "$%.2f".format(wagerAmountValue * percentages[2])
                    }

                    val totalWager = wagerAmountValue
                    val maxPayout = maxOf(
                        wagerAmountValue * percentages[0] * decimalOdds.odds[0],
                        wagerAmountValue * percentages[1] * decimalOdds.odds[1],
                        if (percentages.size > 2) wagerAmountValue * percentages[2] * decimalOdds.odds[2] else 0.0
                    )
                    profitAmount = "$%.2f".format(maxPayout - totalWager)
                } else {
                    event1WagerAmount = "0.00"
                    event2WagerAmount = "0.00"
                    event3WagerAmount = "0.00"
                    profitAmount = "0.00"
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = event1Odds,
                onValueChange = onEvent1OddsChange,
                label = { Text("Event 1 Odds") },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.padding(horizontal = 4.dp))

            OutlinedTextField(
                value = event1WagerAmount,
                onValueChange = { },
                label = { Text("Amount Wagered") },
                modifier = Modifier.weight(1f),
                readOnly = true
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = event2Odds,
                onValueChange = onEvent2OddsChange,
                label = { Text("Event 2 Odds") },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.padding(horizontal = 4.dp))

            OutlinedTextField(
                value = event2WagerAmount,
                onValueChange = { },
                label = { Text("Amount Wagered") },
                modifier = Modifier.weight(1f),
                readOnly = true
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = event3Odds,
                onValueChange = onEvent3OddsChange,
                label = { Text("Event 3 Odds") },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.padding(horizontal = 4.dp))

            OutlinedTextField(
                value = event3WagerAmount,
                onValueChange = { },
                label = { Text("Amount Wagered") },
                modifier = Modifier.weight(1f),
                readOnly = true
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = wagerAmount,
                onValueChange = { newAmount ->
                    wagerAmount = newAmount
                    context.saveWagerAmount(newAmount)
                },
                label = { Text("Wager Amount") },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.padding(horizontal = 4.dp))

            OutlinedTextField(
                value = profitAmount,
                onValueChange = { },
                label = { Text("Profit") },
                modifier = Modifier.weight(1f),
                readOnly = true
            )
        }

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
                wagerAmount = ""
                context.saveEventOdds("", "", "")
                context.saveWagerAmount("")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear All")
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