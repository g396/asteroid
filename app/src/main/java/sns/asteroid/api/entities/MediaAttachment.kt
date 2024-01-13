package sns.asteroid.api.entities

import kotlinx.serialization.Serializable

/**
 * @param type Enumerable (unknown, image, gifv, video, audio)
 * @param preview_url empty when this is a audio file
 * @param remote_url null when this is not a remote file
 * @param description MastodonのドキュメントだとNullableになってないけど本当はNullable
 * @param blurhash 偶に連合でBlurhashが無い物が流れてくる
 */
@Serializable
data class MediaAttachment(
    override val id: String,
    val type: String,
    val url: String,
    val preview_url: String = "",
    val remote_url: String?,
    val meta: Meta,
    val description: String?,
    val blurhash: String = "",
): java.io.Serializable, ContentInterface {

    @Serializable
    data class Meta(
        val focus: Focus? = null,
    ): java.io.Serializable {

        @Serializable
        data class Focus(
            val x: Double,
            val y: Double,
        ): java.io.Serializable
    }
}