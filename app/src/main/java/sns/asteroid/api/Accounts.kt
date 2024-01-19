package sns.asteroid.api

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class Accounts(
    private val server: String,
    private val accessToken: String,
) {
    fun getAccountByAcct(acct: String): Response? {
        val url = ("https://$server/api/v1/accounts/lookup").toHttpUrlOrNull()
            ?: return null

        val urlBuilder = url.newBuilder().apply {
            addQueryParameter("acct", acct)
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

    fun getStatuses(id: String, maxId: String?, sinceId: String?, subject: String): Response? {
        return getStatuses(id, maxId, sinceId, subject, false)
    }
    
    fun getStatuses(id: String, maxId: String?, sinceId: String?, subject: String, isPinned: Boolean): Response? {
        val url = ("https://$server/api/v1/accounts/${id}/statuses").toHttpUrlOrNull()
            ?: return null

        val urlBuilder = url.newBuilder().apply {
            addQueryParameter("limit", "40")
            addQueryParameter("pinned", "$isPinned")

            maxId?.let {
                addQueryParameter("max_id", "$maxId")
            }
            sinceId?.let {
                addQueryParameter("since_id", "$sinceId")
            }

            if(subject.contains("media"))
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

    fun getFollowers(id: String, maxId: String?, sinceId: String?): Response? {
        val url = ("https://$server/api/v1/accounts/${id}/followers").toHttpUrlOrNull()
            ?: return null

        val urlBuilder = url.newBuilder().apply {
            addQueryParameter("limit", "40")

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

    fun getFollowing(id: String, maxId: String?, sinceId: String?): Response? {
        val url = ("https://$server/api/v1/accounts/${id}/following").toHttpUrlOrNull()
            ?: return null

        val urlBuilder = url.newBuilder().apply {
            addQueryParameter("limit", "40")

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

    fun getBlocks(maxId: String?, sinceId: String?): Response? {
        val url = ("https://$server/api/v1/blocks").toHttpUrlOrNull()
            ?: return null

        val urlBuilder = url.newBuilder().apply {
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
        } catch (e:Exception) {
            return null
        }
    }

    fun getMutes(maxId: String?, sinceId: String?): Response? {
        val url = ("https://$server/api/v1/mutes").toHttpUrlOrNull()
            ?: return null

        val urlBuilder = url.newBuilder().apply {
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
        } catch (e:Exception) {
            return null
        }
    }

    fun getFavourites(maxId: String?, sinceId: String?): Response? {
        val url = ("https://$server/api/v1/favourites").toHttpUrlOrNull()
            ?: return null

        val urlBuilder = url.newBuilder().apply {
            addQueryParameter("limit", "40")

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
        } catch (e:Exception) {
            return null
        }
    }

    fun getBookmarks(maxId: String?, sinceId: String?): Response? {
        val url = ("https://$server/api/v1/bookmarks").toHttpUrlOrNull()
            ?: return null

        val urlBuilder = url.newBuilder().apply {
            addQueryParameter("limit", "40")

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
        } catch (e:Exception) {
            return null
        }
    }

    fun getRelationships(ids: List<String>): Response? {
        val url = ("https://$server/api/v1/accounts/relationships").toHttpUrlOrNull()
            ?: return null

        val urlBuilder = url.newBuilder().apply {
            ids.forEach{
                addQueryParameter("id[]", it)
            }
        }

        val request = Request.Builder()
            .url(urlBuilder.build())
            .addHeader("Authorization", "Bearer $accessToken")
            .get()
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e:Exception) {
            return null
        }
    }

    fun getList(id: String): Response? {
        val url = ("https://$server/api/v1/accounts/${id}/lists").toHttpUrlOrNull()
            ?: return null

        val urlBuilder = url.newBuilder()

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


    fun postUserAction(id: String, action: PostAction): Response? {
        val url = ("https://$server/api/v1/accounts/${id}/${action.endpoint}").toHttpUrlOrNull()
            ?: return null

        val urlBuilder = url.newBuilder().apply {
            when(action) {
                PostAction.NOTIFY           -> addQueryParameter("notify", "true")
                PostAction.DISABLE_NOTIFY   -> addQueryParameter("notify", "false")
                PostAction.SHOW_BOOST       -> addQueryParameter("reblogs", "true")
                PostAction.HIDE_BOOST       -> addQueryParameter("reblogs", "false")
                PostAction.MUTE             -> addQueryParameter("notifications", "false")
                PostAction.MUTE_NOTIFICATION-> addQueryParameter("notifications", "true")
                else -> {}
            }
        }
        val requestBody = "{}".toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(urlBuilder.build())
            .addHeader("Authorization", "Bearer $accessToken")
            .post(requestBody)
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e:Exception) {
            return null
        }
    }

    enum class PostAction(val endpoint: String) {
        FOLLOW("follow"),
        UNFOLLOW("unfollow"),
        REQUEST_FOLLOW("follow"), // エンドポイントはフォロー時と共通
        UNDO_REQUEST_FOLLOW("unfollow"), // エンドポイントはフォロー解除時と共通
        BLOCK("block"),
        UNBLOCK("unblock"),
        MUTE("mute"),
        MUTE_NOTIFICATION("mute"),
        UNMUTE("unmute"),
        NOTIFY("follow"),
        DISABLE_NOTIFY("follow"),
        SHOW_BOOST("follow"),
        HIDE_BOOST("follow"),
    }

    fun postAcceptOrRejectFollowRequests(accountId: String, isAccept: Boolean): Response? {
        val path = if (isAccept) "authorize" else "reject"
        val url = ("https://$server/api/v1/follow_requests/${accountId}/${path}").toHttpUrlOrNull()
            ?: return null

        val requestBody = "{}".toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url.newBuilder().build())
            .addHeader("Authorization", "Bearer $accessToken")
            .post(requestBody)
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e:Exception) {
            return null
        }
    }

    fun patchUpdateCredentials(
        displayName: String? = null,
        note: String? = null,
        fields: List<Map<String, String>>? = null,
        isLocked: Boolean? = null,
        isBot: Boolean? = null,
        avatar: ByteArray? = null,
        header: ByteArray? = null,
    ): Response? {
        val url = ("https://$server/api/v1/accounts/update_credentials").toHttpUrlOrNull()
            ?: return null

        val urlBuilder = url.newBuilder().apply {
            displayName?.let {
                addQueryParameter("display_name", it)
            }
            note?.let {
                addQueryParameter("note", it)
            }
            isLocked?.let {
                addQueryParameter("locked", "$it")
            }
            isBot?.let {
                addQueryParameter("bot", "$it")
            }
            fields?.forEachIndexed { index, field ->
                addQueryParameter("fields_attributes[$index][name]", field["name"])
                addQueryParameter("fields_attributes[$index][value]", field["value"])
            }
        }

        val multipartBody =
            if ((avatar == null) and (header == null))
                "{}".toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            else {
                val avatarPart = avatar?.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                val headerPart = header?.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                MultipartBody.Builder().setType(MultipartBody.FORM).apply {
                    avatarPart?.let { addFormDataPart("avatar", "avatar", it) }
                    headerPart?.let { addFormDataPart("header", "header", it) }
                }.build()
            }

        val request = Request.Builder()
            .url(urlBuilder.build())
            .addHeader("Authorization", "Bearer $accessToken")
            .patch(multipartBody)
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e:Exception) {
            return null
        }
    }
}