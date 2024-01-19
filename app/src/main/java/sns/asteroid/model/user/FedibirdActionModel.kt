package sns.asteroid.model.user

import kotlinx.serialization.json.Json
import sns.asteroid.CustomApplication
import sns.asteroid.R
import sns.asteroid.api.EmojiReactions
import sns.asteroid.api.entities.Status
import sns.asteroid.db.entities.Credential

class FedibirdActionModel(val credential: Credential) {

    data class Result(
        val isSuccess: Boolean,
        val status: Status?,
        val toastMessage: String,
    )

    fun putEmojiReactions(statusId: String, emojiShortCode: String): Result {
        val client = EmojiReactions(credential.instance, credential.accessToken)
        val response = client.putEmojiReactions(statusId, emojiShortCode)
            ?: return Result(false, status = null, toastMessage = getString(R.string.failed))

        if(!response.isSuccessful)
            return Result(false, status = null, toastMessage = getString(R.string.failed))
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        val status = json.decodeFromString(Status.serializer(), response.body!!.string())

        return Result(true, status = status, toastMessage = getString(R.string.reacted))
            .also { response.close() }
    }

    fun deleteEmojiReactions(statusId: String, emojiShortCode: String): Result {
        val client = EmojiReactions(credential.instance, credential.accessToken)
        val response = client.deleteEmojiReactions(statusId, emojiShortCode)
            ?: return Result(false, status = null, toastMessage = getString(R.string.failed))

        if(!response.isSuccessful)
            return Result(false, status = null, toastMessage = getString(R.string.failed))
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        val status = json.decodeFromString(Status.serializer(), response.body!!.string())

        return Result(true, status = status, toastMessage = getString(R.string.undo))
            .also { response.close() }
    }

    fun getString(resId: Int): String {
        return CustomApplication.getApplicationContext().getString(resId)
    }
}