package codeviewer.ui

import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import codeviewer.platform.HomeFolder
import codeviewer.ui.common.AppTheme
import codeviewer.ui.common.Settings
import codeviewer.ui.editor.Editors
import codeviewer.ui.filetree.FileTree
import repositories.FileRepository

@Composable
fun MainView(repository: FileRepository,openFile: () -> Unit) {

    DisableSelection {
        MaterialTheme(
            colors = AppTheme.colors.material
        ) {
            Surface {
                CodeViewerView(repository.codeViewer, openFile) {
                    repository.open(it)
                }
            }
        }
    }
}