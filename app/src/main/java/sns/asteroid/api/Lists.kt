package sns.asteroid.api

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import sns.asteroid.db.entities.Credential

class Lists(val credential: Credential) {
    fun getLists(): Response? {
        val url = ("https://${credential.instance}/api/v1/lists").toHttpUrlOrNull()
            ?: return null

        val urlBuilder =url.newBuilder()

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

    fun getSingleList(id: String): Response? {
        val url = ("https://${credential.instance}/api/v1/lists/$id").toHttpUrlOrNull()
            ?: return null

        val urlBuilder = url.newBuilder()

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

    fun updateList(id: String, title: String, repliesPolicy: String, exclusive: Boolean?): Response? {
        val url = ("https://${credential.instance}/api/v1/lists/$id").toHttpUrlOrNull()
            ?: return null

        val multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("title", title)
            .addFormDataPart("replies_policy", repliesPolicy)
            .addFormDataPart("exclusive", "$exclusive")
            .build()

        val request = Request.Builder()
            .url(url.newBuilder().build())
            .addHeader("Authorization", "Bearer ${credential.accessToken}")
            .put(multipartBody)
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e: Exception) {
            null
        }
    }

    fun createList(title: String): Response? {
        val url = ("https://${credential.instance}/api/v1/lists").toHttpUrlOrNull()
            ?: return null

        val multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("title", title)
            .build()

        val request = Request.Builder()
            .url(url.newBuilder().build())
            .addHeader("Authorization", "Bearer ${credential.accessToken}")
            .post(multipartBody)
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e: Exception) {
            null
        }
    }

    fun deleteList(id: String): Response? {
        val url = ("https://${credential.instance}/api/v1/lists/$id").toHttpUrlOrNull()
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

    fun getAccounts(maxId: String?, sinceId: String?, listId: String): Response? {
        val url = ("https://${credential.instance}/api/v1/lists/$listId/accounts").toHttpUrlOrNull()
            ?: return null

        val urlBuilder = url.newBuilder().apply {
            // 差分比較のために全数取得する必要があるのでlimitを無制限にしている
            // ほんとは直接定数を置かないようにしたいところ
            addQueryParameter("limit", "0")
            maxId?.let { addQueryParameter("max_id", it) }
            sinceId?.let { addQueryParameter("since_id", it) }
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

    fun addAccount(userId: String, listId: String): Response? {
        val url = ("https://${credential.instance}/api/v1/lists/$listId/accounts").toHttpUrlOrNull()
            ?: return null

        val multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("account_ids[]", userId)
            .build()

        val request = Request.Builder()
            .url(url.newBuilder().build())
            .addHeader("Authorization", "Bearer ${credential.accessToken}")
            .post(multipartBody)
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e: Exception) {
            null
        }
    }

    fun removeAccount(userId: String, listId: String): Response? {
        val url = ("https://${credential.instance}/api/v1/lists/$listId/accounts").toHttpUrlOrNull()
            ?: return null

        val urlBuilder = url.newBuilder().apply {
            addQueryParameter("account_ids[]", userId)
        }
        val requestBody = "{}".toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(urlBuilder.build())
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