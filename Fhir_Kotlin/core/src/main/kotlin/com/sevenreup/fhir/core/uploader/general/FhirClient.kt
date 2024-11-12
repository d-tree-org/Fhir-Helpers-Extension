package com.sevenreup.fhir.core.uploader.general

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.okhttp.client.OkHttpRestfulClientFactory
import ca.uhn.fhir.parser.IParser
import ca.uhn.fhir.rest.client.api.IGenericClient
import ca.uhn.fhir.rest.gclient.IQuery
import ca.uhn.fhir.util.BundleUtil
import com.sevenreup.fhir.core.utils.Logger
import com.sevenreup.fhir.core.utils.logicalId
import io.github.cdimascio.dotenv.Dotenv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.hl7.fhir.instance.model.api.IBaseBundle
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Resource
import java.net.URL
import java.util.concurrent.TimeUnit


class FhirClient(private val dotenv: Dotenv, private val iParser: IParser) {
    val client: IGenericClient
    private val okHttpClient: OkHttpClient
    val ctx: FhirContext = FhirContext.forR4()

    init {
        okHttpClient = createOkHttpClient()
        val factory = OkHttpRestfulClientFactory()
        factory.fhirContext = ctx
        factory.setHttpClient(okHttpClient)
        ctx.restfulClientFactory = factory
        client = ctx.newRestfulGenericClient(dotenv["FHIR_BASE_URL"])
    }

    private fun createOkHttpClient(): OkHttpClient {
        val tokenAuthenticator = TokenAuthenticator.createAuthenticator(dotenv)
        val authInterceptor = AuthInterceptor(tokenAuthenticator)
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC)

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .readTimeout(2, TimeUnit.MINUTES)
            .build()
    }

    suspend inline fun <reified T : Resource> searchResources(
        count: Int = 100,
        limit: Int? = null,
        noinline search: IQuery<IBaseBundle>.() -> Unit
    ): List<T> {
        val resources: MutableList<IBaseResource> = mutableListOf()
        val query =
            client.search<IBaseBundle>().forResource(T::class.java).apply(search).returnBundle(Bundle::class.java)
                .count(count)
        if (limit != null) {
            query.count(limit)
        }
        var bundle = query.execute()
        resources.addAll(BundleUtil.toListOfResources(ctx, bundle))

        if (limit == null) {
            while (bundle.getLink(IBaseBundle.LINK_NEXT) != null) {
                Logger.info(bundle.link.map { it.url }.toString())
                bundle = client.loadPage().next(bundle).execute()
                resources.addAll(BundleUtil.toListOfResources(ctx, bundle))
            }
        }
        return resources.toList() as List<T>
    }

    suspend inline fun <reified T : Resource>transaction(requests: List<Bundle.BundleEntryRequestComponent>): List<T> {
        val bundle = Bundle()
        bundle.setType(Bundle.BundleType.TRANSACTION)
        bundle.entry.addAll(requests.map {rq ->
            Bundle.BundleEntryComponent().apply {
                request = rq
            }
        })
        var resBundle =  client.transaction().withBundle(bundle).execute()
        val resources: MutableList<IBaseResource> = mutableListOf()
        resources.addAll(BundleUtil.toListOfResources(ctx, resBundle))
        return resources.toList() as List<T>
    }

    suspend fun fetchBundle(path: String = "", query: Map<String, String> = mapOf()): DataResponseState<Bundle> {
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
        val call = okHttpClient.newCall(request)
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
                val rawStr =
                    response.body?.string() ?: return@withContext DataResponseState.Error(Exception("Response empty"))
                DataResponseState.Success(iParser.parseResource(Bundle::class.java, rawStr))
            } catch (e: Exception) {
                Logger.error("Failed to upload batch: ${e.message}")
                DataResponseState.Error(e)
            }
        }
    }

    suspend fun bundleUpload(
        list: List<Bundle.BundleEntryComponent>,
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
            } else if (response is DataResponseState.Error) {
                throw Exception(response.exception)
            }
        }
    }

    @JvmName("bundleUploadResource")
    suspend fun bundleUpload(
        list: List<Resource>,
        batchSize: Int
    ) {
        bundleUpload(list.map { res -> Bundle.BundleEntryComponent().apply {
            resource = res
            request = Bundle.BundleEntryRequestComponent().apply {
                method = Bundle.HTTPVerb.PUT
                url = "${res.resourceType.name}/${res.logicalId}"
            }
        } }, batchSize)
    }

    suspend fun uploadBatchUpload(list: List<Bundle.BundleEntryComponent>): DataResponseState<Boolean> {
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
        val call = okHttpClient.newCall(request)
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