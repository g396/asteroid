package sns.asteroid.model.timeline

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import sns.asteroid.R
import sns.asteroid.api.Accounts
import sns.asteroid.api.entities.Status
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.timeline.GettingContentsModel.Result

open class UserTimelineModel(
    credential: Credential,
    val userId: String,
    val subject: String,
): AbstractTimelineModel<Status>(credential) {
    override fun getContents(maxId: String?, sinceId: String?): Result<Status> {
        val response =
            Accounts(credential.instance, credential.accessToken).getStatuses(userId, maxId, sinceId, subject)
            ?: return Result(isSuccess = false, toastMessage = getString(R.string.failed))

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