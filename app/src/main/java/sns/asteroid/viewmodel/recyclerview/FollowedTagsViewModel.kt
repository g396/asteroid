package sns.asteroid.viewmodel.recyclerview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import sns.asteroid.api.entities.Tag
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.hashtag.FollowedTagsModel

class FollowedTagsViewModel(
    columnInfo: ColumnInfo, credential: Credential
): RecyclerViewViewModel<Tag>(columnInfo, credential) {
    override val timelineModel = FollowedTagsModel(credential)

    @Suppress("UNCHECKED_CAST")
    class Factory(private val columnInfo: ColumnInfo, private val credential: Credential) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FollowedTagsViewModel(columnInfo, credential) as T
        }
    }
}