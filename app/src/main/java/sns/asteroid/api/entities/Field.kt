package sns.asteroid.api.entities

import kotlinx.serialization.Serializable

@Serializable
data class Field (
    val name: String,
    val value: String,
    val verified_at: String?, // ISO 8601 Datetime
): java.io.Serializable