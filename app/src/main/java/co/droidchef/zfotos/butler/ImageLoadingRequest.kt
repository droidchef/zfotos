package co.droidchef.zfotos.butler

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.io.InterruptedIOException
import java.util.concurrent.Callable

class ImageLoadingRequest(
    private val urlToLoadImageFrom: String,
    private val httpClient: OkHttpClient,
    private val requestCallback: RequestCallback
) : Callable<Unit> {

    private lateinit var inputStream: InputStream

    override fun call() {
        try {
            inputStream = getInputStreamFromUrl()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            requestCallback.onSuccess(bitmap)
        } catch (exception: Exception) {
            when (exception) {
                is InterruptedIOException -> {
                    requestCallback.onCancel()
                } // Someone cancelled the load so lets just do it
                else -> {
                    requestCallback.onFailure(exception)
                }
            }
        } finally {
            inputStream.close()
        }
    }

    private fun getInputStreamFromUrl(): InputStream {
        val request = Request.Builder().url(urlToLoadImageFrom).build()
        val response = httpClient.newCall(request).execute()
        return response.body?.byteStream()!!
    }


    interface RequestCallback {

        fun onSuccess(bitmap: Bitmap)

        fun onFailure(exception: Exception)

        fun onCancel()

    }
}