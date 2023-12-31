package sns.asteroid.model.user

import kotlinx.serialization.json.Json
import sns.asteroid.CustomApplication
import sns.asteroid.R
import sns.asteroid.api.Polls
import sns.asteroid.api.entities.Poll
import sns.asteroid.db.entities.Credential

class PollModel(val credential: Credential) {
    data class Result(
        val isSuccess: Boolean,
        val poll: Poll? = null,
        val toastMessage: String? = null,
    )

    fun vote(id: String, choices: List<Int>): Result {
        val response = Polls(credential.instance, credential.accessToken)
            .vote(id, choices)
            ?: return Result(isSuccess = false)

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        if (!response.isSuccessful)
            return Result(isSuccess = false, toastMessage = response.body?.string())
                .also { response.close() }

        return try {
            val poll = json.decodeFromString(Poll.serializer(), response.body!!.string())
            Result(isSuccess = true, poll = poll, toastMessage = getString(R.string.voted) )
        } catch (e: Exception) {
            Result(isSuccess = false, toastMessage = e.toString())
        } finally {
            response.close()
        }
    }

    private fun getString(resId: Int): String {
        return CustomApplication.getApplicationContext().getString(resId)
    }
}