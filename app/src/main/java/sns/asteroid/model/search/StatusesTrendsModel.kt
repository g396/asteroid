package sns.asteroid.model.search

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import sns.asteroid.R
import sns.asteroid.api.Trends
import sns.asteroid.api.entities.Status
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.timeline.GettingContentsModel

class StatusesTrendsModel(credential: Credential): AbstractSearchModel<Status>(credential, query = "", offset = 0) {
    override fun search(offset: Int): GettingContentsModel.Result<Status> {
        val response = Trends(credential.instance, credential.accessToken).getTrendsStatuses(offset, limit)
            ?: return GettingContentsModel.Result(isSuccess = false, toastMessage = getString(R.string.failed))

        if(!response.isSuccessful)
            return GettingContentsModel.Result<Status>(isSuccess = false, toastMessage = response.body?.string())
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        return try {
            val content = json.decodeFromString(ListSerializer(Status.serializer()), response.body!!.string())
            GettingContentsModel.Result(isSuccess = true, contents = content)
        } catch (e: Exception) {
            GettingContentsModel.Result(isSuccess = false, toastMessage = e.toString())
        } finally {
            response.close()
        }

    }
}