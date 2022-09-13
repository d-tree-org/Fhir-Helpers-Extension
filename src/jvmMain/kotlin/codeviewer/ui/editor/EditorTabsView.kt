package codeviewer.ui.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import codeviewer.ui.common.AppTheme

@Composable
fun EditorTabsView(model: Editors, compile: () -> Unit) {
    val activeTab = model.active

    Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        LazyRow(modifier = Modifier.weight(1f)) {
            items(model.editors) { editor ->
                EditorTabView(editor)
            }
        }
        EditorBuildTools(activeTab, compile)
    }
}

@Composable
fun EditorBuildTools(activeEditor: Editor?, compile: () -> Unit) {
    var canBuild by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(activeEditor) {
        canBuild = activeEditor?.canBuild() ?: false
    }

    Row {
        Surface(color = AppTheme.colors.backgroundMedium) {
            IconButton(
                compile,
                enabled = canBuild
            ) {
                Icon(
                    imageVector = Icons.Default.Build, contentDescription = "",
                    modifier = Modifier.size(24.dp)
                        .padding(4.dp),
                )
            }
        }
    }
}

@Composable
fun EditorTabView(model: Editor) = Surface(
    color = if (model.isActive) {
        AppTheme.colors.backgroundDark
    } else {
        Color.Transparent
    }
) {
    Row(
        Modifier
            .clickable(remember(::MutableInteractionSource), indication = null) {
                model.activate()
            }
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            model.fileName,
            color = LocalContentColor.current,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        val close = model.close

        if (close != null) {
            Icon(
                Icons.Default.Close,
                tint = LocalContentColor.current,
                contentDescription = "Close",
                modifier = Modifier
                    .size(24.dp)
                    .padding(4.dp)
                    .clickable {
                        close()
                    }
            )
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp, 24.dp)
                    .padding(4.dp)
            )
        }
    }
}
