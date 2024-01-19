package sns.asteroid.api

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class Announcements(private val server: String, private val accessToken: String) {
    fun getAll(): Response? {
        val url = ("https://${server}/api/v1/announcements").toHttpUrlOrNull()
            ?: return null

        val urlBuilder = url.newBuilder().apply {
            addQueryParameter("with_dismissed", "true")
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