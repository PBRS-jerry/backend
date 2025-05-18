package com.example.reviewservice.service

import com.example.reviewservice.dto.ReviewRequest
import com.example.reviewservice.dto.ReviewResponse
import com.example.reviewservice.event.ReviewEvent
import com.example.reviewservice.event.ReviewEventProducer
import com.example.reviewservice.exception.ResourceNotFoundException
import com.example.reviewservice.model.Review
import com.example.reviewservice.repository.ReviewRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val reviewEventProducer: ReviewEventProducer
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun createReview(userId: String, request: ReviewRequest): ReviewResponse {
        logger.info("Creating review for user $userId and book ${request.bookId}")

        val review = Review(
            userId = userId,
            bookId = request.bookId,
            score = request.score,
            genres = request.genres.toTypedArray(),
            reviewText = request.reviewText
        )

        val savedReview = reviewRepository.save(review)

        // Publish event to Kafka
        val reviewEvent = ReviewEvent(
            eventId = "",  // Will be set by producer
            userId = userId,
            bookId = request.bookId,
            score = request.score,
            genres = request.genres,
            reviewText = request.reviewText
        )
        reviewEventProducer.publishReviewEvent(reviewEvent)

        return mapToResponse(savedReview)
    }

    @Transactional(readOnly = true)
    fun getReviewById(reviewId: Long): ReviewResponse {
        logger.debug("Fetching review with ID: $reviewId")

        val review = reviewRepository.findById(reviewId)
            .orElseThrow { ResourceNotFoundException("Review not found with id: $reviewId") }

        return mapToResponse(review)
    }

    @Transactional(readOnly = true)
    fun getUserReviews(userId: String, pageable: Pageable): Page<ReviewResponse> {
        logger.debug("Fetching reviews for user: $userId")

        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map { mapToResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getBookReviews(bookId: String, pageable: Pageable): Page<ReviewResponse> {
        logger.debug("Fetching reviews for book: $bookId")

        return reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId, pageable)
            .map { mapToResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getBookAverageScore(bookId: String): Double {
        logger.debug("Calculating average score for book: $bookId")

        return reviewRepository.calculateAverageScoreForBook(bookId) ?: 0.0
    }

    @Transactional
    fun updateReview(userId: String, reviewId: Long, request: ReviewRequest): ReviewResponse {
        logger.info("Updating review $reviewId for user $userId")

        val existingReview = reviewRepository.findById(reviewId)
            .orElseThrow { ResourceNotFoundException("Review not found with id: $reviewId") }

        if (existingReview.userId != userId) {
            throw IllegalArgumentException("User does not own this review")
        }

        val updatedReview = Review(
            id = existingReview.id,
            userId = userId,
            bookId = request.bookId,
            score = request.score,
            genres = request.genres.toTypedArray(),
            reviewText = request.reviewText,
            createdAt = existingReview.createdAt
        )

        val savedReview = reviewRepository.save(updatedReview)

        // Publish update event to Kafka
        val reviewEvent = ReviewEvent(
            eventId = "",
            type = "review_update",
            userId = userId,
            bookId = request.bookId,
            score = request.score,
            genres = request.genres,
            reviewText = request.reviewText
        )
        reviewEventProducer.publishReviewEvent(reviewEvent)

        return mapToResponse(savedReview)
    }

    @Transactional
    fun deleteReview(userId: String, reviewId: Long) {
        logger.info("Deleting review $reviewId for user $userId")

        val review = reviewRepository.findById(reviewId)
            .orElseThrow { ResourceNotFoundException("Review not found with id: $reviewId") }

        if (review.userId != userId) {
            throw IllegalArgumentException("User does not own this review")
        }

        reviewRepository.delete(review)

        // Publish delete event to Kafka
        val reviewEvent = ReviewEvent(
            eventId = "",
            type = "review_delete",
            userId = userId,
            bookId = review.bookId,
            score = 0,
            genres = review.genres.toList(),
            reviewText = null
        )
        reviewEventProducer.publishReviewEvent(reviewEvent)
    }

    private fun mapToResponse(review: Review): ReviewResponse {
        return ReviewResponse(
            id = review.id!!,
            userId = review.userId,
            bookId = review.bookId,
            score = review.score,
            genres = review.genres.toList(),
            reviewText = review.reviewText,
            createdAt = review.createdAt
        )
    }
}