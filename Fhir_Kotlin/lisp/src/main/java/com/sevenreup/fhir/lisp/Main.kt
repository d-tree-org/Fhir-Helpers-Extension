package com.sevenreup.fhir.lisp

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.sevenreup.fhir.compiler.utils.LOG
import org.eclipse.lsp4j.jsonrpc.Launcher
import org.eclipse.lsp4j.launch.LSPLauncher
import org.eclipse.lsp4j.services.LanguageClient
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket


class Args {
    @Parameter(names = ["--tcpServerPort", "-sp"])
    var tcpServerPort: Int? = null
    @Parameter(names = ["--tcpClientPort", "-p"])
    var tcpClientPort: Int? = null
    @Parameter(names = ["--tcpClientHost", "-h"])
    var tcpClientHost: String = "localhost"
}

fun main(argv: Array<String>) {
    LOG.connectJULFrontend()

    val args = Args().also { JCommander.newBuilder().addObject(it).build().parse(*argv) }
//    try {
//        val (inStream, outStream) = args.tcpClientPort?.let {
//            // Launch as TCP Client
//            LOG.connectStdioBackend()
//            tcpConnectToClient(args.tcpClientHost, it)
//        } ?: args.tcpServerPort?.let {
//            // Launch as TCP Server
//            LOG.connectStdioBackend()
//            tcpStartServer(it)
//        } ?: Pair(System.`in`, System.out)
//
//        val server = FhirLanguageServer()
//        val threads = Executors.newSingleThreadExecutor { Thread(it, "client") }
//        val launcher = LSPLauncher.createServerLauncher(server, ExitingInputStream(inStream), outStream, threads) { it }
//
//        server.connect(launcher.remoteProxy)
//        launcher.startListening()
//    } catch (e: Exception) {
//        e.printStackTrace()
//    }

    try {
        val socket = Socket("localhost", args.tcpServerPort ?: 9090)
        val serverSocket = ServerSocket()
       val launcher = SocketLauncher()
        val socketIn: InputStream = socket.getInputStream()
        val socketOut: OutputStream = socket.getOutputStream()
        val server = FhirLanguageServer()
        val launcher: Launcher<LanguageClient> = LSPLauncher.createServerLauncher(server, socketIn, socketOut)
        val client: LanguageClient = launcher.remoteProxy
        server.connect(client)
        launcher.startListening()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}