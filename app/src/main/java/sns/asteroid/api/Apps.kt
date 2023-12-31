package sns.asteroid.api

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class Apps(private val server: String, private val appName: String) {
    companion object {
        const val APP_NAME = "Asteroid"
        const val REDIRECT_URI = "asteroid.oauth://callback"
        const val SCOPE = "read write follow"
    }

    fun createApps(): Response? {
        val url = ("https://$server/api/v1/apps").toHttpUrlOrNull()
            ?: return null

        val params = HashMap<String, String>().apply {
            put("client_name", appName.ifBlank { APP_NAME })
            put("redirect_uris", REDIRECT_URI)
            put("scopes", SCOPE)
        }

        val urlBuilder =url.newBuilder().apply {
            params.forEach{ param -> addQueryParameter(param.key, param.value)}
        }

        val requestBody = "{}".toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(urlBuilder.build())
            .post(requestBody)
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e:Exception) {
            return null
        }
    }
}