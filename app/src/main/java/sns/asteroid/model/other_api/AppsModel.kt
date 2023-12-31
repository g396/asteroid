package sns.asteroid.model.other_api

import kotlinx.serialization.json.Json
import sns.asteroid.CustomApplication
import sns.asteroid.R
import sns.asteroid.api.Apps
import sns.asteroid.api.entities.Application

/**
 * サーバに対し
 * クライアントの情報を登録・クライアントのキーを取得
 */
class AppsModel(private val server: String, private val appName: String) {
    data class Result(
        val isSuccess: Boolean,
        val application: Application? = null,
        val toastMessage: String?
    )

    fun createApps(): Result {
        val client = Apps(server, appName)
        val response = client.createApps()
            ?: return Result(isSuccess = false, toastMessage = getString(R.string.failed))

        if(!response.isSuccessful)
            return Result(isSuccess = false, toastMessage = response.body?.string())
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        return try {
            val application = json.decodeFromString(Application.serializer(), response.body!!.string())
            Result(isSuccess = true, application = application, toastMessage = null)
        } catch (e: Exception) {
            Result(isSuccess = false, toastMessage = e.toString())
        } finally {
            response.close()
        }
    }

    fun getString(resId: Int): String {
        return CustomApplication.getApplicationContext().getString(resId)
    }
}