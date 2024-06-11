package com.sevenreup.fhir.core.uploader

import ca.uhn.fhir.parser.IParser
import com.sevenreup.fhir.core.utils.Logger
import com.sevenreup.fhir.core.utils.createFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Resource
import java.io.IOException
import kotlin.io.path.Path

object Uploader {
  fun upload(client: OkHttpClient, iParser: IParser, fhirServerUrl: String, fhirServerUrlApiKey: String, cacheBundle: Boolean, requestID: String, baseFilePath: String, transform: Bundle.() -> Unit): Response {
        val mediaType = "application/json".toMediaTypeOrNull()
        val bundle = Bundle()
        bundle.type = Bundle.BundleType.TRANSACTION
        bundle.apply {
            transform()
        }
      val requestString = iParser.encodeResourceToString(bundle)
      if (cacheBundle) {
          requestString.createFile(Path(baseFilePath).resolve("out/${requestID}.json").toString())
      }
        val requestBody = requestString.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(fhirServerUrl)
            .headers(mapOf(Pair("Authorization", "Bearer $fhirServerUrlApiKey")).toHeaders())
            .post(requestBody)
            .build()
        val call = client.newCall(request)

        return call.execute()
    }

     fun createBundleEntry(res: Resource): Bundle.BundleEntryComponent {
        val resUrl = "${res.fhirType()}/${res.idElement?.idPart.orEmpty()}"
        return Bundle.BundleEntryComponent().apply {
            resource = res
            fullUrl = resUrl
            request = Bundle.BundleEntryRequestComponent().apply {
                method = Bundle.HTTPVerb.PUT
                url = resUrl
            }
        }
    }
}
