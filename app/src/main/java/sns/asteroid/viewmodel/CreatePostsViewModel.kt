package sns.asteroid.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sns.asteroid.CustomApplication
import sns.asteroid.R
import sns.asteroid.api.entities.MediaAttachment
import sns.asteroid.api.entities.Status
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.settings.RecentlyHashtagModel
import sns.asteroid.model.settings.SettingsManageAccountsModel
import sns.asteroid.model.user.MediaModel
import sns.asteroid.model.user.StatusesModel
import sns.asteroid.model.util.ISO639Lang

class CreatePostsViewModel(credential: Credential?, val replyTo: Status?): ViewModel() {
    private val _credential = MutableLiveData<Credential>()
    val credential: LiveData<Credential> get() = _credential

    private var _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    private val _hashtags = MutableLiveData<List<String>>()
    val hashtags: LiveData<List<String>> get() = _hashtags

    private val _language = MutableLiveData<List<ISO639Lang>>()
    val language: LiveData<List<ISO639Lang>> get() = _language

    // 複数種類のメディアを同時に添付することは出来ないので最初に選んだメディアの種類を保持する必要あり
    private val _property = MutableLiveData<Property>()
    val property: LiveData<Property> get() = _property

    private val _media = MutableLiveData<List<Pair<Uri, Bitmap>>>()
    val media: LiveData<List<Pair<Uri, Bitmap>>> get() = _media

    private val descriptions = mutableMapOf<Uri, String>()

    val mediaAttachments = mutableListOf<Pair<Uri, MediaAttachment>>()

    init {
        _property.value = Property.NONE
        _media.value = mutableListOf()

        viewModelScope.launch {
            _credential.value = credential ?: loadDefaultCredential()
            loadHashtags()
            loadLanguagesList()
        }
    }

    /**
     * 投稿を送信する
     */
    suspend fun postStatuses(
        text: String,
        spoilerText: String,
        sensitive: Boolean,
        visibility: String,
        language: String,
        pollOption: List<String>?,
        pollExpire: Int?,
        pollMultiple: Boolean?,
        resizeImage: Boolean,
    ): Boolean {
        val notUploadedYet = media.value!!.filter { selected ->
            val found = mediaAttachments.find { uploaded -> selected.first == uploaded.first }
            found == null
        }
        notUploadedYet.forEach {
            val result = withContext(Dispatchers.IO) {
                MediaModel(credential.value!!).postMedia(it.first, descriptions[it.first], resizeImage)
            }
            if (result.isSuccess) {
                mediaAttachments.add(Pair(it.first, result.mediaAttachment!!))
            } else {
                _toastMessage.value = result.message
                return false
            }
        }

        val result = withContext(Dispatchers.IO) {
            val list =  mediaAttachments.unzip().second
            StatusesModel(credential.value!!).postStatuses(
                text, spoilerText, list, sensitive, visibility, pollOption, pollExpire, pollMultiple, replyTo, language
            )
        }
        _toastMessage.value = result.toastMessage

        saveHashtag(result.status)
        return result.isSuccess
    }

    /**
     * 添付ファイルのリストに指定のファイルを追加する
     */
    suspend fun addMedia(property: Property, uri: Uri) = withContext(Dispatchers.IO) {
        val thumbnail =
            if(property == Property.AUDIO) {
                val resources = CustomApplication.getApplicationContext().resources
                ResourcesCompat.getDrawable(resources, R.drawable.audiofile, null)?.toBitmap()
            } else {
                MediaModel.getThumbnail(uri)
            } ?: return@withContext

        val item = Pair(uri, thumbnail)
        val added = this@CreatePostsViewModel.media.value!!.toMutableList().apply { add(item) }

        _property.postValue(property)
        _media.postValue(added)
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

    /**
     * 添付ファイルに説明文を追加する
     */
    fun addDescription(uri: Uri, description: String) {
        descriptions[uri] = description
    }

    fun getDescription(uri: Uri): String {
        return descriptions.getOrDefault(uri, "")
    }

    /**
     * 添付ファイルのリストから指定のファイルを削除する
     */
    fun removeImage(uri: Uri) {
        val removed = media.value!!.toMutableList().apply { removeIf { it.first == uri } }
        _media.postValue(removed)

        if(removed.isEmpty()) _property.postValue(Property.NONE)
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
    class Factory(private val credential: Credential?, private val replyTo: Status?
        ) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CreatePostsViewModel(credential, replyTo) as T
        }
    }

    enum class Property {
        IMAGE,
        VIDEO,
        AUDIO,
        NONE,
    }

}