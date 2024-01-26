package sns.asteroid.api.entities

import kotlinx.serialization.Serializable

/**
 * @param verified_at ISO-8601 Datetime
 */
@Serializable
data class Field (
    val name: String,
    val value: String,
    val verified_at: String?,
): java.io.Serializable