package sns.asteroid.model.timeline

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import sns.asteroid.R
import sns.asteroid.api.Notifications
import sns.asteroid.api.entities.Notification
import sns.asteroid.api.entities.Notification.Companion.margeSameReaction
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.timeline.GettingContentsModel.Result


class NotificationTimelineModel(
    credential: Credential,
    val onlyMention: Boolean = false,
): AbstractTimelineModel<Notification>(credential) {
    override fun getContents(maxId: String?, sinceId: String?): Result<Notification> {
        val client = Notifications(credential)
        val response = client.getAll(maxId, sinceId, onlyMention)
            ?: return Result(isSuccess = false, toastMessage = getString(R.string.failed_loading))

        if(!response.isSuccessful)
            return Result<Notification>(isSuccess = false, toastMessage = response.body!!.string())
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        val notifications =
            json.decodeFromString(ListSerializer(Notification.serializer()), response.body!!.string())

        if(notifications.isEmpty())
            return Result<Notification>(isSuccess = true)
                .also { response.close() }

        return Result(
            isSuccess   = true,
            contents    = notifications.margeSameReaction(),
            maxId       = notifications.last().id,
            sinceId     = notifications.first().id,
        ).also { response.close() }
    }
}