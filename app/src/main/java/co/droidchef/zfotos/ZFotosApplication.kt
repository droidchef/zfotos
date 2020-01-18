package co.droidchef.zfotos

import android.app.Application
import co.droidchef.zfotos.butler.Butler
import co.droidchef.zfotos.butler.cache.DiskCache
import co.droidchef.zfotos.butler.cache.DoubleStoryCache
import co.droidchef.zfotos.butler.cache.MemCache
import co.droidchef.zfotos.di.apiModule
import co.droidchef.zfotos.di.applicationModule
import co.droidchef.zfotos.di.networkModule
import co.droidchef.zfotos.di.viewModelModule
import com.jakewharton.disklrucache.DiskLruCache
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class ZFotosApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ZFotosApplication)
            androidLogger(Level.DEBUG)
            modules(listOf(apiModule, networkModule, applicationModule, viewModelModule))
        }

        Butler.setImageCache(
            DoubleStoryCache(
                MemCache(Runtime.getRuntime().maxMemory()),
                DiskCache(DiskLruCache.open(this.cacheDir, 1, 1, 10 * 1024 * 1024))
            )
        )
        registerComponentCallbacks(Butler.callbacks2)
    }

    override fun onTerminate() {
        unregisterComponentCallbacks(Butler.callbacks2)
        super.onTerminate()
    }
}