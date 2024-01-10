package sns.asteroid.model.user

import kotlinx.serialization.json.Json
import sns.asteroid.CustomApplication
import sns.asteroid.R
import sns.asteroid.api.Statuses
import sns.asteroid.api.entities.Context
import sns.asteroid.api.entities.MediaAttachment
import sns.asteroid.api.entities.Status
import sns.asteroid.api.entities.StatusSource
import sns.asteroid.db.entities.Credential

class StatusesModel(val credential: Credential) {
    data class Result(
        val isSuccess: Boolean,
        val toastMessage: String,
        val status: Status? = null,
    )

    data class ResultOfContext(
        val isSuccess: Boolean,
        val context: Context?, // is not Android.Context
        val toastMessage: String?,
    )

    fun getContext(id: String): ResultOfContext {
        val client = Statuses(credential)
        val response = client.getContext(id)
            ?: return ResultOfContext(isSuccess = false, context = null, toastMessage = getString(R.string.failed))

        if(!response.isSuccessful)
            return ResultOfContext(isSuccess = false, context = null, toastMessage = response.body!!.string())

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        val context = json.decodeFromString(Context.serializer(), response.body!!.string())
        return ResultOfContext(isSuccess = true, context = context, toastMessage = null)
    }

    fun postStatuses(text: String, visibility: String): Result {
        if(text.isEmpty()) return Result(isSuccess = false, getString(R.string.empty))

        val response =
            Statuses(credential).postNewStatus(status = text, visibility = visibility)
            ?: return Result(false, getString(R.string.failed))

        if(!response.isSuccessful)
            return Result(isSuccess = false, toastMessage = response.body?.string().toString())
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        val status = try {
            json.decodeFromString(Status.serializer(), response.body!!.string())
        } catch (e: Exception) {
            return Result(isSuccess = false, toastMessage = e.toString())
                .also { response.close() }
        }
        return Result(isSuccess = true, status = status, toastMessage = getString(R.string.send))
            .also { response.close() }
    }

    fun postStatuses(
        text: String,
        spoilerText: String,
        mediaAttachments: List<MediaAttachment>,
        sensitive: Boolean,
        visibility: String,
        pollOptions: List<String>?,
        pollExpire: Int?,
        pollMultiple: Boolean?,
        replyTo: Status?,
        language: String,
    ): Result {
        if(text.isEmpty() and mediaAttachments.isEmpty()) return Result(false, getString(R.string.empty))

        val mediaIds = mediaAttachments.associateBy { it.id } .keys.toList()

        val client = Statuses(credential)
        val response = client.postNewStatus(
            status = text,
            spoilerText = spoilerText,
            mediaIds = mediaIds,
            sensitive = sensitive,
            visibility = visibility,
            pollOptions = pollOptions,
            pollExpiresIn = pollExpire,
            pollMultiple = pollMultiple,
            inReplyToId = replyTo?.id,
            language  = language,
        ) ?: return Result(false, getString(R.string.failed))

        if(!response.isSuccessful)
            return Result(isSuccess = false, toastMessage = response.body?.string().toString())

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        return try {
            val status = json.decodeFromString(Status.serializer(), response.body!!.string())
            Result(isSuccess = true, status = status, toastMessage = getString(R.string.send))
        } catch (e: Exception) {
            Result(isSuccess = false, toastMessage = e.toString())
        } finally {
            response.close()
        }
    }

    fun editStatus(
        id:String,
        text: String,
        spoilerText: String,
        mediaFile: List<MediaModel.MediaFile>,
        sensitive: Boolean,
        pollOptions: List<String>?,
        pollExpire: Int?,
        pollMultiple: Boolean?,
        language: String,
    ): Result {
        if(text.isEmpty() and mediaFile.isEmpty()) return Result(false, getString(R.string.empty))

        val mediaIds = mediaFile.mapNotNull { it.mediaAttachment?.id }
        val mediaAttributes = mediaFile.map { Triple(it.mediaAttachment?.id, it.description, "0.00,0.00") }

        val client = Statuses(credential)
        val response = client.editStatus(
            id = id,
            status = text,
            spoilerText = spoilerText,
            mediaIds = mediaIds,
            mediaAttributes = mediaAttributes,
            sensitive = sensitive,
            pollOptions = pollOptions,
            pollExpiresIn = pollExpire,
            pollMultiple = pollMultiple,
            pollHideTotals = null,
            language  = language,
        ) ?: return Result(false, getString(R.string.failed))

        if(!response.isSuccessful)
            return Result(isSuccess = false, toastMessage = response.body?.string().toString())

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        return try {
            val status = json.decodeFromString(Status.serializer(), response.body!!.string())
            Result(isSuccess = true, status = status, toastMessage = getString(R.string.send))
        } catch (e: Exception) {
            Result(isSuccess = true, toastMessage = e.toString())
        } finally {
            response.close()
        }
    }

    fun deleteStatus(statusId: String): Result {
        val client = Statuses(credential)
        val response = client.deleteStatus(statusId)
            ?: return Result(false, getString(R.string.failed))

        return if(response.isSuccessful)
            Result(true, getString(R.string.deleted)).also { response.close() }
        else
            Result(false, response.body!!.string()).also { response.close() }
    }

    fun getStatusSource(statusId: String): StatusSource? {
        val client = Statuses(credential)
        val response = client.getStatusSource(statusId) ?: return null

        if(!response.isSuccessful) return null
            .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        return try {
            json.decodeFromString(StatusSource.serializer(), response.body!!.string())
        } catch (e: Exception) {
            null
        } finally {
            response.close()
        }
    }

    /**
     * ふぁぼる
     */
    fun postFavourite(statusId: String): Result {
        val client = Statuses(credential)
        val response = client.postAction(statusId, Statuses.PostAction.FAVOURITE)
            ?: return Result(false, getString(R.string.failed))

        return if(response.isSuccessful) {
            val json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
            val status = json.decodeFromString(Status.serializer(), response.body!!.string())
            Result(true, getString(R.string.favourited), status).also { response.close() }
        } else
            Result(false, response.body!!.string()).also { response.close() }
    }

    /**
     * ふぁぼ取り消す
     */
    fun postUnFavourite(statusId: String): Result {
        val client = Statuses(credential)
        val response = client.postAction(statusId, Statuses.PostAction.UNFAVOURITE)
            ?: return Result(false, getString(R.string.failed))

        return if(response.isSuccessful) {
            val json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
            val status = json.decodeFromString(Status.serializer(), response.body!!.string())
            Result(true, getString(R.string.undo), status).also { response.close() }
        } else
            Result(false, response.body!!.string()).also { response.close() }
    }

    /**
     * ブーストする
     */
    fun postBoost(statusId: String, visibility: Visibility): Result {
        val client = Statuses(credential)
        val response = when(visibility) {
            Visibility.NONE -> client.postAction(statusId, Statuses.PostAction.BOOST)
            Visibility.PUBLIC -> client.postAction(statusId, Statuses.PostAction.BOOST_PUBLIC)
            Visibility.UNLISTED -> client.postAction(statusId, Statuses.PostAction.BOOST_PRIVATE)
            Visibility.PRIVATE -> client.postAction(statusId, Statuses.PostAction.BOOST_LOCKED)
        } ?: return Result(false, getString(R.string.failed))

        return if(response.isSuccessful) {
            val json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
            val status = json.decodeFromString(Status.serializer(), response.body!!.string())
            Result(true, getString(R.string.reblogged), status).also { response.close() }
        } else
            Result(false, response.body!!.string()).also { response.close() }
    }

    /**
     * ブースト取り消す
     */
    fun postUnBoost(statusId: String): Result {
        val client = Statuses(credential)
        val response = client.postAction(statusId, Statuses.PostAction.UNBOOST)
            ?: return Result(false, getString(R.string.failed))

        return if(response.isSuccessful) {
            val json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
            val status = json.decodeFromString(Status.serializer(), response.body!!.string())
            Result(true, getString(R.string.undo), status).also { response.close() }
        } else
            Result(false, response.body!!.string()).also { response.close() }
    }

    /**
     * ブックマークする
     */
    fun postBookMark(statusId: String): Result {
        val client = Statuses(credential)
        val response = client.postAction(statusId, Statuses.PostAction.BOOKMARK)
            ?: return Result(false, getString(R.string.failed))

        return if(response.isSuccessful) {
            val json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
            val status = json.decodeFromString(Status.serializer(), response.body!!.string())
            Result(true, getString(R.string.bookmarked), status).also { response.close() }
        } else
            Result(false, response.body!!.string()).also { response.close() }
    }

    /**
     * ブックマーク取り消す
     */
    fun postUnBookmark(statusId: String): Result {
        val client = Statuses(credential)
        val response = client.postAction(statusId, Statuses.PostAction.UNBOOKMARK)
            ?: return Result(false, getString(R.string.failed))

        return if(response.isSuccessful) {
            val json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
            val status = json.decodeFromString(Status.serializer(), response.body!!.string())
            Result(true, getString(R.string.undo), status).also { response.close() }
        } else
            Result(false, response.body!!.string()).also { response.close() }
    }

    /**
     * ピン留めする
     */
    fun postPin(statusId: String): Result {
        val client = Statuses(credential)
        val response = client.postAction(statusId, Statuses.PostAction.PIN)
            ?: return Result(false, getString(R.string.failed))

        return if(response.isSuccessful) {
            val json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
            val status = json.decodeFromString(Status.serializer(), response.body!!.string())
            Result(true, getString(R.string.pin), status).also { response.close() }
        } else
            Result(false, response.body!!.string()).also { response.close() }
    }

    /**
     * ピン留めする
     */
    fun postUnPin(statusId: String): Result {
        val client = Statuses(credential)
        val response = client.postAction(statusId, Statuses.PostAction.UNPIN)
            ?: return Result(false, getString(R.string.failed))

        return if(response.isSuccessful) {
            val json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
            val status = json.decodeFromString(Status.serializer(), response.body!!.string())
            Result(true, getString(R.string.undo), status).also { response.close() }
        } else
            Result(false, response.body!!.string()).also { response.close() }
    }

    fun getString(resId: Int): String {
        return CustomApplication.getApplicationContext().getString(resId)
    }

    enum class Visibility {
        NONE,
        PUBLIC,
        UNLISTED,
        PRIVATE,
    }
}