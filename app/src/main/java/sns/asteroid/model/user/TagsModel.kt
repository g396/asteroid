package sns.asteroid.model.user

import kotlinx.serialization.json.Json
import sns.asteroid.CustomApplication
import sns.asteroid.R
import sns.asteroid.api.Hashtags
import sns.asteroid.api.entities.Tag
import sns.asteroid.db.entities.Credential

class TagsModel(val credential: Credential) {

    fun getTag(id: String): Result {
        val client = Hashtags(credential.instance, credential.accessToken)

        val response = client.getTag(id)
            ?: return Result(isSuccess = false, message = getString(R.string.failed_loading))

        if (!response.isSuccessful)
            return Result(isSuccess = false, message = response.body?.string() ?: getString(R.string.failed_loading))
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        return try {
            val tag = json.decodeFromString(Tag.serializer(), response.body!!.string())
            Result(isSuccess = true, tag = tag, message = getString(R.string.followed))
        } catch (e: Exception) {
            Result(isSuccess = false, message = e.toString())
        } finally {
            response.close()
        }
    }

    fun followTag(id: String): Result {
        val client = Hashtags(credential.instance, credential.accessToken)

        val response = client.followTag(id)
            ?: return Result(isSuccess = false, message = getString(R.string.failed_loading))

        if (!response.isSuccessful)
            return Result(isSuccess = false, message = response.body?.string() ?: getString(R.string.failed_loading))
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        return try {
            val tag = json.decodeFromString(Tag.serializer(), response.body!!.string())
            Result(isSuccess = true, tag = tag, message = getString(R.string.followed))
        } catch (e: Exception) {
            Result(isSuccess = false, message = e.toString())
        } finally {
            response.close()
        }
    }

    fun unfollowTag(id: String): Result {
        val client = Hashtags(credential.instance, credential.accessToken)

        val response = client.unfollowTag(id)
            ?: return Result(isSuccess = false, message = getString(R.string.failed_loading))

        if (!response.isSuccessful)
            return Result(isSuccess = false, message = response.body?.string() ?: getString(R.string.failed_loading))
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        return try {
            val tag = json.decodeFromString(Tag.serializer(), response.body!!.string())
            Result(isSuccess = true, tag = tag, message = getString(R.string.unfollowed))
        } catch (e: Exception) {
            Result(isSuccess = false, message = e.toString())
        } finally {
            response.close()
        }
    }

    fun getString(resId: Int): String {
        return CustomApplication.getApplicationContext().getString(resId)
    }

    data class Result(
        val isSuccess: Boolean,
        val tag: Tag? = null,
        val message: String,
    )
}