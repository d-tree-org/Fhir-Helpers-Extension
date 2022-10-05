package com.sevenreup.fhir.server

import com.sevenreup.fhir.compiler.LOG
import com.sevenreup.fhir.compiler.LogLevel
import com.sevenreup.fhir.compiler.LogMessage
import com.sevenreup.fhir.server.utils.AsyncExecutor
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.*
import java.io.Closeable
import java.util.concurrent.CompletableFuture

class FhirLanguageServer : LanguageServer, LanguageClientAware, Closeable {
    private lateinit var client: LanguageClient
    private val textDocuments = FMLTextDocumentService()
    private val workspaces = FMLWorkspaceService()
    private val async = AsyncExecutor()
    override fun connect(client: LanguageClient) {
        this.client = client
        connectLoggingBackend()

        textDocuments.connect(client)

        LOG.info("Connected to client")
    }

    override fun initialize(params: InitializeParams?): CompletableFuture<InitializeResult> = async.compute {
        val serverCapabilities = ServerCapabilities()

        val serverInfo = ServerInfo("Fhir Mapping Language (FML) Language Server", VERSION)

        InitializeResult(serverCapabilities, serverInfo)
    }

    override fun getNotebookDocumentService(): NotebookDocumentService {
        return FhirNotebookDocumentService()
    }

    override fun shutdown(): CompletableFuture<Any> {
        close()
        return CompletableFuture.completedFuture(null)
    }

    override fun exit() {

    }

    override fun getTextDocumentService(): TextDocumentService = textDocuments

    override fun getWorkspaceService(): WorkspaceService = workspaces

    override fun close() {
        textDocuments.close()
        async.shutdown(awaitTermination = true)
    }

    private fun connectLoggingBackend() {
        val backend: (LogMessage) -> Unit = {
            client.logMessage(MessageParams().apply {
                type = it.level.toLSPMessageType()
                message = it.message
            })
        }
        LOG.connectOutputBackend(backend)
        LOG.connectErrorBackend(backend)
    }

    private fun LogLevel.toLSPMessageType(): MessageType = when (this) {
        LogLevel.ERROR -> MessageType.Error
        LogLevel.WARN -> MessageType.Warning
        LogLevel.INFO -> MessageType.Info
        else -> MessageType.Log
    }

    init {
        LOG.info("Kotlin Language Server: Version ${VERSION ?: "?"}")
    }

    companion object {
        val VERSION: String? = System.getProperty("kotlinLanguageServer.version")
    }
}