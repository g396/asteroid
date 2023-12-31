package sns.asteroid.model.streaming

import java.net.URLEncoder

class HashtagStreamingClient(
    listener: OnReceiveListener,
    server: String,
    accessToken: String,
    hashtag: String,
): TimelineStreamingClient(listener, generateUri(server, accessToken, hashtag)) {
    companion object {
        private fun generateUri(server: String, accessToken: String, hashtag: String): String {
            return StringBuilder()
                .append("wss://${server}/api/v1/streaming/")
                .append("?")
                .append("access_token=${accessToken}")
                .append("&")
                .append("stream=hashtag")
                .append("&")
                .append("tag=${URLEncoder.encode(hashtag, "UTF-8")}")
                .toString()
        }
    }
}