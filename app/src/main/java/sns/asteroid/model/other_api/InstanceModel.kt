package sns.asteroid.model.other_api

import kotlinx.serialization.json.Json
import sns.asteroid.CustomApplication
import sns.asteroid.R
import sns.asteroid.api.Instance
import sns.asteroid.api.entities.InstanceV1

class InstanceModel(domain: String) {
    private val client = Instance(domain)

    fun getInstanceV1(): ResultOfInstanceV1 {
        val response = client.getInstanceV1()
            ?: return ResultOfInstanceV1(isSuccess = false, toastMessage = getString(R.string.failed))

        if (!response.isSuccessful) {
            return ResultOfInstanceV1(isSuccess = false, toastMessage = response.body!!.string())
                .also { response.close() }
        }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        val instance = json.decodeFromString(InstanceV1.serializer(), response.body!!.string())

        return ResultOfInstanceV1(isSuccess = true, instance = instance)
            .also { response.close() }
    }

    fun getString(resId: Int): String {
        return CustomApplication.getApplicationContext().getString(resId)
    }

    data class ResultOfInstanceV1(
        val isSuccess: Boolean,
        val instance: InstanceV1? = null,
        val toastMessage: String? = null,
    )
}