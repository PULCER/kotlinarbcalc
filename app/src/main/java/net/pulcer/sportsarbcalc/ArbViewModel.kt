package net.pulcer.sportsarbcalc

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class ArbViewModel : ViewModel() {
    var bettingPercentages = mutableStateOf(emptyList<Double>())
    var decimalOdds = mutableStateOf(DecimalOdds(emptyList()))

    fun setBettingPercentages(percentages: List<Double>) {
        bettingPercentages.value = percentages
    }

    fun setDecimalOdds(odds: DecimalOdds) {
        decimalOdds.value = odds
    }
}
