package logging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.OutputStream
import java.lang.IllegalArgumentException

class LoggingOutputStream(
    private val logChannel: Channel<String>,
    private val scope: CoroutineScope
) : OutputStream() {
    /**
     * Indicates stream state.
     */
    private var hasBeenClosed = false

    /**
     * Internal buffer where data is stored.
     */
    private var buf: ByteArray

    /**
     * The number of valid bytes in the buffer.
     */
    private var count: Int

    /**
     * Remembers the size of the buffer.
     */
    private var curBufLength: Int

    /**
     * Creates the Logging instance to flush to the given logger.
     *
     * @param log         the Logger to write to
     * @param level       the log level
     * @throws IllegalArgumentException in case if one of arguments is  null.
     */
    init {
        curBufLength = DEFAULT_BUFFER_LENGTH
        buf = ByteArray(curBufLength)
        count = 0
    }

    /**
     * Writes the specified byte to this output stream.
     *
     * @param b the byte to write
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    override fun write(b: Int) {
        if (hasBeenClosed) {
            throw IOException("The stream has been closed.")
        }
        // don't log nulls
        if (b == 0) {
            return
        }
        // would this be writing past the buffer?
        if (count == curBufLength) {
            // grow the buffer
            val newBufLength = curBufLength +
                    DEFAULT_BUFFER_LENGTH
            val newBuf = ByteArray(newBufLength)
            System.arraycopy(buf, 0, newBuf, 0, curBufLength)
            buf = newBuf
            curBufLength = newBufLength
        }
        buf[count] = b.toByte()
        count++
    }

    /**
     * Flushes this output stream and forces any buffered output
     * bytes to be written out.
     */
    override fun flush() {
        if (count == 0) {
            return
        }
        val bytes = ByteArray(count)
        System.arraycopy(buf, 0, bytes, 0, count)
        val str = String(bytes)
        scope.launch(Dispatchers.IO) {
            logChannel.send(str)
        }
        count = 0
    }

    /**
     * Closes this output stream and releases any system resources
     * associated with this stream.
     */
    override fun close() {
        flush()
        hasBeenClosed = true
        logChannel.close()
    }

    companion object {
        /**
         * Default number of bytes in the buffer.
         */
        private const val DEFAULT_BUFFER_LENGTH = 2048
    }
}