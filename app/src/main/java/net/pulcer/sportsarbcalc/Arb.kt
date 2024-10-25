package net.pulcer.sportsarbcalc

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import androidx.compose.ui.unit.sp



@Composable
fun ArbScreen(
    onGoBack: () -> Unit,
    event1OddsEncoded: String,
    event2OddsEncoded: String,
    event3OddsEncoded: String?
) {
    val event1Odds = URLDecoder.decode(event1OddsEncoded, StandardCharsets.UTF_8.toString())
    val event2Odds = URLDecoder.decode(event2OddsEncoded, StandardCharsets.UTF_8.toString())
    val event3Odds = event3OddsEncoded?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
    var wagerAmount by remember { mutableStateOf("") }
    var selectedOption by remember { mutableStateOf("Wager") }
    val viewModel: ArbViewModel = viewModel(LocalContext.current as ViewModelStoreOwner)
    val decimalOddsList = viewModel.decimalOdds.value.odds

    val bettingPercentages = viewModel.bettingPercentages.value

    fun formatAmount(amount: Double): String {
        return "$%,.2f".format(amount)
    }

    fun calculateWageredAmount(eventIndex: Int, eventBettingPercentage: Double, bettingPercentages: List<Double>): String {
        val selectedEventIndex = when (selectedOption) {
            "E1" -> 0
            "E2" -> 1
            "E3" -> 2
            else -> -1
        }

        val wagerAmountValue = wagerAmount.toDoubleOrNull() ?: return "%.2f%%".format(eventBettingPercentage * 100)

        return when (selectedEventIndex) {
            -1 -> {
                val wageredAmount = wagerAmountValue * eventBettingPercentage
                "$%.2f".format(wageredAmount)
            }
            0, 1, 2 -> {
                if (eventIndex == selectedEventIndex) {
                    "$%.2f".format(wagerAmountValue)
                } else {
                    val selectedEventPercentage = bettingPercentages.getOrElse(selectedEventIndex) { 1.0 }
                    val totalWager = if (selectedEventPercentage > 0) wagerAmountValue / selectedEventPercentage else 0.0
                    val otherEventWager = totalWager * eventBettingPercentage
                    "$%.2f".format(otherEventWager)
                }
            }
            else -> "Invalid selection"
        }
    }

    fun calculateTotalWager(): Double {
        val event1Wager = calculateWageredAmount(0, bettingPercentages.getOrNull(0) ?: 0.0, bettingPercentages).trim('$').toDoubleOrNull() ?: 0.0
        val event2Wager = calculateWageredAmount(1, bettingPercentages.getOrNull(1) ?: 0.0, bettingPercentages).trim('$').toDoubleOrNull() ?: 0.0
        val event3Wager = if (!event3Odds.isNullOrBlank() && event3Odds != "none") {
            calculateWageredAmount(2, bettingPercentages.getOrNull(2) ?: 0.0, bettingPercentages).trim('$').toDoubleOrNull() ?: 0.0
        } else 0.0

        return event1Wager + event2Wager + event3Wager
    }

    val totalWagerAmount = calculateTotalWager()

    fun calculateArbProfit(): Double {
        val event1Wager = calculateWageredAmount(0, bettingPercentages.getOrNull(0) ?: 0.0, bettingPercentages).trim('$').toDoubleOrNull() ?: 0.0
        val event2Wager = calculateWageredAmount(1, bettingPercentages.getOrNull(1) ?: 0.0, bettingPercentages).trim('$').toDoubleOrNull() ?: 0.0
        val event3Wager = if (!event3Odds.isNullOrBlank() && event3Odds != "none") {
            calculateWageredAmount(2, bettingPercentages.getOrNull(2) ?: 0.0, bettingPercentages).trim('$').toDoubleOrNull() ?: 0.0
        } else 0.0
        val wagers = listOf(event1Wager, event2Wager, event3Wager)

        val potentialPayouts = wagers.zip(decimalOddsList) { wager, odds -> wager * odds }
        val maxPayout = potentialPayouts.maxOrNull() ?: 0.0

        return maxPayout - wagers.sum()
    }

    val arbProfit = calculateArbProfit()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            "Arbitrage Opportunity",
            style = TextStyle(fontSize = 30.sp),
            modifier = Modifier.padding(8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color.Gray)
                .padding(8.dp)
        ) {
            Column {
                bettingPercentages.getOrNull(0)?.let {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Event 1 ($event1Odds):",
                            style = TextStyle(fontSize = 24.sp),
                            modifier = Modifier.weight(1f)
                        )
                        val wagerAmountForEvent1 = calculateWageredAmount(0, it, bettingPercentages)
                            .removePrefix("$")
                            .replace(",", "")
                            .toDoubleOrNull() ?: 0.0
                        Text(
                            formatAmount(wagerAmountForEvent1),
                            style = TextStyle(fontSize = 24.sp)
                        )
                    }
                }


                bettingPercentages.getOrNull(1)?.let {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Event 2 ($event2Odds):",
                            style = TextStyle(fontSize = 24.sp),
                            modifier = Modifier.weight(1f)
                        )
                        val wagerAmountForEvent2 = calculateWageredAmount(1, it, bettingPercentages)
                            .removePrefix("$")
                            .replace(",", "")
                            .toDoubleOrNull() ?: 0.0
                        Text(
                            formatAmount(wagerAmountForEvent2),
                            style = TextStyle(fontSize = 24.sp)
                        )
                    }
                }

                if (!event3Odds.isNullOrBlank() && event3Odds != "none") {
                    bettingPercentages.getOrNull(2)?.let {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Event 3 ($event3Odds):",
                                style = TextStyle(fontSize = 24.sp),
                                modifier = Modifier.weight(1f)
                            )
                            val wagerAmountForEvent3 = calculateWageredAmount(2, it, bettingPercentages)
                                .removePrefix("$")
                                .replace(",", "")
                                .toDoubleOrNull() ?: 0.0
                            Text(
                                formatAmount(wagerAmountForEvent3),
                                style = TextStyle(fontSize = 24.sp)
                            )
                        }
                    }
                }


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Total Wager:",
                        style = TextStyle(fontSize = 24.sp),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        formatAmount(totalWagerAmount),
                        style = TextStyle(fontSize = 24.sp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Arbitrage Profit:",
                        style = TextStyle(fontSize = 24.sp),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        formatAmount(arbProfit),
                        style = TextStyle(fontSize = 24.sp)
                    )
                }
            }
        }

        OutlinedTextField(
            value = wagerAmount,
            onValueChange = {
                wagerAmount = it
            },
            label = { Text("Wager Amount") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp)
                .border(2.dp, Color.Gray),
            placeholder = { Text("Enter wager amount") }

        )


        Spacer(modifier = Modifier.height(16.dp))

        val options = listOf("Wager", "E1", "E2") + (if (!event3Odds.isNullOrBlank() && event3Odds != "none") listOf("E3") else emptyList())
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            options.forEach { option ->
                OutlinedButton(
                    onClick = { selectedOption = option },
                    border = if (option == selectedOption) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(option)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onGoBack,
            modifier = Modifier
                .fillMaxWidth() // This makes the button stretch across the whole width
        ) {
            Text("Go Back")
        }
    }
}
