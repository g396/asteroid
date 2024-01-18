package sns.asteroid.api

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class Notifications(
    private val server: String,
    private val accessToken: String,
) {
    fun getAll(maxId: String?, sinceId: String?, onlyMention: Boolean): Response? {
        val url = ("https://$server/api/v1/notifications").toHttpUrlOrNull()
            ?: return null

        // ブラックリスト形式で除外しないとfedibirdでバグる・・・
        val excludeTypes = listOf(
            "follow",
            "follow_request",
            "favourite",
            "reblog",
            "poll",
            "emoji_reaction",
            "status_reference",
            "update",
            "status",
            "admin.sign_up",
            "admin.report",
        )

        val urlBuilder =url.newBuilder().apply {
            addQueryParameter("limit", "40")
            maxId?.let { addQueryParameter("max_id", it) }
            sinceId?.let { addQueryParameter("since_id", it) }
            if(onlyMention) excludeTypes.forEach { addQueryParameter("exclude_types[]", it) }
        }

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