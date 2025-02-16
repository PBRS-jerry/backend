package com.example.bookservice.model

import com.example.bookservice.domain.Tag
import com.example.bookservice.utils.TagListConverter
import jakarta.persistence.*

@Entity

@Table(name = "books")
data class Book(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    val author: String,

    @Column(nullable = false)
    val year: Int,

    @Column(nullable = false)
    val genre: String,

    @Column(nullable = false)
    var isActive: Boolean = true,

    @Column(columnDefinition = "jsonb")
    @Convert(converter = TagListConverter::class)
    var tags: List<Tag> = emptyList()
)
