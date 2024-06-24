package com.sevenreup.fhir.core.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import java.lang.Exception

private val logger = KotlinLogging.logger {}


object Logger {
    fun debug(message: String) {
        logger.debug { formatMessage(LogLevel.DEBUG, message) }
    }

    fun info(message: String) {
        logger.info { formatMessage(LogLevel.INFO, message) }
    }

    fun warn(message: String) {
        logger.warn { formatMessage(LogLevel.WARNING, message) }
    }

    fun error(message: String) {
        logger.error { formatMessage(LogLevel.ERROR, message) }
    }

    fun error(exception: Exception) {
        logger.error { formatMessage(LogLevel.ERROR, exception.message ?: exception.toString()) }
    }

    private fun formatMessage(level: LogLevel, message: String): String {
        val color = colorFromLogLevel(level)
        val levelTag = level.tag.colorize(color, bold = true)
        return "$levelTag ${message.colorize(color)}"
    }

    private fun colorFromLogLevel(level: LogLevel): Color {
        return when (level) {
            LogLevel.DEBUG -> Color.CYAN
            LogLevel.INFO -> Color.GREEN
            LogLevel.WARNING -> Color.YELLOW
            else -> Color.RED
        }
    }

    private fun String.colorize(color: Color, bold: Boolean = false): String {
        val style = if (bold) "1;" else ""
        return "\u001B[${style}${color.code}m$this\u001B[0m"
    }

}

enum class LogLevel(val tag: String) {
    DEBUG("DEBUG"),
    INFO("INFO"),
    WARNING("WARN"),
    ERROR("ERROR")
}

enum class Color(val code: Int) {
    RED(31),
    GREEN(32),
    YELLOW(33),
    BLUE(34),
    MAGENTA(35),
    CYAN(36),
    WHITE(37)
}