package com.sevenreup.fhir.lisp

import org.eclipse.lsp4j.DidChangeNotebookDocumentParams
import org.eclipse.lsp4j.DidCloseNotebookDocumentParams
import org.eclipse.lsp4j.DidOpenNotebookDocumentParams
import org.eclipse.lsp4j.DidSaveNotebookDocumentParams
import org.eclipse.lsp4j.services.NotebookDocumentService

class FhirNotebookDocumentService : NotebookDocumentService {
    override fun didOpen(params: DidOpenNotebookDocumentParams?) {
        TODO("Not yet implemented")
    }

    override fun didChange(params: DidChangeNotebookDocumentParams?) {
        TODO("Not yet implemented")
    }

    override fun didSave(params: DidSaveNotebookDocumentParams?) {
        TODO("Not yet implemented")
    }

    override fun didClose(params: DidCloseNotebookDocumentParams?) {
        TODO("Not yet implemented")
    }
}