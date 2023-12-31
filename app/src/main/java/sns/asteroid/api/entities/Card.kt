package sns.asteroid.api.entities

import kotlinx.serialization.Serializable

@Serializable
data class Card(
    val url: String,
    val title: String,
    val description: String,
    val type: String,
    val author_name: String,
    val author_url: String,
    val provider_name: String,
    val provider_url: String,
    val html: String,
    val width: Int,
    val height: Int,
    val image: String? = null,
    val embed_url: String,
    val blueHash: String? = null,
): java.io.Serializable