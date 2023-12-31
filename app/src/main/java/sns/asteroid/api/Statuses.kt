package sns.asteroid.api

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import sns.asteroid.db.entities.Credential

class Statuses(
    private val credential: Credential,
) {
    fun getContext(id: String): Response? {
        val url = ("https://${credential.instance}/api/v1/statuses/$id/context").toHttpUrlOrNull()
            ?: return null

        val request = Request.Builder()
            .url(url.newBuilder().build())
            .addHeader("Authorization", "Bearer ${credential.accessToken}")
            .get()
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e: Exception) {
            null
        }
    }

    fun postNewStatus(status: String, visibility: String): Response? {
        return postNewStatus(
            status = status,
            mediaIds = null,
            pollOptions = null,
            pollExpiresIn = null,
            pollMultiple = null,
            pollHideTotals = null,
            inReplyToId = null,
            sensitive = false,
            spoilerText = "",
            visibility = visibility,
            language = null,
            scheduledAt = null,
        )
    }

    fun postNewStatus(
        status: String,
        spoilerText: String,
        mediaIds: List<String>,
        sensitive: Boolean,
        visibility: String,
        pollOptions: List<String>?,
        pollExpiresIn: Int?,
        pollMultiple: Boolean?,
        inReplyToId: String?,
    ): Response? {
        return postNewStatus(
            status = status,
            mediaIds = mediaIds,
            pollOptions = pollOptions,
            pollExpiresIn = pollExpiresIn,
            pollMultiple = pollMultiple,
            pollHideTotals = null,
            inReplyToId = inReplyToId,
            sensitive = sensitive,
            spoilerText = spoilerText,
            visibility = visibility,
            language = null,
            scheduledAt = null,
        )
    }

    fun postNewStatus(
        status: String?,
        mediaIds: List<String>?,
        pollOptions: List<String>?,
        pollExpiresIn: Int?,
        pollMultiple: Boolean?,
        pollHideTotals: Boolean?,
        inReplyToId: String?,
        sensitive: Boolean?,
        spoilerText: String,
        visibility: String,
        language: String?,
        scheduledAt: String?,
    ): Response? {
        val url = ("https://${credential.instance}/api/v1/statuses").toHttpUrlOrNull()
            ?: return null

        val params = HashMap<String, String>().apply {
            status?.let {put("status", "$status")}
            pollExpiresIn?.let {put("poll[expires_in]", "$pollExpiresIn")}
            pollMultiple?.let {put("poll[multiple]", "$pollMultiple")}
            pollHideTotals?.let {put("poll[hide_totals]", "$pollHideTotals")}
            inReplyToId?.let {put("in_reply_to_id", "$inReplyToId")}
            sensitive?.let {put("sensitive", "$sensitive")}
            language?.let {put("language", "$language")}
            scheduledAt?.let {put("scheduled_at", "$scheduledAt")}

            if(visibility.isNotBlank()) put("visibility", visibility)
            if(spoilerText.isNotBlank()) put("spoiler_text", spoilerText)
        }

        val urlBuilder =url.newBuilder().apply {
            params.forEach{ param -> addQueryParameter(param.key, param.value)}
            mediaIds?.forEach { addQueryParameter("media_ids[]", it) }
            pollOptions?.forEach { addQueryParameter("poll[options][]", it) }
        }

        val requestBody = "{}".toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(urlBuilder.build())
            .addHeader("Authorization", "Bearer ${credential.accessToken}")
            .post(requestBody)
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e:Exception) {
            return null
        }
    }

    fun deleteStatus(id: String): Response? {
        val url = ("https://${credential.instance}/api/v1/statuses/$id").toHttpUrlOrNull()
            ?: return null

        val request = Request.Builder()
            .url(url.newBuilder().build())
            .addHeader("Authorization", "Bearer ${credential.accessToken}")
            .delete()
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e: Exception) {
            return null
        }
    }

    fun postAction(id: String, action: PostAction): Response? {
        val url = ("https://${credential.instance}/api/v1/statuses/$id/${action.value}").toHttpUrlOrNull()
            ?: return null

        val requestBody = "{}".toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url.newBuilder().build())
            .addHeader("Authorization", "Bearer ${credential.accessToken}")
            .post(requestBody)
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * 投稿をブーストした人のリストを取得
     */
    fun getWhoBoosted(id:String, maxId: String?, sinceId: String?): Response? {
        return getWhoActioned(id, ActionBy.BOOST, maxId, sinceId)
    }

    /**
     * 投稿をふぁぼった人のリストを取得
     */
    fun getWhoFavourited(id:String, maxId: String?, sinceId: String?): Response? {
        return getWhoActioned(id, ActionBy.FAVOURITE, maxId, sinceId)
    }
    private fun getWhoActioned(id: String, action: ActionBy, maxId: String?, sinceId: String?) : Response? {
        val url = ("https://${credential.instance}/api/v1/statuses/$id/${action.value}").toHttpUrlOrNull()
            ?: return null

        val urlBuilder =url.newBuilder().apply {
            addQueryParameter("max_id", maxId)
            addQueryParameter("since_id", sinceId)
            addQueryParameter("limit", "20")
        }

        val request = Request.Builder()
            .url(urlBuilder.build())
            .addHeader("Authorization", "Bearer ${credential.accessToken}")
            .get()
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e: Exception) {
            return null
        }
    }

    enum class PostAction(val value: String){
        FAVOURITE("favourite"),
        UNFAVOURITE("unfavourite"),
        BOOST("reblog"),
        UNBOOST("unreblog"),
        BOOKMARK("bookmark"),
        UNBOOKMARK("unbookmark"),
        PIN("pin"),
        UNPIN("unpin"),
    }

    enum class ActionBy(val value: String) {
        FAVOURITE("favourited_by"),
        BOOST("reblogged_by"),
    }
}