package com.example.bookservice.service

import com.example.bookservice.model.Book
import com.example.bookservice.repository.BookRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class BookService(private val bookRepository: BookRepository) {

    fun getBooks(): List<Book?>? {
        return bookRepository.findAllBooksWithoutRelations()
    }

    fun getBook(id: Long): Book? {
        return bookRepository.findById(id).orElse(null)
    }

    fun searchBooks(query: String?, genre: String?, year: Int?, pageable: Pageable): Page<Book> {
        return bookRepository.searchBooks(query, genre, year, pageable)
    }

    fun createBook(book: Book): Book {
        return bookRepository.save(book)
    }

//    fun updateBook(id: Long, updatedBook: Book): Book? {
//        val book = bookRepository.findById(id).orElse(null) ?: return null
//        book.title = updatedBook.title
//        book.author = updatedBook.author
//        book.genre = updatedBook.genre
//        book.year = updatedBook.year
//        return bookRepository.save(book)
//    }

    fun deleteBook(id: Long) {
        bookRepository.deleteById(id)
    }
}