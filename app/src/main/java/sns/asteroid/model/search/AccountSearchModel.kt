package sns.asteroid.model.search

import kotlinx.serialization.json.Json
import sns.asteroid.R
import sns.asteroid.api.entities.Account
import sns.asteroid.api.entities.Search
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.timeline.GettingContentsModel

class AccountSearchModel(credential: Credential, query: String, offset: Int): AbstractSearchModel<Account>(credential, query, offset) {
    override fun search(offset: Int): GettingContentsModel.Result<Account> {
        val response =
            sns.asteroid.api.Search(credential.instance, credential.accessToken)
            .search(
                query = query,
                offset = offset,
                limit = limit,
                type = sns.asteroid.api.Search.Type.ACCOUNTS,
            ) ?: return GettingContentsModel.Result(isSuccess = false, toastMessage = getString(R.string.failed))

        if(!response.isSuccessful)
            return GettingContentsModel.Result<Account>(isSuccess = false, toastMessage = response.body!!.string())
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        return try {
            val content = json.decodeFromString(Search.serializer(), response.body!!.string())
            GettingContentsModel.Result(isSuccess = true, contents = content.accounts)
        } catch (e: Exception) {
            GettingContentsModel.Result(isSuccess = false, toastMessage = e.toString())
        } finally {
            response.close()
        }
    }
}