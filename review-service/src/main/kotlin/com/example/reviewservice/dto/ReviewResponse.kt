package com.example.reviewservice.dto

import java.time.ZonedDateTime

data class ReviewResponse(
    val id: Long,
    val userId: String,
    val bookId: String,
    val score: Int,
    val genres: List<String>,
    val reviewText: String?,
    val createdAt: ZonedDateTime
)