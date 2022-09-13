package codeviewer.ui.editor

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import codeviewer.model.CompileState
import kotlinx.coroutines.CoroutineScope
import codeviewer.platform.File
import codeviewer.util.EmptyTextLines
import codeviewer.util.SingleSelection
import java.util.UUID

data class Editor(
    val file: File,
    val id: String = UUID.randomUUID().toString(),
    private var _fileName: String = "",
    val compileState: CompileState = CompileState(),
) {
    init {
        _fileName = file.name
    }

    var selection: SingleSelection = SingleSelection()

    val isActive: Boolean
        get() = selection.selected === this

    val fileName: String
        get() = _fileName

    val lines: (backgroundScope: CoroutineScope) -> EditorLines = { backgroundScope ->
        val textLines = try {
            file.readLines(backgroundScope)
        } catch (e: Throwable) {
            e.printStackTrace()
            EmptyTextLines
        }
        val isCode = file.name.endsWith(".kt", ignoreCase = true)

        fun content(index: Int): EditorContent {
            val text = textLines.get(index)
            val state = mutableStateOf(text)
            return EditorContent(state, isCode)
        }

        object : EditorLines {
            override val size get() = textLines.size

            override fun get(index: Int) = EditorLine(
                number = index + 1,
                content = content(index)
            )
        }
    }

    fun activate() {
        selection.selected = this
    }

    fun canBuild(): Boolean {
        return fileName.endsWith(".map", ignoreCase = true)
    }
}

interface EditorLines {
    val lineNumberDigitCount: Int get() = size.toString().length
    val size: Int
    operator fun get(index: Int): EditorLine
}

class EditorLine(val number: Int, val content: EditorContent)
class EditorContent(val value: State<String>, val isCode: Boolean)