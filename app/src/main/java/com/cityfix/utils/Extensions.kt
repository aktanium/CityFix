package com.cityfix.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Instant.toFormattedDate(): String =
    this.atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("MMM d, yyyy"))

fun Instant.toFormattedDateTime(): String =
    this.atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' HH:mm"))

fun Instant.toRelativeTime(): String {
    val now = Instant.now()
    val seconds = now.epochSecond - this.epochSecond
    return when {
        seconds < 60 -> "just now"
        seconds < 3600 -> "${seconds / 60}m ago"
        seconds < 86400 -> "${seconds / 3600}h ago"
        seconds < 604800 -> "${seconds / 86400}d ago"
        else -> toFormattedDate()
    }
}

fun Double.toCoordinateString(): String = "%.6f".format(this)

/**
 * Converts an epoch-millis timestamp into a short relative phrase
 * ("Just now", "5m ago", "3d ago", "2w ago"). Used by feed-style UI.
 */
fun Long.toTimeAgo(): String {
    val diff = System.currentTimeMillis() - this
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 604_800_000 -> "${diff / 86_400_000}d ago"
        else -> "${diff / 604_800_000}w ago"
    }
}
