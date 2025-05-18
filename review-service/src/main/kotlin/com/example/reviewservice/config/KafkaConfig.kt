package com.example.reviewservice.config

import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.KafkaAdmin

@Configuration
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String,

    @Value("\${kafka.topics.review-events}")
    private val reviewEventsTopic: String
) {

    @Bean
    fun kafkaAdmin(): KafkaAdmin {
        val configs = mapOf(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers
        )
        return KafkaAdmin(configs)
    }

    @Bean
    fun reviewEventsTopic(): NewTopic {
        return TopicBuilder.name(reviewEventsTopic)
            .partitions(6)
            .replicas(3)
            .configs(
                mapOf(
                    "retention.ms" to "604800000", // 7 days
                    "cleanup.policy" to "delete"
                )
            )
            .build()
    }
}