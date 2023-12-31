package sns.asteroid.model.user

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import sns.asteroid.CustomApplication
import sns.asteroid.R
import sns.asteroid.api.Accounts
import sns.asteroid.api.Lists
import sns.asteroid.api.entities.ListTimeline
import sns.asteroid.db.entities.Credential

class ListsModel(val credential: Credential) {
    data class Result(
        val isSuccess: Boolean,
        val lists: List<ListTimeline>? = null,
        val message: String?
    )

    fun getAll(): Result {
        val client = Lists(credential)
        val response = client.getLists()
            ?: return Result(isSuccess = false, lists = null, message = getString(R.string.failed))

        if(!response.isSuccessful)
            return Result(isSuccess = false, lists = null, message = response.body!!.string())

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        val lists = json.decodeFromString(ListSerializer(ListTimeline.serializer()), response.body!!.string())

        return Result(isSuccess = true, lists = lists, message = null)
    }

    fun get(id: String): Result {
        val response = Lists(credential).getSingleList(id)
            ?: return Result(isSuccess = false, message = getString(R.string.failed))

        if(!response.isSuccessful)
            return Result(isSuccess = false, message = response.code.toString())
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        return try {
            val list = json.decodeFromString(ListTimeline.serializer(), response.body!!.string())
            Result(isSuccess = true, lists = listOf(list), message = null)
        } catch (e: Exception) {
            Result(isSuccess = false, message = e.toString())
        } finally {
            response.close()
        }
    }

    fun getInAccount(userId: String): Result {
        val client = Accounts(credential)
        val response = client.getList(userId)
            ?: return Result(isSuccess = false, lists = null, message = getString(R.string.failed))

        if(!response.isSuccessful)
            return Result(isSuccess = false, lists = null, message = response.body!!.string())

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        val lists = json.decodeFromString(ListSerializer(ListTimeline.serializer()), response.body!!.string())

        return Result(isSuccess = true, lists = lists, message = null)
    }

    fun createList(title: String): ListTimeline? {
        val client = Lists(credential)
        val response = client.createList(title)
            ?: return null

        if (!response.isSuccessful)
            return null

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        return json.decodeFromString(ListTimeline.serializer(), response.body!!.string())
    }

    fun updateList(id: String, title: String, repliesPolicy: String, exclusive: Boolean?): Result {
        val response = Lists(credential).updateList(id, title, repliesPolicy, exclusive)
            ?: return Result(isSuccess = false, message = getString(R.string.failed))

        if (!response.isSuccessful)
            return Result(isSuccess = false, message = response.body?.string())

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        return try {
            val list = json.decodeFromString(ListTimeline.serializer(), response.body!!.string())
            Result(isSuccess = true, lists = listOf(list), message = null)
        } catch (e: Exception) {
            Result(isSuccess = false, message = e.toString())
        } finally {
            response.close()
        }
    }

    fun deleteList(id: String): Boolean {
        val client = Lists(credential)
        val response = client.deleteList(id) ?: return false
        return response.isSuccessful
    }

    fun addAccountToList(userId: String, listId: String): Boolean {
        val client = Lists(credential)
        val response = client.addAccount(userId, listId) ?: return false
        return response.isSuccessful
    }

    fun removeAccountFromList(userId: String, listId: String): Boolean {
        val client = Lists(credential)
        val response = client.removeAccount(userId, listId) ?: return false
        return response.isSuccessful
    }

    fun getString(resId: Int): String {
        return CustomApplication.getApplicationContext().getString(resId)
    }
}