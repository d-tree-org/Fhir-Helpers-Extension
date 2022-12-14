package com.sevenreup.fhir.lisp.utils

import com.sevenreup.fhir.compiler.utils.LOG
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.ServerSocket

/**
 * Starts a TCP server socket. Blocks until the first
 * client has connected, then returns a pair of IO streams.
 */
fun tcpStartServer(port: Int): Pair<InputStream, OutputStream> = ServerSocket(port)
    .also { LOG.info("Waiting for client on port {}...", port) }
    .accept()
    .let { Pair(it.inputStream, it.outputStream) }

/**
 * Starts a TCP client socket and connects to the client at
 * the specified address, then returns a pair of IO streams.
 */
fun tcpConnectToClient(host: String, port: Int): Pair<InputStream, OutputStream> =
    run { LOG.info("Connecting to client at {}:{}...", host, port) }
    .let { Socket(host, port) }
    .let { Pair(it.inputStream, it.outputStream) }
