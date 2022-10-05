package com.sevenreup.fhir.server

import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DidSaveTextDocumentParams
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.TextDocumentService
import java.io.Closeable

class FMLTextDocumentService: TextDocumentService, Closeable {
    private lateinit var client: LanguageClient

    override fun didOpen(params: DidOpenTextDocumentParams?) {
        TODO("Not yet implemented")
    }

    override fun didChange(params: DidChangeTextDocumentParams?) {
        TODO("Not yet implemented")
    }

    override fun didClose(params: DidCloseTextDocumentParams?) {
        TODO("Not yet implemented")
    }

    override fun didSave(params: DidSaveTextDocumentParams?) {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    fun connect(client: LanguageClient) {
        this.client = client
    }
}