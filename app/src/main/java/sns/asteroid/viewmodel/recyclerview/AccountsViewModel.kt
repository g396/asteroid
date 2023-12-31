package sns.asteroid.viewmodel.recyclerview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import sns.asteroid.api.entities.Account
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.account.*

class AccountsViewModel(
    columnInfo: ColumnInfo, credential: Credential
): RecyclerViewViewModel<Account>(columnInfo, credential) {
    override val timelineModel = when(columnInfo.subject) {
        "following"     -> FollowingModel(credential, columnInfo.option_id)
        "followers"     -> FollowerModel(credential, columnInfo.option_id)
        "block"         -> BlocksModel(credential)
        "mute"          -> MutesModel(credential)
        "directory"     -> DirectoryModel(credential)
        "suggestions"   -> SuggestionsModel(credential)
        "favourited_by" -> WhoActionedModel(credential, id = columnInfo.option_id, WhoActionedModel.Action.FAVOURITE)
        "reblogged_by"  -> WhoActionedModel(credential, id = columnInfo.option_id, WhoActionedModel.Action.BOOST)
        else -> TODO()
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val columnInfo: ColumnInfo,
        private val credential: Credential,
    ): ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AccountsViewModel(columnInfo, credential) as T
        }
    }

}