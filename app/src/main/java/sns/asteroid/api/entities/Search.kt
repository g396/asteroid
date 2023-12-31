package sns.asteroid.api.entities

@kotlinx.serialization.Serializable
data class Search(
    val accounts: List<Account> = emptyList(),
    val statuses: List<Status>  = emptyList(),
    val hashtags: List<Tag>     = emptyList(),
): java.io.Serializable
