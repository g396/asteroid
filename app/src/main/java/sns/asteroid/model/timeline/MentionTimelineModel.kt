package sns.asteroid.model.timeline

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import sns.asteroid.R
import sns.asteroid.api.Notifications
import sns.asteroid.api.entities.Notification
import sns.asteroid.api.entities.Status
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.timeline.GettingContentsModel.Result

class MentionTimelineModel(
    credential: Credential,
): AbstractTimelineModel<Status>(credential) {
    override fun getContents(maxId: String?, sinceId: String?): Result<Status> {
        val response =
            Notifications(credential.instance, credential.accessToken).getAll(maxId, sinceId, onlyMention = true)
            ?: return Result(isSuccess = false, toastMessage = getString(R.string.failed_loading))

        if(!response.isSuccessful)
            return Result<Status>(isSuccess = false, toastMessage = response.body?.string())
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        return try {
            val notifications = json.decodeFromString(ListSerializer(Notification.serializer()), response.body!!.string())
            Result(
                isSuccess   = true,
                contents    = notifications.mapNotNull { it.status },
                maxId       = notifications.lastOrNull()?.id,
                sinceId     = notifications.firstOrNull()?.id
            )
        } catch (e: Exception) {
            Result(isSuccess = false, toastMessage = e.toString())
        } finally {
            response.close()
        }
    }
}