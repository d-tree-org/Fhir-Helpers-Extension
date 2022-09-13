package codeviewer.ui

import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import codeviewer.ui.common.AppTheme
import repositories.FileRepository

@Composable
fun MainView(repository: FileRepository, openFile: () -> Unit) {

    DisableSelection {
        MaterialTheme(
            colors = AppTheme.colors.material
        ) {
            Surface {
                CodeViewerView(
                    repository.codeViewer, openFile,
                    openSideFile = {
                        repository.open(it)
                    },
                    compile = repository::compile,
                )
            }
        }
    }
}