package codeviewer.ui

import codeviewer.ui.common.Settings
import codeviewer.ui.editor.Editors
import codeviewer.ui.filetree.FileTree

class CodeViewer(
    val editors: Editors,
    val fileTree: FileTree,
    val settings: Settings
)