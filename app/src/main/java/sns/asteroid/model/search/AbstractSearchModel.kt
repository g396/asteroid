package sns.asteroid.model.search

import sns.asteroid.CustomApplication
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.timeline.GettingContentsModel

abstract class AbstractSearchModel<T>(val credential: Credential, val query: String, private var offset: Int): GettingContentsModel<T> {
    protected val limit = 20

    override fun reload(): GettingContentsModel.Result<T> {
        offset = 0
        return getOlder()
    }

    override fun getOlder(): GettingContentsModel.Result<T> {
        return search(offset = offset).also {
            offset = offset.plus(it.contents?.size ?: 0)
        }
    }

    override fun getLatest(): GettingContentsModel.Result<T> {
       return search(offset = 0).also {
           offset = it.contents?.size ?: offset
       }
    }

    protected abstract fun search(offset: Int): GettingContentsModel.Result<T>

    protected fun getString(resId: Int): String {
        return CustomApplication.getApplicationContext().getString(resId)
    }
}