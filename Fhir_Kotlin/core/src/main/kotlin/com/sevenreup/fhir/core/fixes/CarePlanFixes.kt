package com.sevenreup.fhir.core.fixes

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import com.google.gson.Gson
import com.sevenreup.fhir.core.config.ProjectConfig
import com.sevenreup.fhir.core.fhir.FhirConfigs
import com.sevenreup.fhir.core.uploader.general.FhirClient
import com.sevenreup.fhir.core.utilities.TransformSupportServices
import com.sevenreup.fhir.core.utils.*
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.CarePlan
import org.hl7.fhir.r4.utils.StructureMapUtilities
import kotlin.io.path.Path

class CarePlanFixes {
    private val iParser: IParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
    private val scu: StructureMapUtilities
    private var contextR4: SimpleWorkerContext = FhirConfigs.createWorkerContext()
    private lateinit var dotenv: Dotenv
    private lateinit var fhirClient: FhirClient
    val gson: Gson
    private val brokenCarePlans = mutableListOf<String>()

    init {
        scu = StructureMapUtilities(contextR4, TransformSupportServices(contextR4))
        gson = createGson()
    }

    fun fixCarePlan(projectRoot: String) {
        runBlocking() {
            dotenv = dotenv {
                directory = projectRoot
            }
            fhirClient = FhirClient(dotenv, iParser)
            val carePlans = fhirClient.searchResources<CarePlan>(count = 120) {
                where(CarePlan.STATUS.exactly().code(CarePlan.CarePlanStatus.ENTEREDINERROR.toCode()))
            }
            handleCarePlans(carePlans)
            if (brokenCarePlans.isNotEmpty()) {
                gson.toJson(brokenCarePlans).createFile(Path(projectRoot).resolve("out/broken-carePlans").toString())
            }
            brokenCarePlans.clear()
        }
    }

    private suspend fun handleCarePlans(carePlans: List<CarePlan>) {
        val carePlansToUpdate = mutableListOf<CarePlan>()
        val groupedBySameVisit = carePlans.groupBy { carePlan ->
            val visit =
                carePlan.category.firstOrNull { code -> code.coding.firstOrNull()?.system == "https://d-tree.org/fhir/care-plan-visit-number" }?.coding?.firstOrNull()?.code
            val patient = carePlan.subject.reference
            "${visit}_${patient}"
        }
        for (entry in groupedBySameVisit) {
            if (entry.value.size == 1) {
                val carePlan = entry.value[0]
                val (otherCarePlansTheSame, hasNextVisit) = getSimilarVisitCarePlan(carePlan)
                if (otherCarePlansTheSame.isNotEmpty()) {
                    pickLastUpdateCarePlan(
                        otherCarePlansTheSame + listOf(carePlan),
                        hasNextVisit
                    )?.let { carePlansToUpdate.add(it) }
                } else {
                    carePlansToUpdate.add(removeEnteredInError(carePlan, hasNextVisit))
                }
                Logger.error("Other same visit ${otherCarePlansTheSame.size}")
            } else {
                Logger.error("Multiple have been set as entered in error")
                val allIds = entry.value.map { it.logicalId }
                val (otherCarePlansTheSame, hasNextVisit) = getSimilarVisitCarePlan(entry.value[0])
                val similar = otherCarePlansTheSame.filter { cp -> !allIds.contains(cp.logicalId) }
                pickLastUpdateCarePlan(entry.value + similar, hasNextVisit)?.let { carePlansToUpdate.add(it) }
            }
        }
        Logger.error("Total to update: ${carePlansToUpdate.size}")
        fhirClient.bundleUpload(carePlansToUpdate, 10)
    }

    private fun pickLastUpdateCarePlan(carePlans: List<CarePlan>, hasNextVisit: Boolean): CarePlan? {
        var sorted = carePlans.sortedByDescending { it.meta.lastUpdated }
        var actualCarePlan: CarePlan? = null
        for (carePlan in sorted) {
            if (carePlan.status != CarePlan.CarePlanStatus.ENTEREDINERROR) {
                Logger.error("Looks this group is already sorted")
                return null
            }
            if (carePlan.isCompleted()) {
                actualCarePlan = carePlan
                break
            }
        }
        var hasCompletedItem = false
        sorted = sorted.sortedByDescending {
            val count = it.activity.count { cp -> cp.detail.status == CarePlan.CarePlanActivityStatus.COMPLETED }
            if (count > 0) {
                hasCompletedItem = true
            }
            count
        }
        if (!hasCompletedItem) {
            sorted = carePlans.sortedByDescending { it.meta.lastUpdated }
        }
        return removeEnteredInError(actualCarePlan ?: sorted.first(), hasNextVisit)
    }

    private suspend fun getSimilarVisitCarePlan(carePlan: CarePlan): Pair<List<CarePlan>, Boolean> {
        val visit =
            (carePlan.category.firstOrNull { code -> code.coding.firstOrNull()?.system == "https://d-tree.org/fhir/care-plan-visit-number" }?.coding?.firstOrNull()?.code
                ?: "1").toInt()
        val patient = carePlan.subject
        val baseUrl = "CarePlan?subject=${patient.extractId()}"
        val currentAndNextBundles = fhirClient.transaction<Bundle>(listOf(
            Bundle.BundleEntryRequestComponent().apply {
                method = Bundle.HTTPVerb.GET
                url = "${baseUrl}&category=${visit}"
            },
            Bundle.BundleEntryRequestComponent().apply {
                method = Bundle.HTTPVerb.GET
                url = "${baseUrl}&category=${visit + 1}&_summary=count"
            }
        ))

        Logger.info("carePlanId: ${carePlan.logicalId}, visit number: $visit - ${currentAndNextBundles.first().entry.size}, next visit: ${currentAndNextBundles[1].total}")
        return Pair(currentAndNextBundles.first().entry.map { it.resource as CarePlan }
            .filter { carePlan.logicalId != it.logicalId }, currentAndNextBundles[1].total > 0)
    }

    private fun removeEnteredInError(carePlan: CarePlan, hasNextVisit: Boolean): CarePlan {
        val isCompleted = carePlan.isCompleted()
        val newCarePlan = carePlan.copy()
        newCarePlan.status = if (isCompleted) CarePlan.CarePlanStatus.COMPLETED else CarePlan.CarePlanStatus.ACTIVE
        if (hasNextVisit && newCarePlan.status != CarePlan.CarePlanStatus.COMPLETED) {
            brokenCarePlans.add(newCarePlan.logicalId)
            newCarePlan.status = CarePlan.CarePlanStatus.COMPLETED
        }
        Logger.error(newCarePlan.status.name)
        return newCarePlan
    }
}