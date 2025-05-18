package com.example.reviewservice.repository

import com.example.reviewservice.model.Review
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ReviewRepository : JpaRepository<Review, Long> {
    fun findByUserIdOrderByCreatedAtDesc(userId: String, pageable: Pageable): Page<Review>

    fun findByBookIdOrderByCreatedAtDesc(bookId: String, pageable: Pageable): Page<Review>

    @Query("SELECT AVG(r.score) FROM Review r WHERE r.bookId = :bookId")
    fun calculateAverageScoreForBook(bookId: String): Double?

    fun existsByUserIdAndBookId(userId: String, bookId: String): Boolean
}