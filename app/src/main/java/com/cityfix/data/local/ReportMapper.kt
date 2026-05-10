package com.cityfix.data.local

import com.cityfix.data.local.entity.ReportEntity
import com.cityfix.data.remote.ReportDto
import com.cityfix.domain.model.Report

private fun parseVotedUsersCsv(csv: String): List<String> =
    if (csv.isEmpty()) emptyList() else csv.split(",")

fun ReportEntity.toDomain(currentUserId: String? = null): Report = Report(
    id = id,
    userId = userId,
    authorEmail = authorEmail,
    title = title,
    description = description,
    category = category,
    imageUri = imageUri,
    latitude = latitude,
    longitude = longitude,
    status = status,
    createdAt = createdAt,
    voteCount = voteCount,
    votedByMe = currentUserId != null && currentUserId in parseVotedUsersCsv(votedUsersCsv),
    commentCount = commentCount
)

fun Report.toEntity(): ReportEntity = ReportEntity(
    id = id,
    userId = userId,
    authorEmail = authorEmail,
    title = title,
    description = description,
    category = category,
    imageUri = imageUri,
    latitude = latitude,
    longitude = longitude,
    status = status,
    createdAt = createdAt,
    voteCount = voteCount,
    // Domain Report doesn't carry the voter list (only the boolean votedByMe),
    // so when constructing an entity from a Report we leave the CSV empty.
    // The snapshot path uses `ReportDto.toEntity()` instead, which preserves it.
    votedUsersCsv = "",
    commentCount = commentCount
)

fun ReportDto.toDomain(currentUserId: String? = null): Report = Report(
    id = id,
    userId = userId,
    authorEmail = authorEmail,
    title = title,
    description = description,
    category = category,
    imageUri = imageUri,
    latitude = latitude,
    longitude = longitude,
    status = status,
    createdAt = createdAt,
    voteCount = voteCount,
    votedByMe = currentUserId != null && currentUserId in votedUsers,
    commentCount = commentCount
)

/** Used by the snapshot listener to cache the *full* voter list locally. */
fun ReportDto.toEntity(): ReportEntity = ReportEntity(
    id = id,
    userId = userId,
    authorEmail = authorEmail,
    title = title,
    description = description,
    category = category,
    imageUri = imageUri,
    latitude = latitude,
    longitude = longitude,
    status = status,
    createdAt = createdAt,
    voteCount = voteCount,
    votedUsersCsv = votedUsers.joinToString(","),
    commentCount = commentCount
)

// Not in the original spec, but needed for Firestore writes from the repo.
fun Report.toDto(): ReportDto = ReportDto(
    id = id,
    userId = userId,
    authorEmail = authorEmail,
    title = title,
    description = description,
    category = category,
    imageUri = imageUri,
    latitude = latitude,
    longitude = longitude,
    status = status,
    createdAt = createdAt,
    voteCount = voteCount,
    // votedUsers is intentionally empty: writes from a domain Report happen
    // via `set(merge = true)` so we never overwrite the canonical voter list.
    votedUsers = emptyList(),
    commentCount = commentCount
)
