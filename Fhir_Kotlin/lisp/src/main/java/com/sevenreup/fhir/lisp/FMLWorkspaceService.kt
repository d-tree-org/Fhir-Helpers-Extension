package com.sevenreup.fhir.lisp

import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.DidChangeWatchedFilesParams
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageClientAware
import org.eclipse.lsp4j.services.WorkspaceService

class FMLWorkspaceService: WorkspaceService, LanguageClientAware {
    private var languageClient: LanguageClient? = null
    override fun didChangeConfiguration(params: DidChangeConfigurationParams?) {
        TODO("Not yet implemented")
    }

    override fun didChangeWatchedFiles(params: DidChangeWatchedFilesParams?) {
        TODO("Not yet implemented")
    }

    override fun connect(client: LanguageClient?) {
        languageClient = client
    }
}