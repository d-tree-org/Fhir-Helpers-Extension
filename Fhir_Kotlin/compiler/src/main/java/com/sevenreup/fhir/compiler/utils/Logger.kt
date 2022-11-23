package com.sevenreup.fhir.compiler.utils

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.time.Instant
import java.util.*
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord

val LOG = Logger()

private class JULRedirector(private val downstream: Logger): Handler() {
    override fun publish(record: LogRecord) {
        when (record.level) {
            Level.SEVERE -> downstream.error(record.message)
            Level.WARNING -> downstream.warn(record.message)
            Level.INFO -> downstream.info(record.message)
            Level.CONFIG -> downstream.debug(record.message)
            Level.FINE -> downstream.trace(record.message)
            else -> downstream.deepTrace(record.message)
        }
        record.thrown?.let(downstream::printStackTrace)
    }

    override fun flush() {}

    override fun close() {}
}

enum class LogLevel(val value: Int) {
    NONE(100),
    ERROR(2),
    WARN(1),
    INFO(0),
    DEBUG(-1),
    TRACE(-2),
    DEEP_TRACE(-3),
    ALL(-100)
}

class LogMessage(
    val level: LogLevel,
    val message: String
) {
    val formatted: String
        get() = "[$level] $message"
}

class Logger {
    private var outBackend: ((LogMessage) -> Unit)? = null
    private var errBackend: ((LogMessage) -> Unit)? = null
    private val outQueue: Queue<LogMessage> = ArrayDeque()
    private val errQueue: Queue<LogMessage> = ArrayDeque()
    private val errStream = DelegatePrintStream { logError(LogMessage(LogLevel.ERROR, it.trimEnd())) }
    val outStream = DelegatePrintStream { log(LogMessage(LogLevel.INFO, it.trimEnd())) }

    private val newline = System.lineSeparator()
    val logTime = false
    var level = LogLevel.INFO

    fun logError(msg: LogMessage) {
        if (errBackend == null) {
            errQueue.offer(msg)
        } else {
            errBackend?.invoke(msg)
        }
    }

    fun log(msg: LogMessage) {
        if (outBackend == null) {
            outQueue.offer(msg)
        } else {
            outBackend?.invoke(msg)
        }
    }

    private fun logWithPlaceholdersAt(msgLevel: LogLevel, msg: String, placeholders: Array<out Any?>) {
        if (level.value <= msgLevel.value) {
            log(LogMessage(msgLevel, format(insertPlaceholders(msg, placeholders))))
        }
    }

    inline fun logWithLambdaAt(msgLevel: LogLevel, msg: () -> String) {
        if (level.value <= msgLevel.value) {
            log(LogMessage(msgLevel, msg()))
        }
    }

    fun printStackTrace(throwable: Throwable) = throwable.printStackTrace(errStream)

    // Convenience logging methods using the traditional placeholder syntax

    fun error(msg: String, vararg placeholders: Any?) = logWithPlaceholdersAt(LogLevel.ERROR, msg, placeholders)

    fun warn(msg: String, vararg placeholders: Any?) = logWithPlaceholdersAt(LogLevel.WARN, msg, placeholders)

    fun info(msg: String, vararg placeholders: Any?) = logWithPlaceholdersAt(LogLevel.INFO, msg, placeholders)

    fun debug(msg: String, vararg placeholders: Any?) = logWithPlaceholdersAt(LogLevel.DEBUG, msg, placeholders)

    fun trace(msg: String, vararg placeholders: Any?) = logWithPlaceholdersAt(LogLevel.TRACE, msg, placeholders)

    fun deepTrace(msg: String, vararg placeholders: Any?) = logWithPlaceholdersAt(LogLevel.DEEP_TRACE, msg, placeholders)

    // Convenience logging methods using inlined lambdas

    inline fun error(msg: () -> String) = logWithLambdaAt(LogLevel.ERROR, msg)

    inline fun warn(msg: () -> String) = logWithLambdaAt(LogLevel.WARN, msg)

    inline fun info(msg: () -> String) = logWithLambdaAt(LogLevel.INFO, msg)

    inline fun debug(msg: () -> String) = logWithLambdaAt(LogLevel.DEBUG, msg)

    inline fun trace(msg: () -> String) = logWithLambdaAt(LogLevel.TRACE, msg)

    inline fun deepTrace(msg: () -> String) = logWithLambdaAt(LogLevel.DEEP_TRACE, msg)

    fun connectJULFrontend() {
        val rootLogger = java.util.logging.Logger.getLogger("")
        rootLogger.addHandler(JULRedirector(this))
    }

    fun connectOutputBackend(outBackend: (LogMessage) -> Unit) {
        this.outBackend = outBackend
        flushOutQueue()
    }

    fun connectErrorBackend(errBackend: (LogMessage) -> Unit) {
        this.errBackend = errBackend
        flushErrQueue()
    }

    fun connectStdioBackend() {
        connectOutputBackend { println(it.formatted) }
        connectOutputBackend { System.err.println(it.formatted) }
    }

    private fun insertPlaceholders(msg: String, placeholders: Array<out Any?>): String {
        val msgLength = msg.length
        val lastIndex = msgLength - 1
        var charIndex = 0
        var placeholderIndex = 0
        var result = StringBuilder()

        while (charIndex < msgLength) {
            val currentChar = msg.get(charIndex)
            val nextChar = if (charIndex != lastIndex) msg.get(charIndex + 1) else '?'
            if ((placeholderIndex < placeholders.size) && (currentChar == '{') && (nextChar == '}')) {
                result.append(placeholders[placeholderIndex] ?: "null")
                placeholderIndex += 1
                charIndex += 2
            } else {
                result.append(currentChar)
                charIndex += 1
            }
        }

        return result.toString()
    }

    private fun flushOutQueue() {
        while (outQueue.isNotEmpty()) {
            outBackend?.invoke(outQueue.poll())
        }
    }

    private fun flushErrQueue() {
        while (errQueue.isNotEmpty()) {
            errBackend?.invoke(errQueue.poll())
        }
    }

    private fun format(msg: String): String {
        val time = if (logTime) "${Instant.now()} " else ""
        var thread = Thread.currentThread().name

        return time + shortenOrPad(thread, 10) + msg.trimEnd()
    }

    private fun shortenOrPad(str: String, length: Int): String =
        if (str.length <= length) {
            str.padEnd(length, ' ')
        } else {
            ".." + str.substring(str.length - length + 2)
        }
}

class DelegatePrintStream(private val delegate: (String) -> Unit): PrintStream(ByteArrayOutputStream(0)) {
    private val newLine = System.lineSeparator()

    override fun write(c: Int) = delegate((c.toChar()).toString())

    override fun write(buf: ByteArray, off: Int, len: Int) {
        if (len > 0 && buf.size > 0) {
            delegate(String(buf, off, len))
        }
    }

    override fun append(csq: CharSequence): PrintStream {
        delegate(csq.toString())
        return this
    }

    override fun append(csq: CharSequence, start: Int, end: Int): PrintStream {
        delegate(csq.subSequence(start, end).toString())
        return this
    }

    override fun append(c:Char): PrintStream {
        delegate((c).toString())
        return this
    }

    override fun print(x: Boolean) = delegate(x.toString())

    override fun print(x: Char) = delegate(x.toString())

    override fun print(x: Int) = delegate(x.toString())

    override fun print(x: Long) = delegate(x.toString())

    override fun print(x: Float) = delegate(x.toString())

    override fun print(x: Double) = delegate(x.toString())

    override fun print(s: CharArray) = delegate(String(s))

    override fun print(s: String) = delegate(s)

    override fun print(obj: Any) = delegate(obj.toString())

    override fun println() = delegate(newLine)

    override fun println(x: Boolean) = delegate(x.toString() + newLine)

    override fun println(x: Char) = delegate(x.toString() + newLine)

    override fun println(x: Int) = delegate(x.toString() + newLine)

    override fun println(x: Long) = delegate(x.toString() + newLine)

    override fun println(x: Float) = delegate(x.toString() + newLine)

    override fun println(x: Double) = delegate(x.toString() + newLine)

    override fun println(x: CharArray) = delegate(String(x) + newLine)

    override fun println(x: String) = delegate(x + newLine)

    override fun println(x: Any) = delegate(x.toString() + newLine)
}
