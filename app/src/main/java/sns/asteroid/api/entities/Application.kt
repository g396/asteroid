package sns.asteroid.api.entities

import kotlinx.serialization.Serializable

@Serializable

data class Application(
    val name: String = "",
    val website: String? = null,
    val vapid_key: String = "",
    val client_id: String? = null,
    val client_secret: String? = null,
): java.io.Serializable