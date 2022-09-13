import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import codeviewer.platform.toProjectFile
import codeviewer.ui.MainView
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import logging.*
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import repositories.FileRepository
import repositories.LoggerRepository
import java.io.PrintStream
import javax.swing.JFileChooser
import javax.swing.UIManager


@ExperimentalSplitPaneApi
@ExperimentalFoundationApi
@Composable
@Preview
fun App(fileRepository: FileRepository, loggerRepository: LoggerRepository) {
    MaterialTheme {
        MainView(fileRepository, loggerRepository) {
            val fileChooser = JFileChooser("/").apply {
                fileSelectionMode = JFileChooser.FILES_ONLY
                dialogTitle = "Select a folder"
                approveButtonText = "Select"
                approveButtonToolTipText = "Select current directory as save destination"
            }
            fileChooser.showOpenDialog(ComposeWindow() /* OR null */)
            val result = fileChooser.selectedFile

            if (result != null) {
                println(result.absoluteFile)
                fileRepository.open(result.toProjectFile())
            }
        }
    }
}

@ExperimentalSplitPaneApi
@DelicateCoroutinesApi
@ExperimentalFoundationApi
fun main() = application {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    val loggerRepository = LoggerRepository()
    val stream = PrintStream(LoggingOutputStream(loggerRepository.channel, GlobalScope))
    System.setOut(stream)
    System.setErr(stream)
    Window(onCloseRequest = ::exitApplication) {
        App(fileRepository = FileRepository(), loggerRepository = loggerRepository)
    }
}