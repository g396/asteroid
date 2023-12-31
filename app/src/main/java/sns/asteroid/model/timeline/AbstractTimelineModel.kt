package sns.asteroid.model.timeline

import sns.asteroid.CustomApplication
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.timeline.GettingContentsModel.Result

abstract class AbstractTimelineModel<T>(protected val credential: Credential):
    GettingContentsModel<T> {
    var maxId: String? = null
    var sinceId: String? = null

    override fun getLatest(): Result<T> {
        return getContents(maxId = null, sinceId = sinceId)
            .also { res ->
                res.sinceId?.let { sinceId ->
                    this.sinceId = sinceId
                }
                res.maxId?.let { maxId ->
                    if(this.maxId == null) this.maxId = maxId
                }
            }
    }

    override fun getOlder(): Result<T> {
        return getContents(maxId = maxId, sinceId = null)
            .also { res ->
                res.maxId?.let { maxId ->
                    this.maxId = maxId
                }
            }
    }

    override fun reload(): Result<T> {
        maxId = null
        sinceId = null
        return getLatest()
    }

    protected abstract fun getContents(maxId: String?, sinceId: String?): Result<T>

    protected fun getString(resId: Int): String {
        return CustomApplication.getApplicationContext().getString(resId)
    }
}