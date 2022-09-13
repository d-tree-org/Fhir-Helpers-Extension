package codeviewer.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import codeviewer.ui.editor.EditorEmptyView
import codeviewer.ui.editor.EditorTabsView
import codeviewer.ui.editor.EditorView
import codeviewer.ui.filetree.FileTreeView
import codeviewer.ui.filetree.FileTreeViewTabView
import codeviewer.ui.statusbar.StatusBar
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.VerticalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import java.awt.Cursor

private fun Modifier.cursorForHorizontalResize(): Modifier =
    pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))

@ExperimentalFoundationApi
@ExperimentalSplitPaneApi
@Composable
fun CodeViewerView(
    model: CodeViewer,
    openFile: () -> Unit,
    openSideFile: (codeviewer.platform.File) -> Unit,
    compile: () -> Unit
) {

    val splitterState = rememberSplitPaneState(initialPositionPercentage = 0.2f)
    val hSplitterState = rememberSplitPaneState(initialPositionPercentage = 0.9f)
    HorizontalSplitPane(
        splitPaneState = splitterState
    ) {
        first(20.dp) {
            Column {
                FileTreeViewTabView()
                FileTreeView(model.fileTree, openSideFile)
            }
        }
        second(50.dp) {
            VerticalSplitPane(splitPaneState = hSplitterState) {
                first(50.dp) {
                    Box() {
                        if (model.editors.active != null) {
                            Column(Modifier) {
                                EditorTabsView(model.editors, compile)
                                Box(Modifier.weight(1f)) {
                                    EditorView(model.editors.active!!, model.settings)
                                }
                                StatusBar(model.settings)
                            }
                        } else {
                            EditorEmptyView(openFile)
                        }
                    }
                }
                second(20.dp) {
                    Box(Modifier.background(Color.Blue).fillMaxSize())
                }
            }
        }
        splitter {
            visiblePart {
                Box(
                    Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colors.background)
                )
            }
            handle {
                Box(
                    Modifier
                        .markAsHandle()
                        .cursorForHorizontalResize()
                        .background(SolidColor(Color.Gray), alpha = 0.50f)
                        .width(9.dp)
                        .fillMaxHeight()
                )
            }
        }
    }
}