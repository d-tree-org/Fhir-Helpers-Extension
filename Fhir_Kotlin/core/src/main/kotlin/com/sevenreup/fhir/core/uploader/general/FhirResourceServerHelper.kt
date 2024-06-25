package com.sevenreup.fhir.core.uploader.general

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.okhttp.client.OkHttpRestfulClientFactory
import ca.uhn.fhir.rest.client.api.IGenericClient
import ca.uhn.fhir.rest.gclient.IQuery
import ca.uhn.fhir.util.BundleUtil
import com.sevenreup.fhir.core.utils.Logger
import io.github.cdimascio.dotenv.Dotenv
import org.hl7.fhir.instance.model.api.IBaseBundle
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Resource


class FhirResourceServerHelper(val dotenv: Dotenv, val fhirClient: FhirClient) {
    val client: IGenericClient
    val ctx: FhirContext = FhirContext.forR4()

    init {
        val factory = OkHttpRestfulClientFactory()
        factory.fhirContext = ctx
        factory.setHttpClient(fhirClient.client)
        ctx.restfulClientFactory = factory
        client = ctx.newRestfulGenericClient(dotenv["FHIR_BASE_URL"])
    }
    suspend inline fun <reified T : Resource> searchResources(noinline search: IQuery<IBaseBundle>.() -> Unit): List<T> {
        val resources: MutableList<IBaseResource> = mutableListOf()
        var bundle =
            client.search<IBaseBundle>().forResource(T::class.java).apply(search).returnBundle(Bundle::class.java)
                .count(100)
                .execute()
        resources.addAll(BundleUtil.toListOfResources(ctx, bundle))

        while (bundle.getLink(IBaseBundle.LINK_NEXT) != null) {
            Logger.info(bundle.link.map { it.url }.toString())
            bundle = client.loadPage().next(bundle).execute()
            resources.addAll(BundleUtil.toListOfResources(ctx, bundle))
        }
        return resources.toList() as List<T>
    }
}