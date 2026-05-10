package com.cityfix.data.remote

/**
 * Plain data class used as the Firestore wire format. All fields default-init
 * so Firestore's reflective deserializer can construct it.
 */
data class ReportDto(
    val id: String = "",
    val userId: String = "",
    val authorEmail: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val imageUri: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val status: String = "NEW",
    val createdAt: Long = 0L
)
