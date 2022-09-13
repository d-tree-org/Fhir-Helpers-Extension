package codeviewer.ui.debugger

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import codeviewer.model.CompileState
import codeviewer.ui.common.AppTheme
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@ExperimentalFoundationApi
@Composable
fun BuildOutView(compileState: CompileState) {
    if (compileState.output.isNotBlank()) {
        LazyColumn {
            stickyHeader {
                TopAppBar(actions = {
                    IconButton(onClick = {
                        val selection = StringSelection(compileState.output)
                        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                        clipboard.setContents(selection, selection)
                    }) {
                        Icon(Icons.Default.CopyAll, "")
                    }
                }, title = {
                })
            }
            item {
                Text(compileState.output)
            }
        }
    } else {
        Column {
            Text("TODO", style = MaterialTheme.typography.h2)
            Text("Add Proper Styling to the json")
        }
    }
}