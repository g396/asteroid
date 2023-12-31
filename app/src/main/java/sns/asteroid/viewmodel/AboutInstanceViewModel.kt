package sns.asteroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import sns.asteroid.api.entities.InstanceV2
import sns.asteroid.api.entities.InstanceV1
import sns.asteroid.db.entities.Credential
import sns.asteroid.api.Instance

class AboutInstanceViewModel(val credential: Credential, target: String): ViewModel() {

    private val _instance = MutableLiveData<InstanceV2>()
    val instance: LiveData<InstanceV2>
        get() = _instance

    private val _instanceV1 = MutableLiveData<InstanceV1>()
    val instanceV1: LiveData<InstanceV1>
        get() = _instanceV1

    private val _errorStatus = MutableLiveData<FailStatus>()
    val errorStatus: LiveData<FailStatus>
        get() = _errorStatus

    private val client = Instance(target)

    suspend fun getAboutInstance() = withContext(Dispatchers.IO) {
        val response = client.getInstance()

        if(response == null){
            withContext(Dispatchers.Main) { _errorStatus.value = FailStatus.FAILED_GENERAL }
            return@withContext
        }

        if(!response.isSuccessful) {
            getAboutInstanceV1()
            return@withContext
        }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        val instance = try {
            json.decodeFromString(InstanceV2.serializer(), response.body!!.string())
        }catch (e:Exception) {
            withContext(Dispatchers.Main) { _errorStatus.value = FailStatus.FAILED_TO_PARSE_JSON }
            return@withContext
        }

        withContext(Dispatchers.Main) { _instance.value = instance }
    }

    private suspend fun getAboutInstanceV1() = withContext(Dispatchers.IO) {
        val response = client.getInstanceV1()

        if(response == null){
            withContext(Dispatchers.Main) { _errorStatus.value = FailStatus.FAILED_GENERAL }
            return@withContext
        }

        if(!response.isSuccessful) {
            withContext(Dispatchers.Main) {
                _errorStatus.value = when(response.code) {
                    503 -> FailStatus.INTERNAL_SERVER_ERROR
                    404 -> FailStatus.NOT_FOUND
                    403 -> FailStatus.PERMISSION_DENIED
                    400 -> FailStatus.FAILED_GENERAL
                    else -> FailStatus.FAILED_GENERAL
                }
            }
            return@withContext
        }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        val instance = try {
            json.decodeFromString(InstanceV1.serializer(), response.body!!.string())
        } catch (e:Exception) {
            withContext(Dispatchers.Main) {_errorStatus.value = FailStatus.FAILED_TO_PARSE_JSON }
            return@withContext
        }

        withContext(Dispatchers.Main) { _instanceV1.value = instance }

    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val data: Credential, private val target: String) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AboutInstanceViewModel(data, target) as T
        }
    }

    enum class FailStatus{
        FAILED_GENERAL,
        NOT_FOUND,
        INTERNAL_SERVER_ERROR,
        PERMISSION_DENIED,
        FAILED_TO_PARSE_JSON,
    }
}