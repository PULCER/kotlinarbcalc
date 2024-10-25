package net.pulcer.sportsarbcalc

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

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