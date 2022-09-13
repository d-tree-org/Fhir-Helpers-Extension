import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import codeviewer.platform.toProjectFile
import codeviewer.ui.MainView
import repositories.FileRepository
import javax.swing.JFileChooser
import javax.swing.UIManager

@Composable
@Preview
fun App(fileRepository: FileRepository) {
    MaterialTheme {
        MainView(fileRepository) {
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

fun main() = application {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    Window(onCloseRequest = ::exitApplication) {
        App(fileRepository = FileRepository())
//        SplitterScreen()
    }
}
