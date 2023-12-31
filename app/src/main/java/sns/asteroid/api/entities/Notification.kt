package sns.asteroid.api.entities

import kotlinx.serialization.Serializable


@Serializable
data class Notification(
    override val id: String,
    val type: String, // Enumerable
    // (mention, status, reblog, follow, follow_request, favourite, poll, update, admin.sign_up, admin.report)
    val created_at: String, // ISO 8601 Datetime
    val account: Account,
    var status: Status? = null,
    // val report: Report,
    val emoji_reaction: EmojiReaction? = null, // use in fedibird.com
): java.io.Serializable, ContentInterface {
    var otherAccount = listOf<Account>()
}
