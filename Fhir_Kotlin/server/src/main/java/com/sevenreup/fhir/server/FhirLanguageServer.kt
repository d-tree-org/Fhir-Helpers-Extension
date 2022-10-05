package com.sevenreup.fhir.server

import com.sevenreup.fhir.compiler.LOG
import com.sevenreup.fhir.compiler.LogLevel
import com.sevenreup.fhir.compiler.LogMessage
import com.sevenreup.fhir.server.semantictokens.semanticTokensLegend
import com.sevenreup.fhir.server.utils.*
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.*
import java.io.Closeable
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture

class FhirLanguageServer : LanguageServer, LanguageClientAware, Closeable {
    val config = Configuration()
    private lateinit var client: LanguageClient
    private val textDocuments = FMLTextDocumentService()
    private val workspaces = FMLWorkspaceService()
    private val async = AsyncExecutor()
    private var progressFactory: Progress.Factory = Progress.Factory.None
    override fun connect(client: LanguageClient) {
        this.client = client
        connectLoggingBackend()

        workspaces.connect(client)
        textDocuments.connect(client)

        LOG.info("Connected to client")
    }

    override fun initialize(params: InitializeParams?): CompletableFuture<InitializeResult> = async.compute {
        val serverCapabilities = ServerCapabilities()
        serverCapabilities.setTextDocumentSync(TextDocumentSyncKind.Incremental)
        serverCapabilities.workspace = WorkspaceServerCapabilities()
        serverCapabilities.workspace.workspaceFolders = WorkspaceFoldersOptions()
        serverCapabilities.workspace.workspaceFolders.supported = true
        serverCapabilities.workspace.workspaceFolders.changeNotifications = Either.forRight(true)
        serverCapabilities.hoverProvider = Either.forLeft(true)
        serverCapabilities.renameProvider = Either.forLeft(true)
        serverCapabilities.completionProvider = CompletionOptions(false, listOf("."))
        serverCapabilities.signatureHelpProvider = SignatureHelpOptions(listOf("(", ","))
        serverCapabilities.definitionProvider = Either.forLeft(true)
        serverCapabilities.documentSymbolProvider = Either.forLeft(true)
        serverCapabilities.workspaceSymbolProvider = Either.forLeft(true)
        serverCapabilities.referencesProvider = Either.forLeft(true)
        serverCapabilities.semanticTokensProvider = SemanticTokensWithRegistrationOptions(semanticTokensLegend, true, true)
        serverCapabilities.codeActionProvider = Either.forLeft(true)
        serverCapabilities.documentFormattingProvider = Either.forLeft(true)
        serverCapabilities.documentRangeFormattingProvider = Either.forLeft(true)
        serverCapabilities.executeCommandProvider = ExecuteCommandOptions(ALL_COMMANDS)

        val clientCapabilities = params?.capabilities
        config.completion.snippets.enabled = clientCapabilities?.textDocument?.completion?.completionItem?.snippetSupport ?: false

        if (clientCapabilities?.window?.workDoneProgress == true) {
            progressFactory = LanguageClientProgress.Factory(client)
        }

        if (clientCapabilities?.textDocument?.rename?.prepareSupport == true) {
            serverCapabilities.renameProvider = Either.forRight(RenameOptions(false))
        }

        textDocuments.lintAll()

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