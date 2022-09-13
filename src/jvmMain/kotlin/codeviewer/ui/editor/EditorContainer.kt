package codeviewer.ui.editor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import codeviewer.ui.common.Settings

@ExperimentalFoundationApi
@Composable
fun EditorContainer(model: Editor, settings: Settings) {

    Row(modifier = Modifier.fillMaxSize()) {
        EditorView(model, settings)
        Column(modifier = Modifier.fillMaxHeight()) {
            Text("This is the stuff")
        }
    }

}