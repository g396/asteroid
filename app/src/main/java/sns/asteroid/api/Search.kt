package sns.asteroid.api

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class Search(
    private val server: String,
    private val accessToken: String,
) {
    fun search(query: String, maxId: String?, minId: String?, type: Type = Type.ALL, limit: Int = 7): Response? {
        val url = ("https://${server}/api/v2/search").toHttpUrlOrNull()
            ?: return null

        val urlBuilder =url.newBuilder().apply {
            addQueryParameter("q", query)
            addQueryParameter("limit", "$limit")

            maxId?.let {
                addQueryParameter("max_id", it)
            }
            minId?.let {
                addQueryParameter("min_id", it)
            }

            if (type != Type.ALL)
                addQueryParameter("type", type.value)
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

    fun search(query: String, offset: Int, limit: Int, type: Type): Response? {
        val url = ("https://${server}/api/v2/search").toHttpUrlOrNull()
            ?: return null

        val urlBuilder = url.newBuilder().apply {
            addQueryParameter("q", query)
            addQueryParameter("offset", "$offset")
            addQueryParameter("limit", "$limit")
            addQueryParameter("resolve", "true")

            if (type != Type.ALL)
                addQueryParameter("type", type.value)
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

    enum class Type(val value: String) {
        ALL(""),
        ACCOUNTS("accounts"),
        STATUSES("statuses"),
        HASHTAGS("hashtags"),
    }
}
