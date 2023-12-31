package sns.asteroid.api.entities

import kotlinx.serialization.Serializable

@Serializable
data class Tag (
    val name: String,
    val url: String,
    val history: List<History> = emptyList(),
    val following: Boolean = false,
): java.io.Serializable, ContentInterface {
    override val id get() = name

    @Serializable
    data class History(
        val day: String,
        val uses: Int,
        val accounts: Int,
    ): java.io.Serializable

    val countSum = history.associateWith { it.uses }
            .toList()
            .unzip().second
            .sum()
            .toString()
}