package sns.asteroid.model.streaming

import kotlinx.serialization.Serializable

interface StreamingClient {
    fun connect()
    fun resume()
    fun close()

    fun isConnecting(): Boolean

    @Serializable
    data class Events(
        val stream: List<String>,
        val event: String,
        val payload: String?,
    )
}