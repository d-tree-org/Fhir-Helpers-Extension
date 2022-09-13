package codeviewer.ui.editor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import codeviewer.platform.VerticalScrollbar
import codeviewer.ui.common.AppTheme
import codeviewer.ui.common.Fonts
import codeviewer.ui.common.Settings
import codeviewer.ui.debugger.BuildOutView
import codeviewer.util.loadableScoped
import codeviewer.util.withoutWidthConstraints
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import kotlin.text.Regex.Companion.fromLiteral

@ExperimentalFoundationApi
@ExperimentalSplitPaneApi
@Composable
fun EditorView(model: Editor, settings: Settings) {
    val splitterState = rememberSplitPaneState(initialPositionPercentage = 0.9f)

    HorizontalSplitPane(
        splitPaneState = splitterState
    ) {
        first {
            EditorContainerHolder(model, settings)
        }
        second {
            BuildOutView(model.compileState)
        }
    }
}

@Composable
fun EditorContainerHolder(model: Editor, settings: Settings) {
    key(model) {
        with (LocalDensity.current) {
            SelectionContainer {
                Surface(
                    Modifier.fillMaxSize(),
                    color = AppTheme.colors.backgroundDark,
                ) {
                    val lines by loadableScoped(model.lines)

                    if (lines != null) {
                        Box {
                            Lines(lines!!, settings)
                            Box(
                                Modifier
                                    .offset(
                                        x = settings.fontSize.toDp() * 0.5f * settings.maxLineSymbols
                                    )
                                    .width(1.dp)
                                    .fillMaxHeight()
                                    .background(AppTheme.colors.backgroundLight)
                            )
                        }
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(36.dp)
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Lines(lines: EditorLines, settings: Settings) = with(LocalDensity.current) {
    val maxNum = remember(lines.lineNumberDigitCount) {
        (1..lines.lineNumberDigitCount).joinToString(separator = "") { "9" }
    }

    Box(Modifier.fillMaxSize()) {
        val scrollState = rememberLazyListState()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = scrollState
        ) {
            items(lines.size) { index ->
                Box(Modifier.height(settings.fontSize.toDp() * 1.6f)) {
                    Line(Modifier.align(Alignment.CenterStart), maxNum, lines[index], settings)
                }
            }
        }

        VerticalScrollbar(
            Modifier.align(Alignment.CenterEnd),
            scrollState
        )
    }
}

@Composable
private fun Line(modifier: Modifier, maxNum: String, line: EditorLine, settings: Settings) {
    Row(modifier = modifier) {
        DisableSelection {
            Box {
                LineNumber(maxNum, Modifier.alpha(0f), settings)
                LineNumber(line.number.toString(), Modifier.align(Alignment.CenterEnd), settings)
            }
        }
        LineContent(
            line.content,
            modifier = Modifier
                .weight(1f)
                .withoutWidthConstraints()
                .padding(start = 28.dp, end = 12.dp),
            settings = settings
        )
    }
}

@Composable
private fun LineNumber(number: String, modifier: Modifier, settings: Settings) = Text(
    text = number,
    fontSize = settings.fontSize,
    fontFamily = Fonts.jetbrainsMono(),
    color = LocalContentColor.current.copy(alpha = 0.30f),
    modifier = modifier.padding(start = 12.dp)
)

@Composable
private fun LineContent(content: EditorContent, modifier: Modifier, settings: Settings) = Text(
    text = if (content.isCode) {
        codeString(content.value.value)
    } else {
        buildAnnotatedString {
            withStyle(AppTheme.code.simple) {
                append(content.value.value)
            }
        }
    },
    fontSize = settings.fontSize,
    fontFamily = Fonts.jetbrainsMono(),
    modifier = modifier,
    softWrap = false
)

private fun codeString(str: String) = buildAnnotatedString {
    withStyle(AppTheme.code.simple) {
        val strFormatted = str.replace("\t", "    ")
        append(strFormatted)
        addStyle(AppTheme.code.punctuation, strFormatted, ":")
        addStyle(AppTheme.code.punctuation, strFormatted, "=")
        addStyle(AppTheme.code.punctuation, strFormatted, "\"")
        addStyle(AppTheme.code.punctuation, strFormatted, "[")
        addStyle(AppTheme.code.punctuation, strFormatted, "]")
        addStyle(AppTheme.code.punctuation, strFormatted, "{")
        addStyle(AppTheme.code.punctuation, strFormatted, "}")
        addStyle(AppTheme.code.punctuation, strFormatted, "(")
        addStyle(AppTheme.code.punctuation, strFormatted, ")")
        addStyle(AppTheme.code.punctuation, strFormatted, ",")
        addStyle(AppTheme.code.keyword, strFormatted, "fun ")
        addStyle(AppTheme.code.keyword, strFormatted, "val ")
        addStyle(AppTheme.code.keyword, strFormatted, "var ")
        addStyle(AppTheme.code.keyword, strFormatted, "private ")
        addStyle(AppTheme.code.keyword, strFormatted, "internal ")
        addStyle(AppTheme.code.keyword, strFormatted, "for ")
        addStyle(AppTheme.code.keyword, strFormatted, "expect ")
        addStyle(AppTheme.code.keyword, strFormatted, "actual ")
        addStyle(AppTheme.code.keyword, strFormatted, "import ")
        addStyle(AppTheme.code.keyword, strFormatted, "package ")
        addStyle(AppTheme.code.value, strFormatted, "true")
        addStyle(AppTheme.code.value, strFormatted, "false")
        addStyle(AppTheme.code.value, strFormatted, Regex("[0-9]*"))
        addStyle(AppTheme.code.annotation, strFormatted, Regex("^@[a-zA-Z_]*"))
        addStyle(AppTheme.code.comment, strFormatted, Regex("^\\s*//.*"))
    }
}

private fun AnnotatedString.Builder.addStyle(style: SpanStyle, text: String, regexp: String) {
    addStyle(style, text, fromLiteral(regexp))
}

private fun AnnotatedString.Builder.addStyle(style: SpanStyle, text: String, regexp: Regex) {
    for (result in regexp.findAll(text)) {
        addStyle(style, result.range.first, result.range.last + 1)
    }
}