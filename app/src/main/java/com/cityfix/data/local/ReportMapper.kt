package com.cityfix.data.local

import com.cityfix.data.local.entity.ReportEntity
import com.cityfix.data.remote.ReportDto
import com.cityfix.domain.model.Report

fun ReportEntity.toDomain(): Report = Report(
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
    createdAt = createdAt
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
    createdAt = createdAt
)

fun ReportDto.toDomain(): Report = Report(
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
    createdAt = createdAt
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
    createdAt = createdAt
)
