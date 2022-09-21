package com.sevenreup.fhir.server.utils

import com.sevenreup.fhir.compiler.LOG
import java.io.InputStream

class ExitingInputStream(private val delegate: InputStream): InputStream() {
    override fun read(): Int = exitIfNegative { delegate.read() }

    override fun read(b: ByteArray): Int = exitIfNegative { delegate.read(b) }

    override fun read(b: ByteArray, off: Int, len: Int): Int = exitIfNegative { delegate.read(b, off, len) }

    private fun exitIfNegative(call: () -> Int): Int {
        val result = call()

        if (result < 0) {
            LOG.info("System.in has closed, exiting")
            System.exit(0)
        }

        return result
    }
}
