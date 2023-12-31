package sns.asteroid.api.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Mastodon 4.0.0 or later
@Serializable
data class InstanceV2(
    val domain: String,
    val title: String,
    val version: String,
    val source_url: String,
    val description: String,
    val usage: Usage,
    val thumbnail: Thumbnail,
    val languages: List<String>,
    val configuration: Configuration,
    val registrations: Registrations,
    val contact: Contact,
    val rules: List<Rules>,
){
    @Serializable
    data class Usage(
        val users: Users,
    ){
        @Serializable
        data class Users(
            val active_month: Int,
        )
    }
    @Serializable
    data class Thumbnail(
        val url: String,
        val blurhash: String? = null,
        val versions: Versions? = null,
    ){
        @Serializable
        data class Versions(
            @SerialName("@1x") val at1x: String? = null,
            @SerialName("@2x") val at2x: String? = null,
        )
    }
    @Serializable
    data class Configuration(
        val urls: Urls,
        val accounts: Accounts,
        val statuses: Statuses,
        val media_attachments: MediaAttachments,
        val polls: Polls,
        val translation: Translation,
    ){
        @Serializable
        data class Urls(
            val streaming_api: String = "",
        )
        @Serializable
        data class Accounts(
            val max_featured_tags: Int,
        )
        @Serializable
        data class Statuses(
            val max_characters: Int,
            val max_media_attachments: Int,
            val characters_reserved_per_url: Int,
        )
        @Serializable
        data class MediaAttachments(
            val supported_mime_types: List<String>,
            val image_size_limit: Int,
            val image_matrix_limit: Int,
            val video_size_limit: Int,
            val video_frame_rate_limit: Int,
            val video_matrix_limit: Int,
        )
        @Serializable
        data class Polls(
            val max_options: Int,
            val max_characters_per_option: Int,
            val min_expiration: Int,
            val max_expiration: Int,
        )
        @Serializable
        data class Translation(
            val enabled: Boolean = false,
        )
    }

    @Serializable
    data class Registrations(
        val enabled: Boolean = false,
        val approval_required: Boolean = false,
        val message: String? = null,
    )
    @Serializable
    data class Contact(
        val email: String,
        val account: Account,
    )
    @Serializable
    data class Rules(
        val id: String,
        val text: String,
    )

    fun rulesToString(): String {
        return StringBuilder().apply {
            rules.forEach { append("${it.text}\n\n") }
        }.toString().trimEnd()
    }
}

