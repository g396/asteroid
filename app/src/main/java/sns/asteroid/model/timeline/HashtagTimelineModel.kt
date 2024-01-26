package sns.asteroid.model.timeline

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import sns.asteroid.R
import sns.asteroid.api.Timelines
import sns.asteroid.api.entities.Status
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.timeline.GettingContentsModel.Result

class HashtagTimelineModel(
    credential: Credential,
    private val hashtag: String,
): AbstractTimelineModel<Status>(credential) {
    override fun getContents(maxId: String?, sinceId: String?): Result<Status> {
        val client = Timelines(credential.instance, credential.accessToken)
        val response = client.getHashtag(hashtag, maxId, sinceId)
            ?: return Result(isSuccess = false, toastMessage = getString(R.string.failed_loading))

        if(!response.isSuccessful)
            return Result<Status>(isSuccess = false, toastMessage = response.body?.string())
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        return try {
            val statuses =
                json.decodeFromString(ListSerializer(Status.serializer()), response.body!!.string())
            Result(
                isSuccess   = true,
                contents    = statuses,
                maxId       = statuses.lastOrNull()?.id,
                sinceId     = statuses.firstOrNull()?.id,
            )
        } catch (e: Exception) {
            Result(isSuccess = false, toastMessage = e.toString())
        } finally {
            response.close()
        }
    }
}