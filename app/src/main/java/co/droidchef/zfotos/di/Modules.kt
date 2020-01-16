package co.droidchef.zfotos.di

import co.droidchef.zfotos.networking.NetworkFactory
import co.droidchef.zfotos.networking.RetrofitFactory
import co.droidchef.zfotos.networking.service.PhotosService
import co.droidchef.zfotos.ui.main.GalleryViewModel
import com.google.gson.GsonBuilder
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

val viewModelModule = module {
    viewModel { GalleryViewModel(get()) }
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