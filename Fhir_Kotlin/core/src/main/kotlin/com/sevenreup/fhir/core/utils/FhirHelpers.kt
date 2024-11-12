package com.sevenreup.fhir.core.utils

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import com.sevenreup.fhir.core.structureMaps.createStructureMapFromFile
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.*
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager
import org.hl7.fhir.utilities.npm.ToolsVersion

fun formatStructureMap(path: String, srcName: String?): CoreResponse<String> {
    val map = createStructureMapFromFile(path, srcName ?: "Main")

    return CoreResponse(data = org.hl7.fhir.r4.utils.StructureMapUtilities.render(map))
}

fun verifyQuestionnaire(path: String) {
    val content = path.readFile()
    val iParser: IParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
    val validator = FhirContext.forCached(FhirVersionEnum.R4).newValidator()
    val questionnaire = iParser.parseResource(Questionnaire::class.java, content)
    validator.validateWithResult(questionnaire)
    println(questionnaire.toString())
}

fun Reference.extractId(): String =
    if (this.reference.isNullOrEmpty()) {
        ""
    } else this.reference.substringAfterLast(delimiter = '/', missingDelimiterValue = "")


val Resource.logicalId: String
    get() {
        return this.idElement?.idPart.orEmpty()
    }

fun CarePlan.isCompleted(): Boolean {
    val tasks = fetchCarePlanActivities(this)
    return tasks.isNotEmpty() &&  tasks.all { it.detail.status == CarePlan.CarePlanActivityStatus.COMPLETED }
}

fun CarePlan.isStarted(): Boolean {
    val statuses = listOf(CarePlan.CarePlanActivityStatus.CANCELLED, CarePlan.CarePlanActivityStatus.COMPLETED)
   return this.activity.firstOrNull {  statuses.contains(it.detail.status) } != null
}

fun CarePlan.CarePlanActivityComponent.shouldShowOnProfile(): Boolean {
    return (this.detail.status == CarePlan.CarePlanActivityStatus.SCHEDULED ||
            this.detail.status == CarePlan.CarePlanActivityStatus.ONHOLD ||
            this.detail.status == CarePlan.CarePlanActivityStatus.CANCELLED)
        .not()
}

private fun fetchCarePlanActivities(
    carePlan: CarePlan?,
): List<CarePlan.CarePlanActivityComponent> {
    if (carePlan == null) return emptyList()
    val activityOnList = mutableMapOf<String, CarePlan.CarePlanActivityComponent>()
    val tasksToFetch = mutableListOf<String>()
    for (planActivity in carePlan.activity) {
        if (!planActivity.shouldShowOnProfile()) {
            continue
        }
        val taskId = planActivity.outcomeReference.firstOrNull()?.extractId()
        if (taskId != null) {
            tasksToFetch.add(taskId)
            activityOnList[taskId] = planActivity
        }
    }
    return activityOnList.values.sortedWith(
        compareBy(nullsLast()) { it.detail?.code?.text?.toBigIntegerOrNull() },
    )
}