package com.sevenreup.fhir.core.fixes

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import com.google.gson.Gson
import com.sevenreup.fhir.core.config.ProjectConfig
import com.sevenreup.fhir.core.fhir.FhirConfigs
import com.sevenreup.fhir.core.uploader.general.FhirClient
import com.sevenreup.fhir.core.uploader.general.FhirResourceServerHelper
import com.sevenreup.fhir.core.utilities.TransformSupportServices
import com.sevenreup.fhir.core.utils.createGson
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.CarePlan
import org.hl7.fhir.r4.utils.StructureMapUtilities

class CarePlanFixes constructor() {
    private val iParser: IParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
    private val scu: StructureMapUtilities
    private var contextR4: SimpleWorkerContext = FhirConfigs.createWorkerContext()
    private lateinit var projectConfig: ProjectConfig
    private lateinit var dotenv: Dotenv
    private lateinit var uploader: FhirClient
    val gson: Gson
    private var currentDir = ""
    private lateinit var resourceHelper: FhirResourceServerHelper

    init {
        scu = StructureMapUtilities(contextR4, TransformSupportServices(contextR4))
        gson = createGson()
    }

    fun fixCarePlan(projectRoot: String) {
        runBlocking() {
            dotenv = dotenv {
                directory = projectRoot
            }
            uploader = FhirClient(dotenv, iParser)
            resourceHelper = FhirResourceServerHelper(dotenv, uploader)
            val carePlans = resourceHelper.searchResources<CarePlan> {
                where(CarePlan.STATUS.exactly().code(CarePlan.CarePlanStatus.ENTEREDINERROR.toCode()))
            }
            handleCarePlans(carePlans)
        }
    }

    private fun handleCarePlans(carePlans: List<CarePlan>) {
       for (carePlan in carePlans) {
           val visit = carePlan.category.firstOrNull { code -> code.coding.firstOrNull()?.system == "https://d-tree.org/fhir/care-plan-visit-number" }?.coding?.firstOrNull()?.code
           val patient = carePlan.subject.reference
       }
    }
}