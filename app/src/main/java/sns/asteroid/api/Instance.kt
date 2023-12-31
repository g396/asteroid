package sns.asteroid.api

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 * インスタンス固有の情報を取得する
 * @param domain インスタンスのドメイン(ex. "mastodon.social")
 */
class Instance(private val domain: String) {
    fun getInstance(): Response? {
        val url = ("https://$domain/api/v2/instance").toHttpUrlOrNull()
            ?: return null

        val urlBuilder =url.newBuilder()
        val request = Request.Builder()
            .url(urlBuilder.build())
            .get()
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e:Exception) { // When occurred timeout
            return null
        }
    }

    fun getInstanceV1(): Response? {
        val url = ("https://$domain/api/v1/instance").toHttpUrlOrNull()
            ?: return null

        val urlBuilder =url.newBuilder()
        val request = Request.Builder()
            .url(urlBuilder.build())
            .get()
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e:Exception) { // When occurred timeout
            return null
        }
    }

    fun getCustomEmojis(): Response? {
        val url = ("https://$domain/api/v1/custom_emojis").toHttpUrlOrNull()
            ?: return null

        val urlBuilder =url.newBuilder()
        val request = Request.Builder()
            .url(urlBuilder.build())
            .get()
            .build()
        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e: Exception) {
            return null
        }
    }
}