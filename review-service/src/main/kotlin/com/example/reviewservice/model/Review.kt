package com.example.reviewservice.model

import com.vladmihalcea.hibernate.type.array.StringArrayType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import java.time.ZonedDateTime

@Entity
@Table(name = "reviews")
data class Review(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: String,

    @Column(name = "book_id", nullable = false)
    val bookId: String,

    @Column(nullable = false)
    val score: Int,

    @Type(value = StringArrayType::class)
    @Column(name = "genres", columnDefinition = "text[]")
    val genres: Array<String>,

    @Column(name = "review_text")
    val reviewText: String? = null,

    @Column(name = "created_at")
    val createdAt: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: ZonedDateTime = ZonedDateTime.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Review

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (bookId != other.bookId) return false
        if (!genres.contentEquals(other.genres)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + userId.hashCode()
        result = 31 * result + bookId.hashCode()
        result = 31 * result + genres.contentHashCode()
        return result
    }
}