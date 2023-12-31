package sns.asteroid.model.streaming

import kotlinx.serialization.json.Json
import okhttp3.WebSocket
import sns.asteroid.api.entities.Notification

class MentionStreamingClient(
    private val listener: OnReceiveListener,
    server: String,
    accessToken: String,
): TimelineStreamingClient(listener, generateUri(server, accessToken)) {

    override fun onMessage(webSocket: WebSocket, text: String) {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        val event = json.decodeFromString(Events.serializer(), text)
        val data = event.payload ?: return

        val notification = json.decodeFromString(Notification.serializer(), data)

        if(notification.type == "mention") notification.status?.let { listener.onUpdated(it) }
    }

    companion object {
        private fun generateUri(server: String, accessToken: String): String {
            val stream = "user:notification"
            val uri = StringBuilder()
                .append("wss://${server}/api/v1/streaming/")
                .append("?")
                .append("access_token=${accessToken}")
                .append("&")
                .append("stream=$stream")
            return uri.toString()
        }
    }
}