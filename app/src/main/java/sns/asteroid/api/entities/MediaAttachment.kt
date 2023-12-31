package sns.asteroid.api.entities

import kotlinx.serialization.Serializable

@Serializable
data class MediaAttachment(
    override val id: String,
    val type: String, // Enumerable (unknown, image, gifv, video, audio)
    val url: String,
    val preview_url: String = "", // empty if the attachment is audio file
    val remote_url: String?, // null if the attachment is local
    // val meta: Map<*,*>, // なんやねんこれ
    val description: String?, // MastodonのドキュメントだとNullableになってないけど本当はNullable
    val blurhash: String = "", // たまに何故かblurhashの無いやつが流れてくるのは何
): java.io.Serializable, ContentInterface