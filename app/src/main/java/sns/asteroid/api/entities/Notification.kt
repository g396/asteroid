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
}
