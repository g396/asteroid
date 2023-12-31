package sns.asteroid.model.timeline

interface GettingContentsModel<T> {
    fun getLatest(): Result<T>
    fun getOlder(): Result<T>
    fun reload(): Result<T>

    data class Result<T>(
        val isSuccess: Boolean,
        val contents: List<T>?      = null,
        val toastMessage: String?   = null,
        val maxId: String?          = null,
        val sinceId: String?        = null,
    )
}