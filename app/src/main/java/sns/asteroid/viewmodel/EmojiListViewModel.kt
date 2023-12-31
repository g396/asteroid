package sns.asteroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sns.asteroid.model.emoji.EmojiModel
import sns.asteroid.model.emoji.UnicodeEmoji

class EmojiListViewModel : ViewModel() {
    private val _emojiCategoryList = MutableLiveData<EmojiModel.EmojiCategoryList>()
    val emojiCategoryList: LiveData<EmojiModel.EmojiCategoryList> get() = _emojiCategoryList

    private val _customEmojis = MutableLiveData<List<EmojiModel.Result.EmojisList>>()
    val customEmojis: LiveData<List<EmojiModel.Result.EmojisList>>
        get() = _customEmojis

    private val _unicodeEmojis = MutableLiveData<List<UnicodeEmoji>>()
    val unicodeEmojis: LiveData<List<UnicodeEmoji>>
        get() = _unicodeEmojis

    val query = MutableLiveData<String>()

    var job: Job? = null

    init {
        query.value = ""
    }

    suspend fun getCustomEmojis(domain: String) {
        job?.cancel()
        job = viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Thread.sleep(100)
                val result = EmojiModel(domain).getCustomEmojis(query.value!!)
                result.emojis?.let { _customEmojis.postValue(it) }
            }
        }
    }

    suspend fun getUnicodeEmojis() = withContext(Dispatchers.IO) {
        EmojiModel.getUnicodeEmojis(query.value!!).let { result ->
            if (unicodeEmojis.value != result) _unicodeEmojis.postValue(result)
        }
    }

    suspend fun getCustomEmojiCategories(domain: String) = withContext(Dispatchers.IO) {
        val result = EmojiModel(domain).getCustomEmojiCategories()
        result?.let {
            if (emojiCategoryList.value != it) _emojiCategoryList.postValue(it)
        }
    }
}