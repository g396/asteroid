package sns.asteroid.api.entities

import kotlinx.serialization.Serializable

/**
 * It is not Android.Context
 */

@Serializable
data class Context(
    val ancestors: List<Status>? = null,
    val descendants: List<Status>? = null,
)