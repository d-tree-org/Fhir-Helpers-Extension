package com.sevenreup.fhir.server

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.sevenreup.fhir.compiler.LOG
import com.sevenreup.fhir.server.utils.ExitingInputStream
import com.sevenreup.fhir.server.utils.tcpConnectToClient
import com.sevenreup.fhir.server.utils.tcpStartServer
import org.eclipse.lsp4j.launch.LSPLauncher
import java.util.concurrent.Executors

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
    val (inStream, outStream) = args.tcpClientPort?.let {
        // Launch as TCP Client
        LOG.connectStdioBackend()
        tcpConnectToClient(args.tcpClientHost, it)
    } ?: args.tcpServerPort?.let {
        // Launch as TCP Server
        LOG.connectStdioBackend()
        tcpStartServer(it)
    } ?: Pair(System.`in`, System.out)

    val server = FhirLanguageServer()
    val threads = Executors.newSingleThreadExecutor { Thread(it, "client") }
    val launcher = LSPLauncher.createServerLauncher(server, ExitingInputStream(inStream), outStream, threads) { it }

    server.connect(launcher.remoteProxy)
    launcher.startListening()
}