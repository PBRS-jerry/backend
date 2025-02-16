package com.example.bookservice.repository

import com.example.bookservice.model.Book
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface BookRepository : JpaRepository<Book, Long> {
//    fun findByTitle(title: String): Book?

    // If you need to explicitly control what gets fetched, you can add:
    @Query("SELECT b FROM Book b")
    fun findAllBooksWithoutRelations(): List<Book?>?

    @Query("""
        SELECT b FROM Book b 
        WHERE (:query IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) 
               OR LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')))
        AND (:genre IS NULL OR LOWER(b.genre) = LOWER(:genre))
        AND (:year IS NULL OR b.year = :year)
    """)
    fun searchBooks(
        @Param("query") query: String?,
        @Param("genre") genre: String?,
        @Param("year") year: Int?,
        pageable: Pageable
    ): Page<Book>
}