package co.droidchef.zfotos.butler.procurement

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.WorkerThread
import okhttp3.OkHttpClient
import okhttp3.Request

class HttpDownloader(private val okHttpClient: OkHttpClient) : Downloader<Bitmap> {

    @WorkerThread
    override fun download(fromSource: Any): Bitmap {
        when (fromSource) {
            is String -> {
                val request = Request.Builder().url(fromSource).build()
                val response = okHttpClient.newCall(request).execute()
                val inputStream = response.body?.byteStream()
                inputStream.use {
                    return BitmapFactory.decodeStream(inputStream)
                }
            }
            else -> {
                throw Throwable("Can't download from a source other than String")
            }
        }
    }

}