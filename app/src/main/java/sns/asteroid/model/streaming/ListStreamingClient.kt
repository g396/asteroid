package sns.asteroid.model.streaming

class ListStreamingClient(
    listener: OnReceiveListener,
    server: String,
    accessToken: String,
    listId: String,
): TimelineStreamingClient(listener, generateUri(server, accessToken, listId)) {
    companion object {
        private fun generateUri(server: String, accessToken: String, listId: String): String {
            return StringBuilder()
                .append("wss://${server}/api/v1/streaming/")
                .append("?")
                .append("access_token=${accessToken}")
                .append("&")
                .append("stream=list")
                .append("&")
                .append("list=${listId}")
                .toString()
        }
    }
}