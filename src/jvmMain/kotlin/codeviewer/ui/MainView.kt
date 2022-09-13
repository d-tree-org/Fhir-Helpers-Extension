package codeviewer.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import codeviewer.ui.common.AppTheme
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import repositories.FileRepository
import repositories.LoggerRepository

@ExperimentalSplitPaneApi
@ExperimentalFoundationApi
@Composable
fun MainView(repository: FileRepository, loggerRepository: LoggerRepository, openFile: () -> Unit) {

    DisableSelection {
        MaterialTheme(
            colors = AppTheme.colors.material
        ) {
            Surface {
                CodeViewerView(
                    repository.codeViewer, loggerRepository,openFile,
                    openSideFile = {
                        repository.open(it)
                    },
                    compile = repository::compile,
                )
            }
        }
    }
}