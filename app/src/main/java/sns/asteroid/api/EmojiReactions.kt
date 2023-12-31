package sns.asteroid.api

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import sns.asteroid.db.entities.Credential

class EmojiReactions(
    private val credential: Credential,
) {
    fun putEmojiReactions(id: String, emoji: String): Response? {
        val url =
            ("https://${credential.instance}/api/v1/statuses/$id/emoji_reactions/$emoji").toHttpUrlOrNull()
                ?: return null

        val requestBody = "{}".toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url.newBuilder().build())
            .addHeader("Authorization", "Bearer ${credential.accessToken}")
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
            ("https://${credential.instance}/api/v1/statuses/$id/emoji_reactions/$emoji").toHttpUrlOrNull()
                ?: return null

        val requestBody = "{}".toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url.newBuilder().build())
            .addHeader("Authorization", "Bearer ${credential.accessToken}")
            .delete(requestBody)
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e: Exception) {
            null
        }
    }
}