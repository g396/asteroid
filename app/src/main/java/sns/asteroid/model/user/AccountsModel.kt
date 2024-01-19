package sns.asteroid.model.user

import kotlinx.serialization.json.Json
import sns.asteroid.CustomApplication
import sns.asteroid.R
import sns.asteroid.api.Accounts
import sns.asteroid.api.entities.Account
import sns.asteroid.db.entities.Credential

class AccountsModel(val credential: Credential) {
    data class Result(
        val isSuccess: Boolean,
        val account: Account? = null,
        val toastMessage: String? = null,
    )

    constructor(server: String) : this(
        Credential("dummy", "dummy", "dummy", server, "dummy", "dummy", 0)
    )

    fun getAccountByAcct(acct: String): Result {
        val response = Accounts(credential.instance, credential.accessToken).getAccountByAcct(acct)
            ?: return Result(isSuccess = false, toastMessage = getString(R.string.failed))

        if(!response.isSuccessful)
            return Result(isSuccess = false, toastMessage = response.body!!.string())
                .also{ response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        return try {
            val account = json.decodeFromString(Account.serializer(), response.body!!.string())
            Result(isSuccess = true, account = account)
        } catch (e: Exception) {
            Result(isSuccess = false, toastMessage = e.toString())
        } finally {
            response.close()
        }
    }

    fun updateProfile(
        displayName: String? = null,
        note: String? = null,
        fields: List<Map<String, String>>? = null,
        isLocked: Boolean? = null,
        isBot: Boolean? = null,
        avatar: ByteArray? = null,
        header: ByteArray? = null,
    ): Result {
        val client = Accounts(credential.instance, credential.accessToken)
        val response = client.patchUpdateCredentials(displayName, note, fields, isLocked, isBot, avatar, header)
            ?: return Result(isSuccess = false, toastMessage = getString(R.string.failed))

        if(!response.isSuccessful)
            return Result(isSuccess = false, toastMessage = response.body!!.string())
                .also{ response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        val account = json.decodeFromString(Account.serializer(), response.body!!.string())

        return Result(isSuccess = true, account = account)
            .also { response.close() }
    }

    private fun getString(resId: Int): String {
        return CustomApplication.getApplicationContext().getString(resId)
    }
}