package sns.asteroid.viewmodel

import android.net.Uri
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sns.asteroid.api.entities.Status
import sns.asteroid.db.entities.Credential
import sns.asteroid.db.entities.Draft
import sns.asteroid.model.DraftModel
import sns.asteroid.model.settings.RecentlyHashtagModel
import sns.asteroid.model.settings.SettingsManageAccountsModel
import sns.asteroid.model.user.MediaModel
import sns.asteroid.model.user.StatusesModel
import sns.asteroid.model.util.ISO639Lang
import sns.asteroid.view.adapter.spinner.VisibilityAdapter

class CreatePostsViewModel(credential: Credential?, val replyTo: Status?, intentText: String?, visibility: String?): ViewModel() {
    private val _credential = MutableLiveData<Credential>()
    val credential: LiveData<Credential> get() = _credential

    private var _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    private val _mediaFile = MutableLiveData<List<MediaModel.MediaFile>>()
    val mediaFile: LiveData<List<MediaModel.MediaFile>> get() = _mediaFile

    /* EditText */
    val content = MutableLiveData<String>()
    val spoilerText = MutableLiveData<String>()

    /* EditText (poll) */
    val value1 = MutableLiveData<String>()
    val value2 = MutableLiveData<String>()
    val value3 = MutableLiveData<String>()
    val value4 = MutableLiveData<String>()

    /* CheckBox */
    val sensitive = MutableLiveData<Boolean>()
    val resizeImage = MutableLiveData<Boolean>()
    val pollMultiple = MutableLiveData<Boolean>()

    /* ToggleButton */
    val createPoll = MutableLiveData<Boolean>()
    val enableSpoilerText = MutableLiveData<Boolean>()

    /* PopupMenu items */
    private val _hashtags = MutableLiveData<List<String>>()
    val hashtags: LiveData<List<String>> get() = _hashtags

    /* Spinner items */
    private val _language = MutableLiveData<List<ISO639Lang>>()
    val language: LiveData<List<ISO639Lang>> get() = _language

    /* Spinner items (poll) */
    val days = listOf(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
    )
    val hours = listOf(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23,
    )
    val mins = listOf(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55)

    /* Spinner position (use 2way-binding) */
    var langPosition: Int = 0
    var visibilityPosition: Int = 0
    var dayPosition: Int = 0
    var hourPosition: Int = 1
    var minPosition: Int = 0

    /* Spinner item getter */
    private val languageCode get() =
        language.value?.getOrNull(langPosition)?.code ?: ""

    private val pollOption get() =
        if(createPoll.value == true) listOfNotNull(value1.value, value2.value, value3.value, value4.value)
        else null

    private val expireInSeconds get() =
        if(createPoll.value == true) (((days[dayPosition] * 24 + hours[hourPosition]) * 60) + mins[minPosition]) * 60
        else null

    private var draftId = 0

    init {
        replyTo?.let {
            if(it.account.id != credential?.account_id) content.value = String.format("@%1\$s ", replyTo.account.acct)
        }
        intentText?.let {
            content.value = it
        }
        if(content.value == null) content.value = ""
        if(spoilerText.value == null) spoilerText.value = ""
        sensitive.value = false
        resizeImage.value = true
        createPoll.value = false
        pollMultiple.value = true
        value1.value = ""
        value2.value = ""
        value3.value = ""
        value4.value = ""
        _mediaFile.value = emptyList()

        visibilityPosition =
            VisibilityAdapter.getPosition(replyTo?.visibility ?: visibility)

        viewModelScope.launch {
            _credential.value = credential ?: loadDefaultCredential()
            loadHashtags()
            loadLanguagesList()
        }
    }

    /**
     * 投稿を送信する
     */
    suspend fun postStatuses(): Boolean {
        val notUploadedYet = mediaFile.value!!.filter { (it.uri != null) and (it.mediaAttachment == null) }
        notUploadedYet.forEach {
            val result = withContext(Dispatchers.IO) {
                MediaModel(credential.value!!).postMedia(it.uri!!, it.description, resizeImage.value!!)
            }
            if (!result.isSuccess) {
                _toastMessage.value = result.message
                return false
            }
            result.mediaAttachment?.let { mediaAttachment ->
                val index = mediaFile.value!!.indexOf(it)
                val uploaded = it.copy(mediaAttachment = mediaAttachment)

                val list = mediaFile.value!!.toMutableList().apply {
                    removeAt(index)
                    add(index, uploaded)
                }
                _mediaFile.value = list
            }
        }

        val multiple =
            if(createPoll.value == true) pollMultiple.value
            else null

        val result = withContext(Dispatchers.IO) {
            val list =  mediaFile.value!!.mapNotNull { it.mediaAttachment }
            StatusesModel(credential.value!!).postStatuses(
                content.value!!,
                spoilerText.value!!,
                list,
                sensitive.value!!,
                VisibilityAdapter.getVisibility(visibilityPosition),
                pollOption,
                expireInSeconds,
                multiple,
                replyTo,
                languageCode
            )
        }
        _toastMessage.value = result.toastMessage

        saveHashtag(result.status)
        return result.isSuccess
    }

    /**
     * 添付ファイルのリストに指定のファイルを追加する
     */
    suspend fun addMedia(type: MediaModel.MediaType, uri: Uri) = withContext(Dispatchers.IO) {
        val media = MediaModel.MediaFile(uri = uri, type = type)
        val withThumbnail = MediaModel.getThumbnail(media)
        _mediaFile.postValue(mediaFile.value!!.plus(withThumbnail))
    }

    suspend fun saveHashtag(status: Status?) = withContext(Dispatchers.IO) {
        if (status == null) return@withContext
        RecentlyHashtagModel.insertOrUpdate(status.tags.map { it.name }, status.created_at)
    }

    suspend fun loadHashtags() = withContext(Dispatchers.IO) {
        val hashtags = RecentlyHashtagModel.getAll()
        _hashtags.postValue(hashtags)
    }

    suspend fun loadLanguagesList() = withContext(Dispatchers.IO) {
        val languages = ISO639Lang.getLanguageList()
        _language.postValue(languages)
    }

    suspend fun save() = withContext(Dispatchers.IO) {
        DraftModel.insert(
            id = draftId,
            content = content.value!!,
            languageCode = languageCode,
            visibility = VisibilityAdapter.getVisibility(visibilityPosition),
            spoilerText = spoilerText.value!!,
            pollValue1 = value1.value!!,
            pollValue2 = value2.value!!,
            pollValue3 = value3.value!!,
            pollValue4 = value4.value!!,
            pollMultiple = pollMultiple.value!!,
            expireDay = days[dayPosition],
            expireHour = hours[hourPosition],
            expireMin = mins[minPosition],
        )
    }

    suspend fun load(position: Int) = withContext(Dispatchers.IO) {
        val draft = DraftModel.getAll().getOrNull(position)?.also { draftId = it.id }
            ?: return@withContext

        content.postValue(draft.content)
        spoilerText.postValue(draft.spoilerText)

        value1.postValue(draft.pollValue1)
        value2.postValue(draft.pollValue2)
        value3.postValue(draft.pollValue3)
        value4.postValue(draft.pollValue4)
        pollMultiple.postValue(draft.pollMultiple)

        dayPosition = days.indexOf(draft.expireDay).let {
            if (it < 0) 0 else it
        }
        hourPosition = hours.indexOf(draft.expireHour).let {
            if (it < 0) 0 else it
        }
        minPosition = mins.indexOf(draft.expireMin).let {
            if (it < 0) 0 else it
        }
        langPosition = language.value!!.indexOfFirst { it.code == draft.language }.let {
            if (it < 0) 0 else it
        }
        visibilityPosition =
            VisibilityAdapter.getPosition(draft.visibility)

        createPoll.postValue(
            draft.pollValue1.isNotBlank()
                    or draft.pollValue2.isNotBlank()
                    or draft.pollValue3.isNotBlank()
                    or draft.pollValue4.isNotBlank()
        )
        enableSpoilerText.postValue(draft.spoilerText.isNotEmpty())
    }

    /**
     * 添付ファイルに説明文を追加する
     */
    fun addDescription(position: Int, description: String) {
        val added = mediaFile.value!!.getOrNull(position)?.copy(description = description) ?: return
        val list = mediaFile.value!!.toMutableList().apply {
            removeAt(position)
            add(position, added)
        }
        _mediaFile.postValue(list)
    }

    /**
     * 添付ファイルのリストから指定のファイルを削除する
     */
    fun removeImage(position: Int) {
        val list = mediaFile.value!!.toMutableList().apply {
            removeAt(position)
        }
        _mediaFile.postValue(list)
    }

    /**
     * 共有からアプリを起動し投稿画面を開いた場合は投稿元アカウントの指定が無いので
     * デフォルトのアカウント(リストの一番上)を取得する
     */
    private suspend fun loadDefaultCredential(): Credential? = withContext(Dispatchers.IO) {
            SettingsManageAccountsModel().getCredentials().firstOrNull()
    }


    /**
     * 投稿元アカウントを切り替える
     */
    fun selectOtherAccount(credential: Credential) {
        _credential.value = credential
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val credential: Credential?,
        private val replyTo: Status?,
        private val intentText: String?,
        private val visibility: String?,
        ) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CreatePostsViewModel(credential, replyTo, intentText, visibility) as T
        }
    }
}