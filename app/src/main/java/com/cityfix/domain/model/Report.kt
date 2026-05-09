package com.cityfix.domain.model

import java.time.Instant

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "reports")
data class Report(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val authorEmail: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val imageUri: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val status: String = "NEW",
    val createdAt: Long = System.currentTimeMillis()
)

data class GeoLocation(
    val latitude: Double,
    val longitude: Double
)

enum class ReportCategory(val displayName: String, val iconRes: String) {
    DAMAGED_ROAD("Damaged Road", "road"),
    GARBAGE("Garbage Accumulation", "delete"),
    BROKEN_STREETLIGHT("Broken Streetlight", "lightbulb"),
    WATER_LEAKAGE("Water Leakage", "water_drop"),
    ILLEGAL_DUMPING("Illegal Dumping", "warning"),
    PUBLIC_SAFETY("Public Safety", "security");

    companion object {
        fun fromName(name: String): ReportCategory =
            entries.firstOrNull { it.name == name } ?: DAMAGED_ROAD
    }
}

enum class ReportStatus(val displayName: String) {
    NEW("New"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved");

    companion object {
        fun fromName(name: String): ReportStatus =
            entries.firstOrNull { it.name == name } ?: NEW
    }
}

data class ReportStats(
    val total: Int,
    val newCount: Int,
    val inProgressCount: Int,
    val resolvedCount: Int
)

data class AppSettings(
    val isDarkMode: Boolean,
    val isNotificationsEnabled: Boolean,
    val mapView: String
)
