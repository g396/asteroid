package sns.asteroid.api.entities

import kotlinx.serialization.Serializable

@Serializable
data class Token(
    val access_token: String,
    val token_type: String,
    val scope: String,
    val created_at: Int,
)
