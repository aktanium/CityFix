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
