package com.example.bookservice.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.classic.Level
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy
import org.springframework.stereotype.Component

data class LoggerConfig(
    val logDir: String = "../logs",
    val maxHistory: Int = 30,
    val logLevel: Level = Level.INFO,
    val pattern: String = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n",
    val maxFileSizeMB: Int = 10,
    val enableConsoleLogging: Boolean = true
)

@Component
class AppLogger(
    private val appName: String = "Book-Service",
    private val config: LoggerConfig = LoggerConfig()
) {
    private val logger: Logger = LoggerFactory.getLogger(appName)

    init {
        configureLogger()
    }

    private fun configureLogger() {
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        val logDir = File(config.logDir).apply { mkdirs() }

        // Create encoder with configurable pattern
        val encoder = PatternLayoutEncoder().apply {
            context = loggerContext
            pattern = config.pattern
            start()
        }

        // Configure rolling file appender for all logs
        val allLogsAppender = createRollingAppender(
            loggerContext,
            encoder,
            "${logDir.path}/${appName}_all",
            "allLogsAppender"
        )

        // Configure rolling file appender for errors only
        val errorLogsAppender = createRollingAppender(
            loggerContext,
            encoder,
            "${logDir.path}/${appName}_error",
            "errorLogsAppender"
        )

        // Configure logger
        (logger as ch.qos.logback.classic.Logger).apply {
            addAppender(allLogsAppender)
            addAppender(errorLogsAppender)
            level = config.logLevel

            // Add console appender if enabled
            if (config.enableConsoleLogging) {
                val consoleAppender = ch.qos.logback.core.ConsoleAppender<ch.qos.logback.classic.spi.ILoggingEvent>().apply {
                    context = loggerContext
                    name = "consoleAppender"
                    this.encoder = encoder
                    start()
                }
                addAppender(consoleAppender)
            }
        }
    }

    private fun createRollingAppender(
        context: LoggerContext,
        encoder: PatternLayoutEncoder,
        baseFilePath: String,
        appenderName: String
    ): RollingFileAppender<ch.qos.logback.classic.spi.ILoggingEvent> {
        // Create the appender
        val appender = RollingFileAppender<ch.qos.logback.classic.spi.ILoggingEvent>()
        appender.context = context
        appender.name = appenderName
        appender.file = "$baseFilePath.log"
        appender.encoder = encoder

        // Create and configure the rolling policy
        val rollingPolicy = SizeAndTimeBasedRollingPolicy<ch.qos.logback.classic.spi.ILoggingEvent>()
        rollingPolicy.context = context
        rollingPolicy.fileNamePattern = "$baseFilePath.%d{yyyy-MM-dd}.%i.log"
        rollingPolicy.maxHistory = config.maxHistory
        rollingPolicy.setMaxFileSize(ch.qos.logback.core.util.FileSize.valueOf("${config.maxFileSizeMB}MB"))
        rollingPolicy.setTotalSizeCap(ch.qos.logback.core.util.FileSize.valueOf("${config.maxFileSizeMB * 10}MB"))
        rollingPolicy.setParent(appender)

        // Start the components
        rollingPolicy.start()
        appender.rollingPolicy = rollingPolicy
        appender.start()

        return appender
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