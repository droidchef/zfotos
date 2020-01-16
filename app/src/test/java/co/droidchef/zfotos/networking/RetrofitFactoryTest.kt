package co.droidchef.zfotos.networking

import com.google.gson.Gson
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class RetrofitFactoryTest {

    private val networkFactory = mockk<NetworkFactory>(relaxed = true)
    private val gson = mockk<Gson>(relaxed = true)

    @Test
    fun `verify random user api retrofit instance has correct base url`() {

        val retrofit = RetrofitFactory(networkFactory.okHttpClient, gson).retrofitRandomUserApi

        assertEquals(RetrofitFactory.RANDOM_USER_API_BASE_URL, retrofit.baseUrl().toString())

    }

}