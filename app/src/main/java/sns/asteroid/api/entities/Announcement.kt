package sns.asteroid.api.entities

@kotlinx.serialization.Serializable
data class Announcement(
    val id: String,
    val content: String,
    val starts_at: String? = null,
    val ends_at: String? = null,
    // val published: Boolean,
    // val all_day: Boolean,
    val published_at: String = "",
    // val updated_at: String,
    // val read: Boolean? = null,
    // val mentions: List<*>,
    // val statuses: List<*>,
    // val tags: List<*>,
    val emojis: List<CustomEmoji>,
    // val reaction: Reaction,
)