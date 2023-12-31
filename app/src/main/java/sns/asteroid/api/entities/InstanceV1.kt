package sns.asteroid.api.entities

import kotlinx.serialization.Serializable

@Serializable
data class InstanceV1(
    val uri: String,
    val title: String,
    val version: String,
    val short_description: String,
    val thumbnail: String,
    val languages: List<String>,
    val configuration: Configuration? = null, // added at 3.4.2
    val registrations: Boolean,
    val contact_account: Account,
){
    @Serializable
    data class Configuration(
        val accounts: Accounts? = null,
        val statuses: Statuses,
        val media_attachments: MediaAttachments,
        val polls: Polls,
    ){
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
    }
}

