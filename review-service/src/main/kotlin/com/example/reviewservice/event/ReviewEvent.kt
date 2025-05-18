package com.example.reviewservice.event

import java.time.ZonedDateTime

data class ReviewEvent(
    val eventId: String,
    val type: String = "review",
    val userId: String,
    val bookId: String,
    val score: Int,
    val genres: List<String>,
    val reviewText: String?,
    val timestamp: ZonedDateTime = ZonedDateTime.now()
)
