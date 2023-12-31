package sns.asteroid.model.timeline

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import sns.asteroid.R
import sns.asteroid.api.Timelines
import sns.asteroid.api.entities.Status
import sns.asteroid.db.entities.Credential

class HashtagTimelineModel(
    credential: Credential,
    private val hashtag: String,
): AbstractTimelineModel<Status>(credential) {
    override fun getContents(maxId: String?, sinceId: String?): GettingContentsModel.Result<Status> {
        val client = Timelines(credential)
        val response = client.getHashtag(hashtag, maxId, sinceId)
            ?: return GettingContentsModel.Result(
                isSuccess = false,
                toastMessage = getString(R.string.failed_loading)
            )

        if(!response.isSuccessful)
            return GettingContentsModel.Result<Status>(
                isSuccess = false,
                toastMessage = response.body!!.string()
            )
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        val statuses = json.decodeFromString(ListSerializer(Status.serializer()), response.body!!.string())

        if(statuses.isEmpty()) return GettingContentsModel.Result<Status>(isSuccess = true)
            .also { response.close() }

        return GettingContentsModel.Result(
            isSuccess = true,
            contents = statuses,
            toastMessage = null,
            maxId = statuses.last().id,
            sinceId = statuses.first().id,
        ).also { response.close() }
    }

}