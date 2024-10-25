package net.pulcer.sportsarbcalc

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast


fun verifyInputs(eventOdds: EventOdds, format: String): Boolean {
    // Check if at least two events have values
    if (eventOdds.odds.size < 2 || eventOdds.odds[0].isBlank() || eventOdds.odds[1].isBlank()) {
        return false
    }

    for (value in eventOdds.odds) {
        val trimmedValue = value.trim() // Trim leading and trailing whitespace
        if (trimmedValue.isBlank()) continue

        val isValid = when (format) {
            "Decimal" -> trimmedValue.toFloatOrNull()?.let { it > 1 } ?: false
            "Money Line" -> trimmedValue.matches(Regex("^\\+?\\d+$|^-\\d+$"))
            "Fractional" -> trimmedValue.matches(Regex("^\\d+/\\d+$"))
            else -> true
        }

        if (!isValid) return false
    }

    return true
}


fun convertToDecimalOdds(eventOdds: EventOdds, format: String): DecimalOdds {
    val filteredOdds = eventOdds.odds.filter { it.isNotEmpty() }

    val decimalOdds = filteredOdds.map { odd ->
        when (format) {
            "Decimal" -> odd.toDoubleOrNull() ?: 0.0
            "Money Line" -> convertMoneyLineToDecimal(odd)
            "Fractional" -> convertFractionalToDecimal(odd)
            else -> 0.0
        }
    }
    return DecimalOdds(decimalOdds)
}

fun convertMoneyLineToDecimal(odd: String): Double {
    val moneyLineValue = odd.toIntOrNull() ?: return 0.0
    return if (moneyLineValue > 0) {
        moneyLineValue / 100.0 + 1
    } else {
        1 - (100.0 / moneyLineValue)
    }
}

fun convertFractionalToDecimal(odd: String): Double {
    val parts = odd.split("/")
    if (parts.size != 2) return 0.0
    val numerator = parts[0].toDoubleOrNull() ?: return 0.0
    val denominator = parts[1].toDoubleOrNull() ?: return 0.0
    return numerator / denominator + 1
}

fun isArbitrageOpportunity(decimalOdds: DecimalOdds): Pair<Boolean, BettingPercentages?> {
    val inversedOdds = decimalOdds.odds.map { 1.0 / it }
    val sumInversedOdds = inversedOdds.sum()

    if (sumInversedOdds < 1.0) {
        val percentages = inversedOdds.map { (it / sumInversedOdds)  }
        return Pair(true, BettingPercentages(percentages))
    }
    return Pair(false, null)
}

fun Context.leaveReview() {
    val appPackageName = packageName
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
    } catch (e: ActivityNotFoundException) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
    }
}

fun Context.contactMe() {
    val email = "anthony@pulcer.net"
    val subject = "Contact from Parlay Calculator App"
    val mailto = "mailto:$email?subject=${Uri.encode(subject)}"

    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse(mailto)
    }

    try {
        startActivity(emailIntent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, "No email client found.", Toast.LENGTH_LONG).show()
    }
}

fun Context.openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, "No web browser found to open this link: $url", Toast.LENGTH_LONG).show()
    }
}
