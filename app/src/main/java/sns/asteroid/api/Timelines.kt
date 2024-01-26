package sns.asteroid.api

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class Timelines(
    private val server: String,
    private val accessToken: String,
) {
    fun getLocal(maxId: String?, sinceId: String?, onlyMedia: Boolean = false, limit: Int = 40): Response? {
        return getPublic(maxId, sinceId, isLocalTimeline = true, onlyMedia, limit)
    }

    fun getPublic(
        maxId: String?,
        sinceId: String?,
        isLocalTimeline: Boolean = false,
        onlyMedia: Boolean = false,
        limit: Int = 40,
    ): Response? {
        val url = ("https://$server/api/v1/timelines/public").toHttpUrlOrNull()
            ?: return null

        val urlBuilder = url.newBuilder().apply {
            addQueryParameter("limit", "$limit")

            maxId?.let {
                addQueryParameter("max_id", "$maxId")
            }
            sinceId?.let {
                addQueryParameter("since_id", "$sinceId")
            }

            if(isLocalTimeline)
                addQueryParameter("local", "true")
            if(onlyMedia)
                addQueryParameter("only_media", "true")
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

    fun getHome(maxId: String?, sinceId: String?, limit: Int = 40): Response? {
        val url = ("https://$server/api/v1/timelines/home").toHttpUrlOrNull()
            ?: return null

        val urlBuilder =url.newBuilder().apply {
            addQueryParameter("limit", "$limit")

            maxId?.let {
                addQueryParameter("max_id", "$maxId")
            }
            sinceId?.let {
                addQueryParameter("since_id", "$sinceId")
            }
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

    fun getList(list_id: String?, maxId: String?, sinceId: String?, limit: Int = 40): Response? {
        val url = ("https://$server/api/v1/timelines/list/$list_id").toHttpUrlOrNull()
            ?: return null

        val urlBuilder =url.newBuilder().apply {
            addQueryParameter("limit", "$limit")

            maxId?.let {
                addQueryParameter("max_id", "$maxId")
            }
            sinceId?.let {
                addQueryParameter("since_id", "$sinceId")
            }
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

    fun getHashtag(hashtag: String, maxId: String?, sinceId: String?, limit: Int = 40): Response? {
        val url = ("https://$server/api/v1/timelines/tag/$hashtag").toHttpUrlOrNull()
            ?: return null

        val urlBuilder =url.newBuilder().apply {
            addQueryParameter("limit", "$limit")

            maxId?.let {
                addQueryParameter("max_id", "$maxId")
            }
            sinceId?.let {
                addQueryParameter("since_id", "$sinceId")
            }
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