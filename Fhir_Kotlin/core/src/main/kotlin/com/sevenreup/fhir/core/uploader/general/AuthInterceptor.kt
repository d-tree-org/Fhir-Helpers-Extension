package com.sevenreup.fhir.core.uploader.general

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenAuthenticator: TokenAuthenticator) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer ${tokenAuthenticator.getToken()}")
            .build()
        return chain.proceed(authenticatedRequest)
    }
}
