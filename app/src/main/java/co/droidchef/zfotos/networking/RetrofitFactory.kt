package co.droidchef.zfotos.networking

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitFactory(private val networkFactory: NetworkFactory) {

    private fun create(baseUrl: String, okHttpClient: OkHttpClient): Retrofit {
        val gson = GsonBuilder()
        val converterFactory = GsonConverterFactory.create(gson.create())
        val callAdapterFactory = RxJava2CallAdapterFactory.create()

        return Retrofit.Builder().apply {
            baseUrl(baseUrl)
            client(okHttpClient)
            addConverterFactory(converterFactory)
            addCallAdapterFactory(callAdapterFactory)
        }.build()
    }

    val retrofitRandomUserApi: Retrofit by lazy {
        return@lazy create(
            RANDOM_USER_API_BASE_URL,
            networkFactory.okHttpClient
        )
    }

    companion object {
        const val RANDOM_USER_API_BASE_URL = "https://randomuser.me/"
    }

}