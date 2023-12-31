package sns.asteroid.api.entities

import kotlinx.serialization.Serializable

@Serializable
data class CustomEmoji(
    val shortcode: String,
    val url: String,
    val static_url: String,
    val visible_in_picker: Boolean,
    val category: String = "",
): java.io.Serializable