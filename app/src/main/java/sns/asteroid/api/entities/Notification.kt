package sns.asteroid.api.entities

import kotlinx.serialization.Serializable

/**
 * @param type Enumerable
 * (mention, status, reblog, follow, follow_request, favourite, poll, update, admin.sign_up, admin.report)
 * @param created_at ISO-8601 Datetime
 * @param emoji_reaction use in fedibird.com and others
 */
@Serializable
data class Notification(
    override val id: String,
    val type: String,
    val created_at: String,
    val account: Account,
    val status: Status? = null,
    // val report: Report,
    val emoji_reaction: EmojiReaction? = null,
): java.io.Serializable, ContentInterface {
    var otherAccount = listOf<Account>()

    companion object {
        fun List<Notification>.margeSameReaction(): List<Notification> {
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
}
