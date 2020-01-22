package co.droidchef.zfotos.di

import android.content.Context
import android.graphics.Bitmap
import co.droidchef.zfotos.butler.Butler
import co.droidchef.zfotos.butler.cache.DiskCache
import co.droidchef.zfotos.butler.cache.DoubleStoryCache
import co.droidchef.zfotos.butler.cache.ImageCache
import co.droidchef.zfotos.butler.cache.MemCache
import co.droidchef.zfotos.butler.delivery.DeliveryManager
import co.droidchef.zfotos.butler.delivery.ImageDeliveryManager
import co.droidchef.zfotos.butler.procurement.Downloader
import co.droidchef.zfotos.butler.procurement.HttpDownloader
import co.droidchef.zfotos.networking.NetworkFactory
import co.droidchef.zfotos.networking.RetrofitFactory
import co.droidchef.zfotos.networking.service.PhotosService
import co.droidchef.zfotos.ui.main.GalleryViewModel
import co.droidchef.zfotos.utils.ViewModelSchedulersProvider
import com.google.gson.GsonBuilder
import com.jakewharton.disklrucache.DiskLruCache
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

val viewModelModule = module {
    viewModel { GalleryViewModel(get()) }

val butlerModule = module {

    fun provideDiskCache(context: Context): DiskCache {
        return DiskCache(DiskLruCache.open(context.cacheDir, 1, 1, 10 * 1024 * 1024))
    }

    // Using a separate OkHttpClient for the image loading library as we don't want to overload
    // the connection pool of the other OkHttpClient used for making normal network requests
    // for the REST API web services.
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().apply {
            followRedirects(true)
            followSslRedirects(true)
            connectTimeout(60, TimeUnit.SECONDS)
            readTimeout(60, TimeUnit.SECONDS)
        }.build()
    }

    fun provideMemoryCache(): ImageCache {
        return MemCache(Runtime.getRuntime().maxMemory())
    }

    single { Butler(get(), get(), get(), get()) }

    // Delivery Layer
    single<DeliveryManager> { ImageDeliveryManager() }

    // Building the Memory Layer
    single<ImageCache> {
        DoubleStoryCache(
            provideMemoryCache(),
            provideDiskCache(androidContext())
        )
    }

    // Building Procurement Layer
    single<Downloader<Bitmap>> { HttpDownloader(provideOkHttpClient()) }

}

val apiModule = module {

    fun providePhotosService(retrofit: Retrofit): PhotosService {
        return retrofit.create(PhotosService::class.java)
    }

    single { providePhotosService(get()) }
}

val networkModule = module {

    single { GsonBuilder().create() }
    single { NetworkFactory().okHttpClient }
    single { RetrofitFactory(get(), get()).retrofitRandomUserApi }

}

val applicationModule = module {

}