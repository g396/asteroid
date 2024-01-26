package sns.asteroid.api

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class EmojiReactions(
    private val server: String,
    private val accessToken: String,
) {
    fun putEmojiReactions(id: String, emoji: String): Response? {
        val url =
            ("https://$server/api/v1/statuses/$id/emoji_reactions/$emoji").toHttpUrlOrNull()
                ?: return null

        val requestBody = "{}".toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url.newBuilder().build())
            .addHeader("Authorization", "Bearer $accessToken")
            .put(requestBody)
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e: Exception) {
            null
        }
    }

    fun deleteEmojiReactions(id:String, emoji: String): Response? {
        val url =
            ("https://$server/api/v1/statuses/$id/emoji_reactions/$emoji").toHttpUrlOrNull()
                ?: return null

        val requestBody = "{}".toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url.newBuilder().build())
            .addHeader("Authorization", "Bearer $accessToken")
            .delete(requestBody)
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e: Exception) {
            null
        }
    }
}