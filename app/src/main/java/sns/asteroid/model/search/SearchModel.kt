package sns.asteroid.model.search

import kotlinx.serialization.json.Json
import sns.asteroid.CustomApplication
import sns.asteroid.R
import sns.asteroid.api.entities.Account
import sns.asteroid.api.entities.Search
import sns.asteroid.api.entities.Status
import sns.asteroid.db.entities.Credential

class SearchModel(val credential: Credential) {
    private val limit = 20

    fun searchAll(query: String, offset: Int = 0): Result {
        if (query.isEmpty())
            return Result(isSuccess = false, toastMessage = getString(R.string.empty))

        val response =
            sns.asteroid.api.Search(credential.instance, credential.accessToken)
            .search(
                query = query,
                offset = offset,
                limit = limit,
                type = sns.asteroid.api.Search.Type.ALL,
            ) ?: return Result(isSuccess = false, toastMessage = getString(R.string.failed))

        if(!response.isSuccessful)
            return Result(isSuccess = false, toastMessage = response.body!!.string())
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        return try {
            val content = json.decodeFromString(Search.serializer(), response.body!!.string())
            Result(isSuccess = true, content = content)
        } catch (e: Exception) {
            Result(isSuccess = false, toastMessage = e.toString())
        } finally {
            response.close()
        }
    }

    fun findAccount(url:String): Account? {
        val response =
            sns.asteroid.api.Search(credential.instance, credential.accessToken)
                .search(
                    query = url,
                    offset = 0,
                    limit = 1,
                    type = sns.asteroid.api.Search.Type.ACCOUNTS,
                ) ?: return null

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        return try {
            json.decodeFromString(Search.serializer(), response.body!!.string()).accounts.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    fun findStatus(url: String): Status? {
        val response =
            sns.asteroid.api.Search(credential.instance, credential.accessToken)
                .search(
                    query = url,
                    offset = 0,
                    limit = 1,
                    type = sns.asteroid.api.Search.Type.STATUSES,
                ) ?: return null

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        return try {
            json.decodeFromString(Search.serializer(), response.body!!.string()).statuses.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    private fun getString(resId: Int): String {
        return CustomApplication.getApplicationContext().getString(resId)
    }

    data class Result(
        val isSuccess: Boolean,
        val content: Search? = null,
        val toastMessage: String? = null,
    )
}