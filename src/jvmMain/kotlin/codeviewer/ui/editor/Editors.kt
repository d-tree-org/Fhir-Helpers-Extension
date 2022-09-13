package codeviewer.ui.editor

import androidx.compose.runtime.mutableStateListOf
import codeviewer.platform.File
import codeviewer.util.SingleSelection

class Editors {
    private val selection = SingleSelection()

    var editors = mutableStateListOf<Editor>()
        private set

    val active: Editor? get() = selection.selected as Editor?

    fun open(file: File) {
        val editor = Editor(file)
        editor.selection = selection
        editors.add(editor)
        editor.activate()
    }

    fun close(editor: Editor) {
        val index = editors.indexOf(editor)
        editors.remove(editor)
        if (editor.isActive) {
            selection.selected = editors.getOrNull(index.coerceAtMost(editors.lastIndex))
        }
    }

    fun change(editor: Editor) {
        val old = editors.find { x -> x.id == editor.id }

        if (old != null) {
            val index = editors.indexOf(old)
            editors.remove(old)
            editors.add(editor)
            selection.selected = editor
        }
    }
}