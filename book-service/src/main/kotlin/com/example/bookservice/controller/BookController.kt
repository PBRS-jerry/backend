package com.example.bookservice.controller

import com.example.bookservice.model.Book
import com.example.bookservice.service.BookService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/book-service")
class BookController(private val bookService: BookService) {

    @GetMapping("/public/books")
    fun getBooks(): List<Book?>? {
        return bookService.getBooks()
    }

    @GetMapping("/public/{id}")
    fun getBook(@PathVariable id: Long): ResponseEntity<Book?> {
        val book = bookService.getBook(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(book)
    }

    @GetMapping("/public/search")
    fun searchBooks(
        @RequestParam(required = false) query: String?,
        @RequestParam(required = false) genre: String?,
        @RequestParam(required = false) year: Int?,
        pageable: Pageable
    ): Page<Book> {
        return bookService.searchBooks(query, genre, year, pageable)
    }

    @PostMapping("/protected")
    fun createBook(@RequestBody book: Book): ResponseEntity<Book> {
        return ResponseEntity.ok(bookService.createBook(book))
    }

//    @PutMapping("/protected/{id}")
//    fun updateBook(@PathVariable id: Long, @RequestBody updatedBook: Book): ResponseEntity<Book?> {
//        val book = bookService.updateBook(id, updatedBook) ?: return ResponseEntity.notFound().build()
//        return ResponseEntity.ok(book)
//    }

    @DeleteMapping("/protected/{id}")
    fun deleteBook(@PathVariable id: Long): ResponseEntity<Void> {
        bookService.deleteBook(id)
        return ResponseEntity.noContent().build()
    }
}