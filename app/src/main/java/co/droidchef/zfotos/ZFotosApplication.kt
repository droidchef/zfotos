package co.droidchef.zfotos

import android.app.Application
import co.droidchef.zfotos.di.apiModule
import co.droidchef.zfotos.di.applicationModule
import co.droidchef.zfotos.di.networkModule
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
            modules(listOf(apiModule, networkModule, applicationModule))
        }
    }
}