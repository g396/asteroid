package sns.asteroid.model.timeline

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import sns.asteroid.R
import sns.asteroid.api.Notifications
import sns.asteroid.api.entities.Notification
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.timeline.GettingContentsModel.Result


class NotificationTimelineModel(
    credential: Credential,
    val onlyMention: Boolean = false,
): AbstractTimelineModel<Notification>(credential) {
    override fun getContents(maxId: String?, sinceId: String?): Result<Notification> {
        val client = Notifications(credential)
        val response = client.getAll(maxId, sinceId, onlyMention)
            ?: return Result(isSuccess = false, toastMessage = getString(R.string.failed_loading))

        if(!response.isSuccessful)
            return Result<Notification>(isSuccess = false, toastMessage = response.body!!.string())
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        val notifications =
            json.decodeFromString(ListSerializer(Notification.serializer()), response.body!!.string())

        if(notifications.isEmpty())
            return Result<Notification>(isSuccess = true)
                .also { response.close() }

        return Result(
            isSuccess   = true,
            contents    = notifications.margeSameReaction(),
            maxId       = notifications.last().id,
            sinceId     = notifications.first().id,
        ).also { response.close() }
    }

    private fun List<Notification>.margeSameReaction(): List<Notification> {
        return this.margeSameReaction("favourite")
            .margeSameReaction("reblog")
            .margeEmojiReaction()
    }

    private fun List<Notification>.margeSameReaction(notificationType: String): List<Notification> {
        val notifications = this

        // 投稿 - アクションしたユーザのリスト のMapを作る
        val groupingFavourites = let {
            val reactions = notifications.filter { (it.type == notificationType) and (it.status != null) }

            val group = reactions.groupBy { it.status!!.id }

            group.keys.associateWith { id ->
                group[id]!!.associate { Pair(it.account, it.id) }.keys
            }
        }

        val list = notifications.toMutableList()

        groupingFavourites.forEach { id, accounts ->
            val first = notifications.find { (it.status?.id == id) and (it.type == notificationType) }
            first?.otherAccount = accounts.toList()

            list.removeIf { (it.status?.id == id) and (it.type == notificationType) and (it.otherAccount.isEmpty()) }
        }

        return list
    }

    private fun List<Notification>.margeEmojiReaction(): List<Notification> {
        val shortCodes = this.associate { Pair(it.id, it.emoji_reaction?.name ?: "") }.values
            .toSet()
            .filter { it.isNotEmpty() }

        var mutableList = this

        shortCodes.forEach {
            mutableList = mutableList.margeEmojiReaction(it)
        }

        return mutableList
    }

    private fun  List<Notification>.margeEmojiReaction(shortCode: String): List<Notification> {
        val notifications = this

        // 投稿 - アクションしたユーザのリスト のMapを作る
        val accountsMap = let {
            val reactions = notifications.filter { (it.emoji_reaction?.name == shortCode) and (it.status != null) }
            val group = reactions.groupBy { it.status!!.id }

            group.keys.associateWith { id ->
                group[id]!!.associate { Pair(it.account, it.id) }.keys
            }
        }

        val list = notifications.toMutableList()

        accountsMap.forEach { id, accounts ->
            val first = notifications.find { (it.status?.id == id) and (it.emoji_reaction?.name == shortCode) }
            first?.otherAccount = accounts.toList()

            list.removeIf { (it.status?.id == id) and (it.emoji_reaction?.name == shortCode) and (it.otherAccount.isEmpty()) }
        }

        return list
    }
}