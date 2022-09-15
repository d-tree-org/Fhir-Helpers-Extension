import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import org.apache.commons.io.FileUtils
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager
import org.hl7.fhir.utilities.npm.ToolsVersion

fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        val path = args[0]
        val fileData = FileUtils.readFileToString(FileUtils.getFile(path), "UTF-8")
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
    } else {
        throw Exception("Path cannot be empty")
    }
}