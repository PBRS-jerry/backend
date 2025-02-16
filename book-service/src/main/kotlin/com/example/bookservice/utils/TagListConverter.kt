package com.example.bookservice.utils

import com.example.bookservice.domain.Tag
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class TagListConverter : AttributeConverter<List<Tag>, String> {
    private val objectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: List<Tag>?): String {
        return try {
            objectMapper.writeValueAsString(attribute ?: emptyList<Tag>())
        } catch (e: Exception) {
            "[]"  // Return empty array in case of error
        }
    }

    override fun convertToEntityAttribute(dbData: String?): List<Tag> {
        return try {
            if (dbData.isNullOrEmpty()) {
                emptyList()
            } else {
                objectMapper.readValue(dbData,
                    objectMapper.typeFactory.constructCollectionType(List::class.java, Tag::class.java))
            }
        } catch (e: Exception) {
            emptyList()  // Return empty list in case of error
        }
    }
}