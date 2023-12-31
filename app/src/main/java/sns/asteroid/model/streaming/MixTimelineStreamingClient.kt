package sns.asteroid.model.streaming

import sns.asteroid.db.entities.Credential

/**
 * LTLとホームのストリーミングを同時に行うWebSocketクライアント
 */
class MixTimelineStreamingClient(
    val listener: TimelineStreamingClient.OnReceiveListener,
    val credential: Credential,
): StreamingClient {
    private val home = TimelineStreamingClient(listener, "home", credential)
    private val local = TimelineStreamingClient(listener,"local", credential)

    override fun connect() {
        home.connect()
        local.connect()
    }

    override fun resume() {
        home.resume()
        local.resume()
    }

    override fun close() {
        home.close()
        local.close()
    }

    override fun isConnecting(): Boolean {
        return home.isConnecting() and local.isConnecting()
    }
}