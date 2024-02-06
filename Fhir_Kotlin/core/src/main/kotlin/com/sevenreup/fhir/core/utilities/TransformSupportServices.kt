package com.sevenreup.fhir.core.utilities

import org.hl7.fhir.exceptions.FHIRException
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.*
import org.hl7.fhir.r4.terminologies.ConceptMapEngine
import org.hl7.fhir.r4.utils.StructureMapUtilities


class TransformSupportServices constructor(val simpleWorkerContext: SimpleWorkerContext) :
    StructureMapUtilities.ITransformerServices {

    val outputs: MutableList<Base> = mutableListOf()

    override fun log(message: String) {
        println(message)
    }

    @Throws(FHIRException::class)
    override fun createType(appInfo: Any, name: String): Base {
        return when (name) {
            "RiskAssessment_Prediction" -> RiskAssessment.RiskAssessmentPredictionComponent()
            "Immunization_VaccinationProtocol" -> Immunization.ImmunizationProtocolAppliedComponent()
            "Immunization_Reaction" -> Immunization.ImmunizationReactionComponent()
            "EpisodeOfCare_Diagnosis" -> EpisodeOfCare.DiagnosisComponent()
            "Encounter_Diagnosis" -> Encounter.DiagnosisComponent()
            "Encounter_Participant" -> Encounter.EncounterParticipantComponent()
            "CarePlan_Activity" -> CarePlan.CarePlanActivityComponent()
            "CarePlan_ActivityDetail" -> CarePlan.CarePlanActivityDetailComponent()
            "Patient_Link" -> Patient.PatientLinkComponent()
            "Timing_Repeat" -> Timing.TimingRepeatComponent()
            "PlanDefinition_Action" -> PlanDefinition.PlanDefinitionActionComponent()
            "Group_Characteristic" -> Group.GroupCharacteristicComponent()
            "Appointment_Participant" -> Appointment.AppointmentParticipantComponent()
            else -> ResourceFactory.createResourceOrType(name)
        }
    }

    override fun createResource(appInfo: Any, res: Base, atRootofTransform: Boolean): Base {
        if (atRootofTransform) outputs.add(res)
        return res
    }

    @Throws(FHIRException::class)
    override fun translate(appInfo: Any, source: Coding, conceptMapUrl: String): Coding {
        val cme = ConceptMapEngine(simpleWorkerContext)
        return cme.translate(source, conceptMapUrl)
    }

    @Throws(FHIRException::class)
    override fun resolveReference(appContext: Any, url: String): Base {
        throw FHIRException("resolveReference is not supported yet")
    }

    @Throws(FHIRException::class)
    override fun performSearch(appContext: Any, url: String): List<Base> {
        return  listOf(Patient().apply {
            id = "1"
        })
    }
}
