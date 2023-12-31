package sns.asteroid.api

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class Directory(
    private val server: String,
    private val accessToken: String,
) {
    fun getDirectory(offset: Int = 0, limit: Int = 20): Response? {
        val url = ("https://$server/api/v1/directory").toHttpUrlOrNull()
            ?: return null

        val urlBuilder =url.newBuilder().apply {
            addQueryParameter("limit", "$limit")
            addQueryParameter("offset", "$offset")
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