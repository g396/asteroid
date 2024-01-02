package sns.asteroid.viewmodel.recyclerview.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sns.asteroid.CustomApplication
import sns.asteroid.api.entities.Poll
import sns.asteroid.api.entities.Status
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.settings.SettingsManageAccountsModel
import sns.asteroid.model.timeline.*
import sns.asteroid.model.user.StatusesModel
import sns.asteroid.viewmodel.recyclerview.RecyclerViewViewModel
import sns.asteroid.viewmodel.recyclerview.StatusViewModelInterface

open class TimelineViewModel(
    columnInfo: ColumnInfo,
    credential: Credential,
): RecyclerViewViewModel<Status>(columnInfo, credential), StatusViewModelInterface {
    override val timelineModel: GettingContentsModel<Status> = when(columnInfo.subject) {
        "local"     -> StandardTimelineModel(credential, StandardTimelineModel.Category.LOCAL)
        "home"      -> StandardTimelineModel(credential, StandardTimelineModel.Category.HOME)
        "public"    -> StandardTimelineModel(credential, StandardTimelineModel.Category.PUBLIC)
        "local_media"   -> StandardTimelineModel(credential, StandardTimelineModel.Category.LOCAL_MEDIA)
        "public_media"  -> StandardTimelineModel(credential, StandardTimelineModel.Category.PUBLIC_MEDIA)
        "mix"       -> MixTimelineModel(credential)
        "list"      -> ListTimelineModel(credential, columnInfo.option_id)
        "favourites"-> ActionedTimelineModel(credential, ActionedTimelineModel.Category.FAVOURITE)
        "bookmarks" -> ActionedTimelineModel(credential, ActionedTimelineModel.Category.BOOKMARK)
        "user_pin"  -> PinnedUserTimelineModel(credential, columnInfo.option_id)
        "user_posts"-> UserTimelineModel(credential, columnInfo.option_id, "posts")
        "user_media"-> UserTimelineModel(credential, columnInfo.option_id, "media")
        "hashtag"   -> HashtagTimelineModel(credential, columnInfo.option_id)
        "mention"   -> MentionTimelineModel(credential)
        else        -> StandardTimelineModel(credential, StandardTimelineModel.Category.LOCAL)
    }

    /**
     * 編集された投稿を置き換える
     * ストリーミングしてる時はタイムラインに表示されていない過去の投稿の編集についても受信するので、
     * タイムラインに表示中の投稿だけ置き換える
     */
    override fun replaceContent(content: Status) {
        val mutableList = contents.value!!.toMutableList()

        val reblogged = mutableList.filter {
            it.reblog?.id == content.id
        }
        reblogged.forEach { r ->
            val new = r.copy().also {
                it.filtered = r.filtered
                it.isShowContent = r.isShowContent
                it.reblog = content
            }
            val index = mutableList.indexOfFirst { it.id == r.id }
            mutableList.removeAt(index)
            mutableList.add(index, new)
        }

        val statuses = mutableList.filter {
            it.id == content.id
        }
        statuses.forEach { s ->
            val new = content.also {
                it.filtered = s.filtered
                it.isShowContent = s.isShowContent
            }
            val index = mutableList.indexOf(s)
            mutableList.removeAt(index)
            mutableList.add(index, new)
        }

        _contents.postValue(mutableList)
    }

    override fun updatePoll(poll: Poll) {
        val mutableList = contents.value!!.toMutableList()

        // ブースト投稿に対しての置換
        val reblogged = mutableList.filter {
            it.reblog?.poll?.id == poll.id
        }
        reblogged.forEach { r ->
            val new = r.copy().also {
                it.reblog = it.reblog?.copy()?.also { s ->
                    s.poll = poll
                }
            }
            val index = mutableList.indexOfFirst { it.id == r.id }
            mutableList.removeAt(index)
            mutableList.add(index, new)
        }

        // 通常投稿に対しての置換
        val statuses = contents.value!!.filter {
            it.poll?.id == poll.id
        }
        statuses.forEach { s ->
            val new = s.copy().also {
                it.poll = poll
            }
            val index = mutableList.indexOf(s)
            mutableList.removeAt(index)
            mutableList.add(index, new)
        }

        _contents.postValue(mutableList)
    }

    override suspend fun postStatus(text: String, visibility: String) = withContext(Dispatchers.IO) {
        val result = StatusesModel(credential.value!!).postStatuses(text, visibility).also {
            toastMessage.postValue(it.toastMessage)
            saveHashtag(it.status)
        }
        result.status?.let { status ->
            val timeline = when(visibility) {
                "public" -> listOf("local", "public", "home", "mix")
                else -> listOf("home", "mix")
            }
            if (timeline.contains(columnInfo.subject)) addLatestContents(listOf(status))
        }
        return@withContext result.isSuccess
    }

    suspend fun checkUpdateMyAvatar(list: List<Status>) {
        withContext(Dispatchers.IO) {
            val myPosts =
                list.find { it.account.id == credential.value!!.account_id } ?: return@withContext
            if (myPosts.account.avatar != credential.value!!.avatarStatic) {
                SettingsManageAccountsModel().apply {
                    updateAvatar(credential.value!!, myPosts.account.avatar)
                    val new = getCredential(credential.value!!.acct)
                    _credential.postValue(new)
                }
            }
        }
    }

    override suspend fun addLatestContents(list: List<Status>) {
        super.addLatestContents(list)
        withContext(Dispatchers.IO) { checkUpdateMyAvatar(list) }
    }

    override fun removeStatus(statusId: String) {
        _contents.value = contents.value?.toMutableList()?.also { _statuses ->
            val status = _statuses.find { it.id == statusId } ?: return
            val index = _statuses.indexOf(status)
            _statuses.removeAt(index)
        }
    }

    private fun getString(resId: Int): String {
        return CustomApplication.getApplicationContext().getString(resId)
    }


    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val column: ColumnInfo,
        private val credential: Credential,
        ): ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TimelineViewModel(column, credential) as T
        }
    }
}