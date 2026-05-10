package com.cityfix.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val authorEmail: String,
    val title: String,
    val description: String,
    val category: String,
    val imageUri: String,
    val latitude: Double,
    val longitude: Double,
    val status: String,
    val createdAt: Long,
    val voteCount: Int = 0,
    /**
     * Comma-joined Firebase uids of users who upvoted this report.
     * Stored on the entity (off the original spec) so the cached feed read
     * path can compute `votedByMe` without round-tripping to Firestore.
     */
    val votedUsersCsv: String = "",
    val commentCount: Int = 0
)
