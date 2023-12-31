package sns.asteroid.model.search

import kotlinx.serialization.json.Json
import sns.asteroid.R
import sns.asteroid.api.entities.Search
import sns.asteroid.api.entities.Status
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.timeline.GettingContentsModel

class StatusesSearchModel(credential: Credential, query: String, offset: Int): AbstractSearchModel<Status>(credential, query, offset) {
    override fun search(offset: Int): GettingContentsModel.Result<Status> {
        val response = sns.asteroid.api.Search(credential.instance, credential.accessToken)
            .search(
                query = query,
                offset = offset,
                limit = limit,
                sns.asteroid.api.Search.Type.STATUSES,
            ) ?: return GettingContentsModel.Result(isSuccess = false, toastMessage = getString(R.string.failed))

        if(!response.isSuccessful)
            return GettingContentsModel.Result<Status>(isSuccess = false, toastMessage = response.body!!.string())
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        return try {
            val content = json.decodeFromString(Search.serializer(), response.body!!.string())
            GettingContentsModel.Result(isSuccess = true, contents = content.statuses)
        } catch (e: Exception) {
            GettingContentsModel.Result(isSuccess = false, toastMessage = e.toString())
        } finally {
            response.close()
        }
    }
}