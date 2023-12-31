package sns.asteroid.model.hashtag

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import sns.asteroid.R
import sns.asteroid.api.Hashtags
import sns.asteroid.api.entities.Tag
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.util.AttributeGetter
import sns.asteroid.model.timeline.AbstractTimelineModel
import sns.asteroid.model.timeline.GettingContentsModel.Result

class FollowedTagsModel(
    credential: Credential
):AbstractTimelineModel<Tag>(credential) {
    override fun getContents(maxId: String?, sinceId: String?): Result<Tag> {
        val client = Hashtags(credential.instance, credential.accessToken)
        val response = client.getFollowedTags(maxId, sinceId)
            ?: return Result(isSuccess = false, toastMessage = getString(R.string.failed_loading))

        if (!response.isSuccessful)
            return Result<Tag>(isSuccess = false, toastMessage = response.body?.string())
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        return try {
            val statuses = json.decodeFromString(ListSerializer(Tag.serializer()), response.body!!.string())
            Result(
                isSuccess = true,
                contents = statuses,
                maxId = AttributeGetter.getMaxIdFromHttpLinkHeader(response),
                sinceId = AttributeGetter.getSinceIdFromHttpLinkHeader(response),
            )
        } catch (e: Exception) {
            Result(isSuccess = false, toastMessage = e.toString())
        } finally {
            response.close()
        }
    }
}