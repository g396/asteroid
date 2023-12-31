package sns.asteroid.api

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class Hashtags(
    val server: String,
    val accessToken: String? = null,
    val limit: Int? = null,
) {
    fun getFollowedTags(maxId: String? = null, sinceId: String? = null): Response? {
        val url = ("https://${server}/api/v1/followed_tags").toHttpUrlOrNull()
            ?: return null

        val urlBuilder =url.newBuilder().apply {
            maxId?.let { addQueryParameter("max_id", it) }
            sinceId?.let { addQueryParameter("since_id", it) }
            limit?.let { addQueryParameter("limit", "$it") }
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

    fun getTag(id: String): Response? {
        val url = ("https://${server}/api/v1/tags/$id").toHttpUrlOrNull()
            ?: return null

        val urlBuilder =url.newBuilder()

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

    fun followTag(id: String): Response? {
        val url = ("https://${server}/api/v1/tags/$id/follow").toHttpUrlOrNull()
            ?: return null

        val urlBuilder =url.newBuilder()
        val requestBody = "{}".toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(urlBuilder.build())
            .addHeader("Authorization", "Bearer $accessToken")
            .post(requestBody)
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e: Exception) {
            null
        }
    }

    fun unfollowTag(id: String): Response? {
        val url = ("https://${server}/api/v1/tags/$id/unfollow").toHttpUrlOrNull()
            ?: return null

        val urlBuilder =url.newBuilder()
        val requestBody = "{}".toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(urlBuilder.build())
            .addHeader("Authorization", "Bearer $accessToken")
            .post(requestBody)
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e: Exception) {
            null
        }
    }
}