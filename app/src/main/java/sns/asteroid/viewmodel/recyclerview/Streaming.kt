package sns.asteroid.viewmodel.recyclerview

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sns.asteroid.model.settings.ColumnInfoModel
import sns.asteroid.model.streaming.StreamingClient

/**
 * ストリーミングの接続・切断処理
 */
interface Streaming {
    val streamingClient: StreamingClient
    val hash: String

    var enableStreaming: Boolean

    /**
     * Websocketでのストリーミングを開始する
     */
    suspend fun startStreaming() {
        enableStreaming = true
        setIsEnableStreaming(true)
        withContext(Dispatchers.IO) { streamingClient.connect() }
    }

    /**
     * Websocketでのストリーミングを再度開始する
     */
    suspend fun resumeStreaming() {
        enableStreaming = true
        withContext(Dispatchers.IO) { streamingClient.resume() }
    }

    /**
     * Websocketでのストリーミングを閉じる
     */
    suspend fun stopStreaming() {
        enableStreaming = false
        setIsEnableStreaming(false)
        withContext(Dispatchers.IO) { streamingClient.close() }
    }

    suspend fun setIsEnableStreaming(enabled: Boolean) {
        withContext(Dispatchers.IO) { ColumnInfoModel.setIsEnableStreaming(enabled, hash) }
    }
}