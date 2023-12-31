package sns.asteroid.model.timeline

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import sns.asteroid.R
import sns.asteroid.api.Accounts
import sns.asteroid.api.entities.Status
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.timeline.GettingContentsModel.Result

class PinnedUserTimelineModel(
    credential: Credential,
    userId: String,
): UserTimelineModel(credential, userId, "posts") {
    private var isFirst = true

    override fun getContents(maxId: String?, sinceId: String?): Result<Status> {
        val posts = super.getContents(maxId, sinceId)
        if(!posts.isSuccess or !isFirst) return posts

        val client = Accounts(credential)
        val response = client.getStatuses(userId, maxId, sinceId, subject, isPinned = true)
            ?: return Result(isSuccess = false, toastMessage = getString(R.string.failed))

        if(!response.isSuccessful)
            return Result<Status>(isSuccess = false, toastMessage = response.body?.string())
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        val pinned = json.decodeFromString(ListSerializer(Status.serializer()), response.body!!.string())
            .onEach { it.pinned = true }

        if(pinned.isEmpty()) return posts.also {
            response.close()
            isFirst = false
        }

        return Result(
            isSuccess       = true,
            contents        = pinned.plus(posts.contents?.filter { !it.pinned } as List<Status>),
            maxId           = posts.maxId,
            sinceId         = posts.sinceId,
        ).also {
            response.close()
            isFirst = false
        }
    }
}