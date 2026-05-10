package com.cityfix.data.remote

import com.cityfix.domain.model.Comment

/**
 * Plain data class used as the Firestore wire format for a comment document.
 * All fields default-init so Firestore's reflective deserializer can construct it.
 */
data class CommentDto(
    val id: String = "",
    val reportId: String = "",
    val userId: String = "",
    val authorName: String = "",
    val text: String = "",
    val createdAt: Long = 0L
) {
    fun toDomain(): Comment = Comment(
        id = id,
        reportId = reportId,
        userId = userId,
        authorName = authorName,
        text = text,
        createdAt = createdAt
    )
}

fun Comment.toDto(): CommentDto = CommentDto(
    id = id,
    reportId = reportId,
    userId = userId,
    authorName = authorName,
    text = text,
    createdAt = createdAt
)
