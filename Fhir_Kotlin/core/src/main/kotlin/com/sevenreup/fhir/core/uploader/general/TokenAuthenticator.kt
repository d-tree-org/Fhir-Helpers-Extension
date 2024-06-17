package com.sevenreup.fhir.core.uploader.general

import com.google.gson.Gson
import io.github.cdimascio.dotenv.Dotenv
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

data class TokenResponse(
    val access_token: String,
    val expires_in: Long
)

class TokenAuthenticator(
    private val keycloakUrl: String,
    private val clientId: String,
    private val clientSecret: String,
    private val username: String,
    private val password: String
) {

    @Volatile
    private var token: String? = null
    @Volatile
    private var tokenExpiryTime: Long = 0

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    @Synchronized
    fun getToken(): String {
        if (token == null || System.currentTimeMillis() > tokenExpiryTime) {
            fetchToken()
        }
        return token ?: throw IllegalStateException("Token should not be null")
    }

    @Synchronized
    private fun fetchToken() {
        val requestBody = FormBody.Builder()
            .add("client_id", clientId)
            .add("client_secret", clientSecret)
            .add("username", username)
            .add("password", password)
            .add("grant_type", "password")
            .build()

        val request = Request.Builder()
            .url("$keycloakUrl/protocol/openid-connect/token")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val jsonResponse = response.body?.string()
            val tokenResponse = gson.fromJson(jsonResponse, TokenResponse::class.java)

            token = tokenResponse.access_token
            tokenExpiryTime = System.currentTimeMillis() + (tokenResponse.expires_in * 1000)
        }
    }

    companion object {
        fun createAuthenticator(dotenv: Dotenv): TokenAuthenticator {
            val keycloakUrl = dotenv["KEYCLOAK_ISSUER"]
            val clientId = dotenv["KEYCLOAK_CLIENT_ID"]
            val clientSecret = dotenv["KEYCLOAK_CLIENT_SECRET"]
            val username = dotenv["KEYCLOAK_USERNAME"]
            val password = dotenv["KEYCLOAK_PASSWORD"]

            return TokenAuthenticator(
                keycloakUrl,
                clientId,
                clientSecret,
                username,
                password
            )
        }
    }
}
