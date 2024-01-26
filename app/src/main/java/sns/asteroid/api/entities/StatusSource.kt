package sns.asteroid.api.entities

@kotlinx.serialization.Serializable
data class StatusSource(
    val id: String,
    val text: String,
    val spoiler_text: String,
): java.io.Serializable
