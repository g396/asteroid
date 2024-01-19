package sns.asteroid.api

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class Suggestions(private val server: String, private val accessToken: String) {
    fun getSuggestions(limit: Int = 20): Response? {
        val url = ("https://$server/api/v1/suggestions").toHttpUrlOrNull()
            ?: return null

        val urlBuilder = url.newBuilder().apply {
            addQueryParameter("limit", "$limit")
        }
        val request = Request.Builder()
            .url(urlBuilder.build())
            .addHeader("Authorization", "Bearer $accessToken")
            .get()
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e: Exception) {
            null
        }
    }
}