package sns.asteroid.model.user

import kotlinx.serialization.json.Json
import sns.asteroid.CustomApplication
import sns.asteroid.R
import sns.asteroid.api.Accounts
import sns.asteroid.api.entities.Account
import sns.asteroid.api.entities.Relationship
import sns.asteroid.db.entities.Credential

class UserActionModel(val credential: Credential) {
    data class Result(
        val isSuccess: Boolean,
        val relationship: Relationship? = null,
        val toastMessage: String,
    )

    fun postUserAction(id: String, action: Accounts.PostAction): Result {
        val client = Accounts(credential.instance, credential.accessToken)
        val response = client.postUserAction(id, action)
            ?: return Result(isSuccess = false, toastMessage = getString(R.string.failed))

        return if(response.isSuccessful) {
            val msg = when(action) {
                Accounts.PostAction.FOLLOW              -> getString(R.string.followed)
                Accounts.PostAction.UNFOLLOW            -> getString(R.string.unfollowed)
                Accounts.PostAction.REQUEST_FOLLOW      -> getString(R.string.requested_follow)
                Accounts.PostAction.UNDO_REQUEST_FOLLOW -> getString(R.string.undo_request_follow)
                Accounts.PostAction.BLOCK               -> getString(R.string.blocked)
                Accounts.PostAction.UNBLOCK             -> getString(R.string.unblocked)
                Accounts.PostAction.MUTE                -> getString(R.string.muted)
                Accounts.PostAction.MUTE_NOTIFICATION   -> getString(R.string.muted)
                Accounts.PostAction.UNMUTE              -> getString(R.string.unmuted)
                Accounts.PostAction.NOTIFY              -> getString(R.string.enable_notify)
                Accounts.PostAction.DISABLE_NOTIFY      -> getString(R.string.disable_notify)
                Accounts.PostAction.SHOW_BOOST          -> getString(R.string.show_reblogs)
                Accounts.PostAction.HIDE_BOOST          -> getString(R.string.hide_reblogs)
            }
            val json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
            val relationship = json.decodeFromString(Relationship.serializer(), response.body!!.string())

            Result(isSuccess = true, relationship = relationship, toastMessage = msg)
        }
        else
            Result(isSuccess = false, toastMessage = response.body!!.string())
    }

    fun acceptFollowRequest(account: Account): Result {
        val client = Accounts(credential.instance, credential.accessToken)
        val response = client.postAcceptOrRejectFollowRequests(account.id, isAccept = true)
            ?: return Result(isSuccess = false, toastMessage = getString(R.string.failed))

        return if(response.isSuccessful)
            Result(isSuccess = true, toastMessage = getString(R.string.accept_follow_request))
        else
            Result(isSuccess = false, toastMessage = response.body!!.string())
    }

    fun rejectFollowRequest(account: Account): Result {
        val client = Accounts(credential.instance, credential.accessToken)
        val response = client.postAcceptOrRejectFollowRequests(account.id, isAccept = false)
            ?: return Result(isSuccess = false, toastMessage = getString(R.string.failed))

        return if(response.isSuccessful)
            Result(isSuccess = true, toastMessage = getString(R.string.reject_follow_request))
        else
            Result(isSuccess = false, toastMessage = response.body!!.string())
    }

    private fun getString(resId: Int): String {
        return CustomApplication.getApplicationContext().getString(resId)
    }
}