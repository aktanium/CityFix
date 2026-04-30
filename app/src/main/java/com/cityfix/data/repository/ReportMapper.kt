package com.cityfix.data.repository

import com.cityfix.data.local.entity.ReportEntity
import com.cityfix.domain.model.GeoLocation
import com.cityfix.domain.model.Report
import com.cityfix.domain.model.ReportCategory
import com.cityfix.domain.model.ReportStatus
import java.time.Instant

fun ReportEntity.toDomain(): Report = Report(
    id = id,
    title = title,
    description = description,
    category = ReportCategory.fromName(category),
    imageUri = imageUri,
    location = GeoLocation(latitude = latitude, longitude = longitude),
    status = ReportStatus.fromName(status),
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt)
)

fun Report.toEntity(): ReportEntity = ReportEntity(
    id = id,
    title = title,
    description = description,
    category = category.name,
    imageUri = imageUri,
    latitude = location.latitude,
    longitude = location.longitude,
    status = status.name,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli()
)
