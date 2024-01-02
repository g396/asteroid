package sns.asteroid.api.entities

import kotlinx.serialization.Serializable
import sns.asteroid.R
import sns.asteroid.model.emoji.CustomEmojiParser

@Serializable
data class Status(
    override val id: String,
    val uri: String,
    val created_at: String, // ISO 8601 Datetime
    val account: Account,
    val content: String, // HTML
    val visibility: String, //Enumerable (public, unlisted, private, direct)
    val sensitive: Boolean,
    val spoiler_text: String,
    val media_attachments: List<MediaAttachment>,
    val application: Application? = null,
    val mentions: List<Mention>,
    val tags: List<Tag>,
    val emojis: List<CustomEmoji>,
    val reblogs_count: Int,
    val favourites_count: Int,
    val replies_count: Int,
    val url: String? = null,
    val in_reply_to_id: String? = null,
    val in_reply_to_account_id: String? = null,
    var reblog: Status? = null,
    var poll: Poll? = null,
    val card: Card? = null,
    val language: String? = null, // ISO 639 Part 1 two-letter language code
    val text: String? = null,
    val edited_at: String? = null, // ISO 8601 Datetime
    var favourited: Boolean = false,
    var reblogged: Boolean = false,
    val muted: Boolean = false,
    var bookmarked: Boolean = false,
    var pinned: Boolean = false,
    var filtered: List<FilterResult> = emptyList(),
    val emoji_reactions: List<EmojiReaction>? = null, // use in fedibird.com
    val emoji_reactions_count: Int = 0,
    var isShowContent: Boolean = reblog?.spoiler_text?.isEmpty() ?: spoiler_text.isEmpty(),
): java.io.Serializable, ContentInterface {
    val parsedContent = CustomEmojiParser.parse(content, emojis)
    val parsedSpoilerText = CustomEmojiParser.parse(spoiler_text, emojis)
}
