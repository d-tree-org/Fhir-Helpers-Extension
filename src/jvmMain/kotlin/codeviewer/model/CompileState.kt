package codeviewer.model

data class CompileState(val output: String = "", val hasError: Boolean = false, val error: String = "")
