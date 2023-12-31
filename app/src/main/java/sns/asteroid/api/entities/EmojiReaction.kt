package sns.asteroid.api.entities

import kotlinx.serialization.Serializable

@Serializable
data class EmojiReaction(
    val name: String = "",
    val count: Int = 0,
    val me: Boolean = false,
    val url: String = "",
    val static_url: String = "",
    val domain: String? = null,
    // val width: Int,
    // val height: Int,
): java.io.Serializable
