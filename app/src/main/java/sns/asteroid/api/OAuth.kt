package sns.asteroid.api

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import sns.asteroid.api.entities.Application
import java.net.URI

class OAuth(
    private val server: String,
    private val application: Application?,
) {
    private val redirectUri = Apps.REDIRECT_URI
    private val scope = Apps.SCOPE

    constructor(server: String): this(server, null)
    fun generateUrlToAuthorize(): URI? {

        val url = ("https://$server/oauth/authorize").toHttpUrlOrNull()
            ?: return null

        val params = HashMap<String, String>().apply {
            application?.client_id?.let { put("client_id", it) }
            application?.client_secret?.let { put("client_secret", it) }
            put("response_type", "code")
            put("redirect_uri", redirectUri)
            put("scope", scope)
            put("force_login", true.toString())
        }

        return url.newBuilder().apply {
            params.forEach { param -> addQueryParameter(param.key, param.value) }
        }.build().toUri()
    }

    fun obtainToken(code: String): Response? {
        val url = ("https://$server/oauth/token").toHttpUrlOrNull()
            ?: return null

        val params = HashMap<String, String>().apply {
            application?.client_id?.let { put("client_id", it) }
            application?.client_secret?.let { put("client_secret", it) }
            put("code", code)
            put("grant_type", "authorization_code")
            put("redirect_uri", redirectUri)
            put("scope", scope)
        }

        val urlBuilder = url.newBuilder().apply {
            params.forEach { param -> addQueryParameter(param.key, param.value) }
        }

        val requestBody = "{}".toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(urlBuilder.build())
            .post(requestBody)
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e: Exception) {
            return null
        }
    }

    fun verifyCredentials(accessToken: String): Response? {
        val url = ("https://${server}/api/v1/accounts/verify_credentials").toHttpUrlOrNull()
            ?: return null
        val urlBuilder = url.newBuilder()

        val request = Request.Builder()
            .url(urlBuilder.build())
            .addHeader("Authorization", "Bearer $accessToken")
            .get()
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e: Exception) {
            return null
        }
    }
}
