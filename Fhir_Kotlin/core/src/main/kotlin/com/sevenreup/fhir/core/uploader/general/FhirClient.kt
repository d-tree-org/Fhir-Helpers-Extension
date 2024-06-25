package com.sevenreup.fhir.core.uploader.general

import ca.uhn.fhir.parser.IParser
import com.sevenreup.fhir.core.utils.Logger
import io.github.cdimascio.dotenv.Dotenv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent
import java.net.URL
import java.util.concurrent.TimeUnit


class FhirClient(private val dotenv: Dotenv, private val iParser: IParser) {
     val client: OkHttpClient

    init {
        val tokenAuthenticator = TokenAuthenticator.createAuthenticator(dotenv)
        val authInterceptor = AuthInterceptor(tokenAuthenticator)

        client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .readTimeout(2, TimeUnit.MINUTES)
            .build()
    }

    suspend fun fetchBundle(path: String = "",query: Map<String, String> = mapOf()): DataResponseState<Bundle> {
        val base = URL(dotenv["FHIR_BASE_URL"]).toHttpUrlOrNull()!!
        val url = base.newBuilder()
        url.addPathSegments(path)
        query.forEach {
            url.addQueryParameter(it.key, it.value)
        }
        val request = Request.Builder()
            .url(url.build())
            .get()
            .build()
        val call = client.newCall(request)
        return withContext(Dispatchers.IO) {
            try {
                val response = call.execute()
                if (!response.isSuccessful) {
                    Logger.error("Failed to upload batch: ${response.code} - ${response.message}")
                    return@withContext DataResponseState.Error(exceptionFromResponse(response))
                } else {
                    Logger.info("Uploaded successfully")
                }
                response.close()
               val rawStr = response.body?.string() ?: return@withContext DataResponseState.Error(Exception("Response empty"))
                DataResponseState.Success(iParser.parseResource(Bundle::class.java,rawStr))
            } catch (e: Exception) {
                Logger.error("Failed to upload batch: ${e.message}")
                DataResponseState.Error(e)
            }
        }
    }

    suspend fun bundleUpload(
        list: List<BundleEntryComponent>,
        batchSize: Int
    ) {
        val totalBatches = if (list.size % batchSize == 0) list.size / batchSize else list.size / batchSize + 1

        for (batchIndex in 0 until totalBatches) {
            val start = batchIndex * batchSize
            val end = minOf((batchIndex + 1) * batchSize, list.size)
            val batchFiles = list.subList(start, end)

            val response = uploadBatchUpload(batchFiles)

            if (response is DataResponseState.Success) {
                Logger.info("Uploaded successfully")
            } else if(response is DataResponseState.Error) {
                throw Exception(response.exception)
            }
        }
    }

    private suspend fun uploadBatchUpload(list: List<BundleEntryComponent>): DataResponseState<Boolean> {
        val bundle = Bundle().apply {
            entry = list
            type = Bundle.BundleType.TRANSACTION
        }
        val mediaType = "application/json".toMediaTypeOrNull()
        val requestBody = iParser.encodeResourceToString(bundle).toRequestBody(mediaType)
        println(iParser.encodeResourceToString(bundle))
        val request = Request.Builder()
            .url(dotenv["FHIR_BASE_URL"])
            .post(requestBody)
            .build()
        val call = client.newCall(request)
        return withContext(Dispatchers.IO) {
            try {
                val response = call.execute()
                if (!response.isSuccessful) {
                    Logger.error("Failed to upload batch: ${response.code} - ${response.message}")
                    return@withContext DataResponseState.Error(exceptionFromResponse(response))
                } else {
                    Logger.info("Uploaded successfully")
                }
                response.close()
                DataResponseState.Success(true)
            } catch (e: Exception) {
                Logger.error("Failed to upload batch: ${e.message}")
                DataResponseState.Error(e)
            }
        }
    }

    private fun exceptionFromResponse(response: Response): Exception {
        return Exception("Status: ${response.code},message: ${response.message}, body: ${response.body?.string()} ")
    }
}

sealed class DataResponseState<out T> {
    data class Success<T>(val data: T) : DataResponseState<T>()

    data class Error(val exception: Exception) : DataResponseState<Nothing>()
}
