import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import com.google.gson.Gson
import org.apache.commons.io.FileUtils
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager
import org.hl7.fhir.utilities.npm.ToolsVersion

fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        when (args[0]) {
            "str_compile" -> {
                val path = args[1]
                compileStructureMap(path)
            }

            "qst_verify" -> {
                val path = args[1]
                verifyQuestionnaire(path)
            }

            else -> {
                throw Exception("Please use a valid mode")
            }
        }

    } else {
        throw Exception("Please specify args")
    }
}

fun compileStructureMap(path: String) {
    val fileData = path.readFile()
    val pcm = FilesystemPackageCacheManager(true, ToolsVersion.TOOLS_VERSION)
    // Package name manually checked from
    // https://simplifier.net/packages?Text=hl7.fhir.core&fhirVersion=All+FHIR+Versions
    val contextR4 = SimpleWorkerContext.fromPackage(pcm.loadPackage("hl7.fhir.r4.core", "4.0.1"))
    contextR4.isCanRunWithoutTerminology = true

    val scu = org.hl7.fhir.r4.utils.StructureMapUtilities(contextR4)
    val map = scu.parse(fileData, "PatientRegistration")

    val iParser: IParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
    val mapString = iParser.encodeResourceToString(map)
    println("\n\nMAP_OUTPUT_STARTS_HERE\n\n")
    println(mapString)
}

fun verifyQuestionnaire(path: String) {
    val content = path.readFile()
    val iParser: IParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
    val validator = FhirContext.forCached(FhirVersionEnum.R4).newValidator()
    val questionnaire = iParser.parseResource(Questionnaire::class.java, content)
    validator.validateWithResult(questionnaire)
    println(questionnaire.toString())
}