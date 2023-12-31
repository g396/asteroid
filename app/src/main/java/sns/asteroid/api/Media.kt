package sns.asteroid.api

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import sns.asteroid.db.entities.Credential

class Media(val credential: Credential) {
    fun postMedia(file: ByteArray, fileName: String, mimeType: String, description: String?): Response? {
        val url = ("https://${credential.instance}/api/v1/media").toHttpUrlOrNull()
            ?: return null

        val requestBody = file.toRequestBody(mimeType.toMediaTypeOrNull())

        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", fileName, requestBody)
            .run {
                if (description != null) addFormDataPart("description", description)
                else this
            }.build()


        val urlBuilder = url.newBuilder()

        val request = Request.Builder()
            .url(urlBuilder.build())
            .addHeader("Authorization", "Bearer ${credential.accessToken}")
            .post(multipartBody)
            .build()

        return try {
            OkHttpClient().newCall(request).execute()
        } catch (e: Exception) {
            return null
        }
    }

    companion object {
        fun download(url: String): Response? {
            val urlBuilder = url.toHttpUrlOrNull()?.newBuilder()
                ?: return null

            val request = Request.Builder()
                .url(urlBuilder.build())
                .build()

            return try {
                OkHttpClient().newCall(request).execute()
            } catch (e: Exception) {
                null
            }
        }
    }
}