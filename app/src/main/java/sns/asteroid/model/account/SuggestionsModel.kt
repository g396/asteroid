package sns.asteroid.model.account

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import sns.asteroid.R
import sns.asteroid.api.Suggestions
import sns.asteroid.api.entities.Account
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.search.AbstractSearchModel
import sns.asteroid.model.timeline.GettingContentsModel.Result

class SuggestionsModel(credential: Credential): AbstractSearchModel<Account>(credential, query = "", offset = 0) {
    override fun search(offset: Int): Result<Account> {
        val response = Suggestions(credential.instance, credential.accessToken).getSuggestions()
            ?: return Result(isSuccess = false, toastMessage = getString(R.string.failed))

        if (!response.isSuccessful)
            return Result<Account>(isSuccess = false, toastMessage = response.code.toString())
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        val accounts = json.decodeFromString(ListSerializer(Account.serializer()), response.body!!.string())
        val toastMessage = if(accounts.isEmpty()) getString(R.string.end_of_list) else null

        return Result(
            isSuccess = true,
            contents = accounts,
            toastMessage = toastMessage,
        ).also { response.close() }
    }
}