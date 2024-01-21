package sns.asteroid.model.timeline

import kotlinx.serialization.json.Json
import sns.asteroid.CustomApplication
import sns.asteroid.R
import sns.asteroid.api.Statuses
import sns.asteroid.api.entities.Context
import sns.asteroid.api.entities.Status
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.timeline.GettingContentsModel.Result

class ContextModel(
    private val credential: Credential,
    private val status: Status
    ): GettingContentsModel<Status> {
    override fun getLatest(): Result<Status> {
        return getOlder()
    }

    override fun getOlder(): Result<Status> {
        val client = Statuses(credential.instance, credential.accessToken)
        val response = client.getContext(status.id)
            ?: return Result(isSuccess = false, toastMessage = getString(R.string.failed))

        if(!response.isSuccessful)
            return Result(isSuccess = false, toastMessage = response.body!!.string())

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        val context = json.decodeFromString(Context.serializer(), response.body!!.string())

        val statuses = mutableListOf(status).apply {
            context.ancestors?.let {
                addAll(0, it)
            }
            context.descendants?.let {
                addAll(it)
            }
        }

        return Result(isSuccess = true, contents = statuses)
    }

    override fun reload(): Result<Status> {
        return getOlder()
    }

    private fun getString(resId: Int): String {
        return CustomApplication.getApplicationContext().getString(resId)
    }
}