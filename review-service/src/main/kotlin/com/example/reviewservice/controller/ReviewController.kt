package com.example.reviewservice.controller

import com.example.reviewservice.dto.ReviewRequest
import com.example.reviewservice.dto.ReviewResponse
import com.example.reviewservice.service.ReviewService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/reviews")
class ReviewController(private val reviewService: ReviewService) {

    @PostMapping
    fun createReview(
        @RequestHeader("X-User-ID") userId: String,
        @Valid @RequestBody request: ReviewRequest
    ): ResponseEntity<ReviewResponse> {
        val response = reviewService.createReview(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{reviewId}")
    fun getReviewById(@PathVariable reviewId: Long): ResponseEntity<ReviewResponse> {
        val response = reviewService.getReviewById(reviewId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/user")
    fun getUserReviews(
        @RequestHeader("X-User-ID") userId: String,
        @PageableDefault(size = 10) pageable: Pageable
    ): ResponseEntity<Page<ReviewResponse>> {
        val response = reviewService.getUserReviews(userId, pageable)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/book/{bookId}")
    fun getBookReviews(
        @PathVariable bookId: String,
        @PageableDefault(size = 10) pageable: Pageable
    ): ResponseEntity<Page<ReviewResponse>> {
        val response = reviewService.getBookReviews(bookId, pageable)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/book/{bookId}/score")
    fun getBookAverageScore(@PathVariable bookId: String): ResponseEntity<Map<String, Double>> {
        val averageScore = reviewService.getBookAverageScore(bookId)
        return ResponseEntity.ok(mapOf("averageScore" to averageScore))
    }

    @PutMapping("/{reviewId}")
    fun updateReview(
        @RequestHeader("X-User-ID") userId: String,
        @PathVariable reviewId: Long,
        @Valid @RequestBody request: ReviewRequest
    ): ResponseEntity<ReviewResponse> {
        val response = reviewService.updateReview(userId, reviewId, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{reviewId}")
    fun deleteReview(
        @RequestHeader("X-User-ID") userId: String,
        @PathVariable reviewId: Long
    ): ResponseEntity<Void> {
        reviewService.deleteReview(userId, reviewId)
        return ResponseEntity.noContent().build()
    }
}