package sns.asteroid.model.timeline

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import sns.asteroid.R
import sns.asteroid.api.Accounts
import sns.asteroid.api.entities.Status
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.timeline.GettingContentsModel.Result
import sns.asteroid.model.util.AttributeGetter

/**
 * リアクションを付けた投稿を取得
 * (ブックマ―ク、お気に入り)
 * これらについてのmaxIdやsinceIdの取得方法は通常のタイムラインとは異なる
 */
class ActionedTimelineModel(
    credential: Credential,
    private val category: Category,
): AbstractTimelineModel<Status>(credential) {

    override fun getContents(maxId: String?, sinceId: String?): Result<Status> {
        val client = Accounts(credential)
        val response = when(category) {
            Category.BOOKMARK -> client.getBookmarks(maxId, sinceId)
            Category.FAVOURITE -> client.getFavourites(maxId, sinceId)
        } ?: return Result(isSuccess=false, toastMessage=getString(R.string.failed_loading))

        if(!response.isSuccessful)
            return Result<Status>(isSuccess= false, toastMessage = response.body?.string())
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        return try {
            val statuses =
                json.decodeFromString(ListSerializer(Status.serializer()), response.body!!.string())
            Result(
                isSuccess   = true,
                contents    = statuses,
                maxId       = AttributeGetter.getMaxIdFromHttpLinkHeader(response),
                sinceId     = AttributeGetter.getSinceIdFromHttpLinkHeader(response),
            )
        } catch (e: Exception) {
            Result(isSuccess = false, toastMessage = e.toString())
        } finally {
            response.close()
        }
    }

    enum class Category {
        BOOKMARK,
        FAVOURITE,
    }
}