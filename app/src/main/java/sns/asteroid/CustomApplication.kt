package sns.asteroid

import android.app.Application
import android.content.Context

class CustomApplication: Application() {
    init {
        instance = this
    }

    companion object {
        private var instance: CustomApplication? = null

        fun getApplicationContext() : Context {
            return instance!!.applicationContext
        }
    }
}