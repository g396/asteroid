package sns.asteroid.api.entities

import kotlinx.serialization.Serializable
import sns.asteroid.model.emoji.CustomEmojiParser

/**
 * @param created_at            ISO-8601 Datetime
 * @param content               HTML text
 * @param visibility            Enumerable (public, unlisted, private, direct)
 * @param language              ISO-639 Part 1 two-letter language code
 * @param edited_at             ISO-8601 Datetime
 * @param emoji_reactions       use in fedibird.com and others
 * @param emoji_reactions_count use in fedibird.com and others
 */
@Serializable
data class Status(
    override val id: String,
    val uri: String,
    val created_at: String,
    val account: Account,
    val content: String,
    val visibility: String,
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
    val reblog: Status? = null,
    val poll: Poll? = null,
    val card: Card? = null,
    val language: String? = null,
    val text: String? = null,
    val edited_at: String? = null,
    val favourited: Boolean = false,
    val reblogged: Boolean = false,
    val muted: Boolean = false,
    val bookmarked: Boolean = false,
    val pinned: Boolean = false,
    val filtered: List<FilterResult> = emptyList(),
    val emoji_reactions: List<EmojiReaction>? = null,
    val emoji_reactions_count: Int = 0,
): java.io.Serializable, ContentInterface {
    val parsedContent = CustomEmojiParser.parse(content, emojis)
    val parsedSpoilerText = CustomEmojiParser.parse(spoiler_text, emojis)

    var isShowContent: Boolean = reblog?.spoiler_text?.isEmpty() ?: spoiler_text.isEmpty()
    var useFilter: Boolean = true
}
