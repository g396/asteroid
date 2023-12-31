package sns.asteroid.model.util

import okhttp3.Response

/**
 * フォロー・フォロワーのmax_idとsince_idを取得するにはHTTPのLinkヘッダから取得する必要がある
 * (参考)
 * https://docs.joinmastodon.org/api/guidelines/#pagination
 */
class AttributeGetter {
    companion object {
        fun getMaxIdFromHttpLinkHeader(response: Response): String {
            val linkHeader = response.headers["link"]
            val regex = Regex("<.*?max_id=(.*?)>")
            return linkHeader?.let {regex.find(linkHeader)?.groupValues?.get(1) } ?: "0"
        }

        fun getSinceIdFromHttpLinkHeader(response: Response): String {
            val linkHeader = response.headers["link"]
            val regex = Regex("<.*?since_id=(.*?)>")
            return linkHeader?.let { regex.find(linkHeader)?.groupValues?.get(1) } ?: "0"
        }
    }
}