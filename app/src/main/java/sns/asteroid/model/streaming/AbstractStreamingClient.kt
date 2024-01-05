package sns.asteroid.model.streaming

import kotlinx.serialization.Serializable
import okhttp3.*

abstract class AbstractStreamingClient<T>(
    private val listener: OnReceiveListener<T>,
    private val uri: String,
): StreamingClient, WebSocketListener() {
    private var webSocket: WebSocket? = null
    private var isConnecting = false

    companion object {
        private val httpClient = OkHttpClient()
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        isConnecting = true
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        isConnecting = false
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        isConnecting = false
        response?.body?.string()?.let { listener.onMessage(it) }
    }

    override fun connect() {
        close()
        val request = Request.Builder().url(uri).build()
        webSocket = httpClient.newWebSocket(request, this)
    }

    override fun resume() {
        if(!isConnecting) connect()
    }

    override fun close() {
        webSocket?.close(1000, null)
    }

    override fun isConnecting(): Boolean {
        return isConnecting
    }

    @Serializable
    data class Events(
        val stream: List<String>,
        val event: String,
        val payload: String?,
    )

    interface OnReceiveListener<T> {
        fun onUpdated(content: T)
        fun onMessage(message: String)
    }
}