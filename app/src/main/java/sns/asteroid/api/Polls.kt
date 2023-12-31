package sns.asteroid.api

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class Polls(
    private val server: String,
    private val accessToken: String,
) {
    fun vote(id: String, choices: List<Int>): Response? {
        val url = ("https://${server}/api/v1/polls/$id/votes").toHttpUrlOrNull()
            ?: return null

        val urlBuilder =url.newBuilder().apply {
            choices.forEach { addQueryParameter("choices[]", it.toString()) }
        }

        val requestBody = "{}".toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(urlBuilder.build())
            .addHeader("Authorization", "Bearer $accessToken")
            .post(requestBody)
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e: Exception) {
            return null
        }
    }
}