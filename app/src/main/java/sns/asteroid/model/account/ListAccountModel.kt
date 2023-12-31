package sns.asteroid.model.account

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import sns.asteroid.R
import sns.asteroid.api.Lists
import sns.asteroid.api.entities.Account
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.util.AttributeGetter
import sns.asteroid.model.timeline.AbstractTimelineModel
import sns.asteroid.model.timeline.GettingContentsModel

class ListAccountModel(credential: Credential, val listId: String): AbstractTimelineModel<Account>(credential) {
    override fun getContents(maxId: String?, sinceId: String?): GettingContentsModel.Result<Account> {
        val client = Lists(credential)
        val response = client.getAccounts(maxId, sinceId, listId)
            ?: return GettingContentsModel.Result(
                isSuccess = false,
                toastMessage = getString(R.string.failed)
            )

        if (!response.isSuccessful)
            return GettingContentsModel.Result<Account>(
                isSuccess = false,
                toastMessage = response.body?.string()
            )
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        val accounts = json.decodeFromString(ListSerializer(Account.serializer()), response.body!!.string())
        val toastMessage = if(accounts.isEmpty()) getString(R.string.end_of_list) else null

        return GettingContentsModel.Result(
            isSuccess = true,
            contents = accounts,
            maxId = AttributeGetter.getMaxIdFromHttpLinkHeader(response),
            sinceId = AttributeGetter.getSinceIdFromHttpLinkHeader(response),
            toastMessage = toastMessage,
        ).also { response.close() }
    }
}