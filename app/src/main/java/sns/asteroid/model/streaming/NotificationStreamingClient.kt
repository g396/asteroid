package sns.asteroid.model.streaming

import kotlinx.serialization.json.Json
import okhttp3.*
import sns.asteroid.api.entities.Notification
import sns.asteroid.db.entities.Credential

/**
 * 通知カラムのストリーミング用WebSocketクライアント
 * (通常のタイムラインは受信するエンティティが異なるので別クラス)
 */
class NotificationStreamingClient(
    private val listener: OnReceiveListener<Notification>,
    uri: String,
): AbstractStreamingClient<Notification>(listener, uri) {

    constructor(listener: OnReceiveListener<Notification>, credential: Credential):
            this(listener, generateUri(credential))

    override fun onMessage(webSocket: WebSocket, text: String) {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        val event = json.decodeFromString(Events.serializer(), text)
        val data = event.payload ?: return

        val notification = json.decodeFromString(Notification.serializer(), data)
        listener.onUpdated(notification)
    }

    companion object {
        private fun generateUri(credential: Credential): String {
            return StringBuilder()
                .append("wss://${credential.instance}/api/v1/streaming")
                .append("?")
                .append("access_token=${credential.accessToken}")
                .append("&")
                .append("stream=user:notification")
                .toString()
        }
    }
}