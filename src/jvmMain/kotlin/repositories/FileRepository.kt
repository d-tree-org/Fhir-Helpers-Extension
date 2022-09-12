package repositories

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import codeviewer.platform.File
import codeviewer.platform.HomeFolder
import codeviewer.ui.CodeViewer
import codeviewer.ui.common.Settings
import codeviewer.ui.editor.Editors
import codeviewer.ui.filetree.FileTree

class FileRepository {
    val settings by mutableStateOf(Settings())
    private val editors = Editors()
    val codeViewer by mutableStateOf(CodeViewer(
        editors = editors,
        fileTree = FileTree(HomeFolder, editors),
        settings = Settings()
    ))

    fun open(file: File) {
        editors.open(file)
    }
}