package sns.asteroid.api.entities

import kotlinx.serialization.Serializable

@Serializable
data class Relationship(
    val id: String,
    val following: Boolean,
    val showing_reblogs: Boolean,
    val notifying: Boolean = false, // v3.3.0 or later
    val languages: List<String> = listOf(), // v4.0.0 or later
    val followed_by: Boolean,
    val blocking: Boolean,
    val blocked_by: Boolean,
    val muting: Boolean,
    val muting_notifications: Boolean,
    val requested: Boolean,
    val domain_blocking: Boolean,
    val endorsed: Boolean,
    val note: String = "", // v3.2.0 or later
)