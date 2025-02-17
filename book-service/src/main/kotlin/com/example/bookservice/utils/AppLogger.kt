package com.example.bookservice.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import ch.qos.logback.classic.Level
import org.springframework.stereotype.Component

@Component
class AppLogger(private val appName: String) {
    private val logger: Logger = LoggerFactory.getLogger(appName)

    init {
        configureLogger()
    }

    private fun configureLogger() {
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        val logDir = File("../logs").apply { mkdirs() }

        // Create encoder
        val encoder = PatternLayoutEncoder().apply {
            context = loggerContext
            pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
            start()
        }

        // Configure rolling file appender for all logs
        val allLogsAppender = RollingFileAppender<ch.qos.logback.classic.spi.ILoggingEvent>().apply {
            context = loggerContext
            name = "allLogsAppender"
            file = "${logDir}/${appName}_all.log"
            encoder = encoder

            val rollingPolicy = TimeBasedRollingPolicy<ch.qos.logback.classic.spi.ILoggingEvent>().apply {
                context = loggerContext
                fileNamePattern = "${logDir}/${appName}_all.%d{yyyy-MM-dd}.log"
                maxHistory = 30
                this.parent = this@apply
                start()
            }

            rollingPolicy = rollingPolicy
            start()
        }

        // Configure rolling file appender for errors only
        val errorLogsAppender = RollingFileAppender<ch.qos.logback.classic.spi.ILoggingEvent>().apply {
            context = loggerContext
            name = "errorLogsAppender"
            file = "${logDir}/${appName}_error.log"
            encoder = encoder

            val rollingPolicy = TimeBasedRollingPolicy<ch.qos.logback.classic.spi.ILoggingEvent>().apply {
                context = loggerContext
                fileNamePattern = "${logDir}/${appName}_error.%d{yyyy-MM-dd}.log"
                maxHistory = 30
                this.parent = this@apply
                start()
            }

            rollingPolicy = rollingPolicy
            start()
        }

        // Add appenders to logger
        (logger as ch.qos.logback.classic.Logger).apply {
            addAppender(allLogsAppender)
            addAppender(errorLogsAppender)
            level = Level.INFO
        }
    }

    fun info(message: String, vararg params: Pair<String, Any?>) {
        val fullMessage = buildLogMessage(message, params)
        logger.info(fullMessage)
    }

    fun error(message: String, throwable: Throwable? = null, vararg params: Pair<String, Any?>) {
        val fullMessage = buildLogMessage(message, params)
        if (throwable != null) {
            logger.error(fullMessage, throwable)
        } else {
            logger.error(fullMessage)
        }
    }

    fun warning(message: String, vararg params: Pair<String, Any?>) {
        val fullMessage = buildLogMessage(message, params)
        logger.warn(fullMessage)
    }

    fun debug(message: String, vararg params: Pair<String, Any?>) {
        val fullMessage = buildLogMessage(message, params)
        logger.debug(fullMessage)
    }

    private fun buildLogMessage(message: String, params: Array<out Pair<String, Any?>>): String {
        return if (params.isEmpty()) message
        else "$message ${params.joinToString(", ") { "${it.first}=${it.second}" }}"
    }
}

// Annotation for logging method execution
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class LogExecution

// Aspect for handling the @LogExecution annotation
@Aspect
@Component
class LoggingAspect(private val logger: AppLogger) {

    @Around("@annotation(LogExecution)")
    fun logExecutionTime(joinPoint: ProceedingJoinPoint): Any? {
        val methodName = joinPoint.signature.name
        logger.info("Entering method: $methodName")

        try {
            val result = joinPoint.proceed()
            logger.info("Exiting method: $methodName")
            return result
        } catch (e: Exception) {
            logger.error("Exception in method: $methodName", e)
            throw e
        }
    }
}