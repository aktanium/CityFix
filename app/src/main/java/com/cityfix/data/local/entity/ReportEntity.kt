package com.cityfix.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val category: String,
    val imageUri: String?,
    val latitude: Double,
    val longitude: Double,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long
)
