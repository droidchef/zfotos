package co.droidchef.zfotos.butler

import android.annotation.SuppressLint
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import androidx.annotation.DrawableRes
import co.droidchef.zfotos.butler.cache.ImageCache
import okhttp3.OkHttpClient
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

object Butler {

    private val executorService = Executors.newScheduledThreadPool(8)

    private var imageCache: ImageCache? = null

    fun setImageCache(imageCache: ImageCache) {
        this.imageCache = imageCache
    }

    @SuppressLint("UseSparseArrays")
    private val jobMap = HashMap<Long, ScheduledFuture<Unit>>()

    private val client: OkHttpClient by lazy { OkHttpClient.Builder().build() }

    private val handler by lazy { Handler(Looper.getMainLooper()) }

    fun load(url: String, into: ImageView, @DrawableRes placeholder: Int) {

        imageCache?.get(url)?.also {

            into.setImageBitmap(it)

        } ?: run {

            val cb = object : ImageLoadingRequest.RequestCallback {
                override fun onSuccess(bitmap: Bitmap) {
                    imageCache?.put(url, bitmap)
                    handler.post {
                        into.setImageBitmap(bitmap)
                    }
                }

                override fun onFailure(exception: Exception) {
                    handler.post {
                        into.setImageResource(placeholder)
                    }
                }

                override fun onCancel() {
                    // noop
                }

            }

            val request = ImageLoadingRequest(url, client, cb)

            val job = executorService.schedule(request, 0, TimeUnit.MILLISECONDS)

            jobMap[into.tag as Long] = job

        }
    }

    fun cancelLoad(imageView: ImageView) {
        val job = jobMap[imageView.tag as Long]

        job?.let {
            it.cancel(true)
            jobMap.remove(imageView.tag as Long)
        }

    }

    fun shutDown() {
        Log.d("ImageLoader", "is going to shut down now...")
        if (!executorService.isShutdown) {
            executorService.shutdownNow()
        }
    }

    val callbacks2 = object : ComponentCallbacks2 {

        override fun onLowMemory() {
            Log.d("Butler", "ON LOW MEMORY")

            imageCache?.trim()
        }

        override fun onConfigurationChanged(newConfig: Configuration) {
            // noop
        }

        override fun onTrimMemory(level: Int) {
            // To keep the implementation simple for now we'll ignore the level
            Log.d("Butler", "ON TRIM MEMORY")
            imageCache?.clear()
        }

    }


}