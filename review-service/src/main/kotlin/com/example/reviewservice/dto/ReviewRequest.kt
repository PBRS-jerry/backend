package com.example.reviewservice.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class ReviewRequest(
    @field:NotBlank(message = "Book ID is required")
    val bookId: String,

    @field:Min(1, message = "Score must be between 1 and 5")
    @field:Max(5, message = "Score must be between 1 and 5")
    val score: Int,

    @field:NotEmpty(message = "At least one genre is required")
    val genres: List<String>,

    val reviewText: String? = null
)
