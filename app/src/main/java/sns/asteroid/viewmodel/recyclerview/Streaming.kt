package sns.asteroid.viewmodel.recyclerview

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sns.asteroid.model.streaming.StreamingClient

/**
 * ストリーミングの接続・切断処理
 */
interface Streaming {
    val streamingClient: StreamingClient

    /**
     * Websocketでのストリーミングを開始する
     */
    suspend fun startStreaming() {
        withContext(Dispatchers.IO) { streamingClient.connect() }
    }

    /**
     * Websocketでのストリーミングを再度開始する
     */
    suspend fun resumeStreaming() {
        withContext(Dispatchers.IO) { streamingClient.resume() }
    }

    /**
     * Websocketでのストリーミングを閉じる
     */
    suspend fun stopStreaming() {
        withContext(Dispatchers.IO) { streamingClient.close() }
    }
}