package com.example.reviewservice.event

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ReviewEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, ReviewEvent>,
    @Value("\${kafka.topics.review-events}")
    private val topicName: String
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun publishReviewEvent(event: ReviewEvent) {
        val eventId = UUID.randomUUID().toString()
        val finalEvent = event.copy(eventId = eventId)

        logger.info("Publishing review event to topic $topicName: $finalEvent")

        kafkaTemplate.send(topicName, finalEvent.userId, finalEvent)
            .whenComplete{result, ex ->
                if (ex != null) {
                    logger.error("Failed to send event: ${ex.message}", ex)
                } else {
                    logger.debug("Successfully sent event: ${result?.recordMetadata}")
                }
            }
    }
}