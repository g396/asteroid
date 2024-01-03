package sns.asteroid.api.entities

import kotlinx.serialization.Serializable
import sns.asteroid.model.emoji.CustomEmojiParser

@Serializable
data class Account(
    override val id: String,
    val username: String,
    val acct: String,
    val display_name: String,
    val locked: Boolean,
    val bot: Boolean,
    val discoverable: Boolean? = null,
    val group: Boolean,
    val created_at: String,
    val note: String,
    val url: String,
    val avatar: String,
    val avatar_static: String,
    val header: String,
    val header_static: String,
    val followers_count: Long,
    val following_count: Long,
    val statuses_count: Long,
    val last_status_at: String? = null,
    val emojis: List<CustomEmoji>,
    val fields: List<Field>?,
    val source: Source? = null,
): java.io.Serializable, ContentInterface {
    val convertedDisplayName = CustomEmojiParser.parse(display_name, emojis)
    val convertedNote = CustomEmojiParser.parse(note, emojis)

    val convertedField = fields?.map {
        val name = CustomEmojiParser.parse(it.name, emojis)
        val value = CustomEmojiParser.parse(it.value, emojis)
        Triple(name, value, it.verified_at)
    }

    @Serializable
    data class Source(
        val privacy: String,
        val sensitive: Boolean,
        val language: String = "",
        val note: String,
        val fields: List<Field>,
        val follow_requests_count: Int,
    )
}