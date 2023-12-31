package sns.asteroid.api.entities

import kotlinx.serialization.Serializable
import sns.asteroid.model.emoji.CustomEmojiParser

@Serializable
data class Poll(
    val id: String,
    val expires_at: String?, // ISO 8601 Datetime
    val expired: Boolean,
    val multiple: Boolean,
    val votes_count: Int,
    val voters_count: Int?, // null if multiple is false
    val options: List<Option>,
    val emojis: List<CustomEmoji>,
    val voted: Boolean? = null,
    val own_votes: List<Int>? = null,
): java.io.Serializable {

    init {
        options.forEach {
            it.votesRatio =
                if(it.votes_count == null) 0.0F
                else if (votes_count == 0) 0.0F
                else it.votes_count.toFloat() / votes_count.toFloat()
            it.parsedTitle = CustomEmojiParser.parse(it.title, emojis)
        }
    }
    @Serializable
    data class Option(
        val title: String,
        val votes_count: Int?, // null if results are not published yet
    ):java.io.Serializable {
        var votesRatio :Float = 0.0F
        var parsedTitle: String = ""
    }
}