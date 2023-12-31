package sns.asteroid.api.entities

import kotlinx.serialization.Serializable

@Serializable
data class ListTimeline(
    override val id: String,
    val title: String,
    val replies_policy: String,
    val exclusive: Boolean? = null,
): ContentInterface