package com.cityfix.domain.model

data class Comment(
    val id: String = "",
    val reportId: String = "",
    val userId: String = "",
    val authorName: String = "",
    val text: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
