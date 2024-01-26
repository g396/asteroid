package sns.asteroid.api.entities

import kotlinx.serialization.Serializable

@Serializable
data class FilterResult(
    val filter: Filter,
    val keyword_matches: List<String>? = null,
    val status_matches: String? = null,
): java.io.Serializable {

    /**
     * @param expires_at ISO-8601 Datetime
     * @param filter_action Enumerable (warn, hide)
     */
    @Serializable
    data class Filter(
        val id: String,
        val title: String,
        val context: List<String>,
        val expires_at: String? = null,
        val filter_action: String = "warn",
        val keywords: FilterKeyword? = null,
        val statuses: FilterStatus? = null,
    ): java.io.Serializable {

        @Serializable
        data class FilterKeyword(
            val id: String,
            val keyword: String,
            val whole_word: Boolean,
        ): java.io.Serializable

        @Serializable
        data class FilterStatus(
            val id: String,
            val status_id: Status,
        ): java.io.Serializable
    }
}