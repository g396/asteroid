package sns.asteroid.api

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import sns.asteroid.db.entities.Credential

class Timelines(
    private val credential: Credential
) {
    fun getLocal(maxId: String?, sinceId: String?, onlyMedia: Boolean = false): Response? {
        return getPublic(maxId, sinceId, isLocalTimeline = true, onlyMedia)
    }

    fun getPublic(maxId: String?, sinceId: String?, isLocalTimeline: Boolean = false, onlyMedia: Boolean = false): Response? {
        val url = ("https://${credential.instance}/api/v1/timelines/public").toHttpUrlOrNull()
            ?: return null

        val params = HashMap<String, String>().apply {
            put("limit", "40")
            if(isLocalTimeline) put("local", "true")
            if(onlyMedia) put("only_media", "true")
            maxId?.let { put("max_id", "$maxId") }
            sinceId?.let {put("since_id", "$sinceId")}
        }

        val urlBuilder =url.newBuilder().apply {
            params.forEach{ param -> addQueryParameter(param.key, param.value)}
        }

        val request = Request.Builder()
            .url(urlBuilder.build())
            .addHeader("Authorization", "Bearer ${credential.accessToken}")
            .get()
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e: Exception) {
            null
        }
    }

    fun getHome(maxId: String?, sinceId: String?): Response? {
        val url = ("https://${credential.instance}/api/v1/timelines/home").toHttpUrlOrNull()
            ?: return null

        val params = HashMap<String, String>().apply {
            put("limit", "40")
            maxId?.let { put("max_id", "$maxId") }
            sinceId?.let {put("since_id", "$sinceId")}
        }

        val urlBuilder =url.newBuilder().apply {
            params.forEach{ param -> addQueryParameter(param.key, param.value)}
        }

        val request = Request.Builder()
            .url(urlBuilder.build())
            .addHeader("Authorization", "Bearer ${credential.accessToken}")
            .get()
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e: Exception) {
            null
        }
    }

    fun getList(list_id: String?, maxId: String?, sinceId: String?): Response? {
        val url = ("https://${credential.instance}/api/v1/timelines/list/$list_id").toHttpUrlOrNull()
            ?: return null

        val params = HashMap<String, String>().apply {
            put("limit", "40")
            maxId?.let { put("max_id", "$maxId") }
            sinceId?.let {put("since_id", "$sinceId")}
        }

        val urlBuilder =url.newBuilder().apply {
            params.forEach{ param -> addQueryParameter(param.key, param.value)}
        }

        val request = Request.Builder()
            .url(urlBuilder.build())
            .addHeader("Authorization", "Bearer ${credential.accessToken}")
            .get()
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e: Exception) {
            null
        }
    }

    fun getHashtag(hashtag: String, maxId: String?, sinceId: String?): Response? {
        val url = ("https://${credential.instance}/api/v1/timelines/tag/$hashtag").toHttpUrlOrNull()
            ?: return null

        val params = HashMap<String, String>().apply {
            put("limit", "40")
            maxId?.let { put("max_id", "$maxId") }
            sinceId?.let {put("since_id", "$sinceId")}
        }

        val urlBuilder =url.newBuilder().apply {
            params.forEach{ param -> addQueryParameter(param.key, param.value)}
        }

        val request = Request.Builder()
            .url(urlBuilder.build())
            .addHeader("Authorization", "Bearer ${credential.accessToken}")
            .get()
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e: Exception) {
            null
        }
    }

}