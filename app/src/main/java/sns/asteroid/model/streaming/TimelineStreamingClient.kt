package sns.asteroid.model.streaming

import kotlinx.serialization.json.Json
import okhttp3.*
import sns.asteroid.api.entities.Status
import sns.asteroid.db.entities.ColumnInfo
import sns.asteroid.db.entities.Credential

/**
 * タイムラインのストリーミング用WebSocketクライアント
 * (通知は受信するエンティティが異なるので別クラス)
 */
open class TimelineStreamingClient(
    private val listener: OnReceiveListener,
    uri: String,
): AbstractStreamingClient<Status>(listener, uri) {
    constructor(listener: OnReceiveListener, columnInfo: ColumnInfo, credential: Credential):
            this(listener, generateUri(columnInfo, credential))

    constructor(listener: OnReceiveListener, subject: String, credential: Credential):
            this(listener, generateUri(subject, credential))

    override fun onMessage(webSocket: WebSocket, text: String) {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        val event = json.decodeFromString(Events.serializer(), text)
        val data = event.payload ?: return

        when(event.event) {
            "update" -> listener.onUpdated(json.decodeFromString(Status.serializer(), data))
            "delete" -> listener.onDeleted(data)
            "status.update" -> listener.onEdited(json.decodeFromString(Status.serializer(), data))
        }
    }

    companion object {
        private fun generateUri(subject: String, credential: Credential): String {
            val stream = when (subject) {
                "local"     -> "public:local"
                "public"    -> "public"
                "home"      -> "user"
                else        -> "public"
            }

            val uri = StringBuilder()
                .append("wss://${credential.instance}/api/v1/streaming/")
                .append("?")
                .append("access_token=${credential.accessToken}")
                .append("&")
                .append("stream=$stream")

            return uri.toString()
        }

        private fun generateUri(columnInfo: ColumnInfo, credential: Credential): String {
            val stream = when (columnInfo.subject) {
                "local"     -> "public:local"
                "public"    -> "public"
                "home"      -> "user"
                "list"      -> "list"
                "hashtag"   -> "hashtag"
                "local_media" -> "public:local:media"
                "public_media" -> "public:media"
                else        -> "public"
            }
            val uri = StringBuilder()
                .append("wss://${credential.instance}/api/v1/streaming/")
                .append("?")
                .append("access_token=${credential.accessToken}")
                .append("&")
                .append("stream=$stream")
            return uri.toString()
        }
    }

    interface OnReceiveListener: AbstractStreamingClient.OnReceiveListener<Status> {
        fun onEdited(status: Status)
        fun onDeleted(statusId: String)
    }
}