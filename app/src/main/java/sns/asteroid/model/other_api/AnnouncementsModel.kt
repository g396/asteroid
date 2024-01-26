package sns.asteroid.model.other_api

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import sns.asteroid.CustomApplication
import sns.asteroid.R
import sns.asteroid.api.Announcements
import sns.asteroid.api.entities.Announcement
import sns.asteroid.db.entities.Credential

class AnnouncementsModel(private val credential: Credential) {
    fun getAll(): Result {
        val response = Announcements(credential.instance, credential.accessToken).getAll()
            ?: return Result(isSuccess = false, message = getString(R.string.failed))

        if (!response.isSuccessful)
            return Result(isSuccess = false, message = response.body?.string())
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        return try {
            val announcements = json.decodeFromString(ListSerializer(Announcement.serializer()), response.body!!.string())
            Result(isSuccess = true, announcements = announcements)
        } catch (e:Exception) {
            Result(isSuccess = false, message = e.toString())
        }
    }

    private fun getString(resId: Int): String {
        return CustomApplication.getApplicationContext().getString(resId)
    }

    data class Result(
        val isSuccess: Boolean,
        val announcements: List<Announcement>? = null,
        val message: String? = null,
    )
}