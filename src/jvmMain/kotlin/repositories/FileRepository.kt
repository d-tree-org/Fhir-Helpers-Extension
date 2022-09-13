package repositories

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import codeviewer.platform.File
import codeviewer.platform.HomeFolder
import codeviewer.ui.CodeViewer
import codeviewer.ui.common.Settings
import codeviewer.ui.editor.Editors
import codeviewer.ui.filetree.FileTree
import kotlinx.coroutines.GlobalScope
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager
import org.hl7.fhir.utilities.npm.ToolsVersion

class FileRepository {
    private val settings by mutableStateOf(Settings())
    private val editors = Editors()

    val codeViewer by mutableStateOf(
        CodeViewer(
            editors = editors,
            fileTree = FileTree(HomeFolder, editors),
            settings = settings
        )
    )

    fun open(file: File) {
        editors.open(file)
    }

    fun compile() {
        editors.active?.let { editor ->
            try {
                val data = editor.lines(GlobalScope)
                val builder = StringBuilder()
                for (d in (0..data.size)) {
                    builder.append(data[d].content.value.value)
                }
                val pcm = FilesystemPackageCacheManager(true, ToolsVersion.TOOLS_VERSION)
                // Package name manually checked from
                // https://simplifier.net/packages?Text=hl7.fhir.core&fhirVersion=All+FHIR+Versions
                val contextR4 = SimpleWorkerContext.fromPackage(pcm.loadPackage("hl7.fhir.r4.core", "4.0.1"))
                contextR4.isCanRunWithoutTerminology = true

                val scu = org.hl7.fhir.r4.utils.StructureMapUtilities(contextR4)
                val map = scu.parse(builder.toString(), "PatientRegistration")

                val iParser: IParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
                val mapString = iParser.encodeResourceToString(map)

                System.out.println(mapString)
                val newEditor = editor.copy(compileState = editor.compileState.copy(output = mapString, error = "", hasError = false))
                editors.change(newEditor)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}