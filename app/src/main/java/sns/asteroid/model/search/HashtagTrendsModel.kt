package sns.asteroid.model.search

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import sns.asteroid.R
import sns.asteroid.api.Trends
import sns.asteroid.api.entities.Tag
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.timeline.GettingContentsModel

class HashtagTrendsModel(credential: Credential): AbstractSearchModel<Tag>(credential, query = "", offset = 0) {
    override fun search(offset: Int): GettingContentsModel.Result<Tag> {
        val client = Trends(credential.instance, credential.accessToken)

        // TODO: 鯖のバージョンを見て判断する
        val response =
            if (credential.instance == "fedibird.com") { client.getTrends(offset, limit) }
            else { client.getTrendsTags(offset, limit) }
            ?: return GettingContentsModel.Result(isSuccess = false, toastMessage = getString(R.string.failed))

        if(!response.isSuccessful)
            return GettingContentsModel.Result<Tag>(isSuccess = false, toastMessage = response.body?.string())
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        return try {
            val content = json.decodeFromString(ListSerializer(Tag.serializer()), response.body!!.string())
            GettingContentsModel.Result(isSuccess = true, contents = content)
        } catch (e: Exception) {
            GettingContentsModel.Result(isSuccess = false, toastMessage = e.toString())
        } finally {
            response.close()
        }

    }
}