package co.droidchef.zfotos.networking

import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NetworkFactoryTest {

    private lateinit var okHttpClient: OkHttpClient

    @Before
    fun setup() {
        okHttpClient = NetworkFactory(true).okHttpClient
    }

    @Test
    fun `verify if client is configured to follow redirects`() {
        assertEquals(true, okHttpClient.followRedirects)
    }

    @Test
    fun `verify if client is configured to follow ssl redirects`() {
        assertEquals(true, okHttpClient.followSslRedirects)
    }

    @Test
    fun `verify if client has connect timeout of 60 seconds`() {
        assertEquals(60000, okHttpClient.connectTimeoutMillis)
    }

    @Test
    fun `verify if client has read timeout of 60 seconds`() {
        assertEquals(60000, okHttpClient.readTimeoutMillis)
    }

    @Test
    fun `verify that client has only one interceptor injected`() {
        assertEquals(1, okHttpClient.interceptors.size)
    }

    @Test
    fun `verify that client has no interceptors injected in release build`() {
        val client = NetworkFactory(false).okHttpClient
        assertEquals(0, client.interceptors.size)
    }

}