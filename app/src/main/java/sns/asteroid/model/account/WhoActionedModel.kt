package sns.asteroid.model.account

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import sns.asteroid.R
import sns.asteroid.api.Statuses
import sns.asteroid.api.entities.Account
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.timeline.AbstractTimelineModel
import sns.asteroid.model.timeline.GettingContentsModel.Result
import sns.asteroid.model.util.AttributeGetter

class WhoActionedModel(credential: Credential, val id: String, val action: Action): AbstractTimelineModel<Account>(credential) {
    override fun getContents(maxId: String?, sinceId: String?): Result<Account> {
        val response = when(action) {
            Action.BOOST -> Statuses(credential).getWhoBoosted(id, maxId, sinceId)
            Action.FAVOURITE -> Statuses(credential).getWhoFavourited(id, maxId, sinceId)
        } ?: return Result(isSuccess = false, toastMessage = getString(R.string.failed))

        if (!response.isSuccessful)
            return Result<Account>(isSuccess = false, toastMessage = response.code.toString())
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        val accounts = try {
            json.decodeFromString(ListSerializer(Account.serializer()), response.body!!.string())
        } catch (e: Exception) {
            return Result(isSuccess = false, toastMessage = e.toString())
        }.also { response.close() }

        return Result(
            isSuccess = true,
            contents = accounts,
            maxId = AttributeGetter.getMaxIdFromHttpLinkHeader(response),
            sinceId = AttributeGetter.getSinceIdFromHttpLinkHeader(response),
            toastMessage = if(accounts.isEmpty()) getString(R.string.end_of_list) else null,
        ).also {
            android.util.Log.d("maxId", it.maxId.toString())
            android.util.Log.d("sinceId", it.sinceId.toString())
        }
    }

    enum class Action {
        BOOST,
        FAVOURITE,
    }
}