package co.droidchef.zfotos.butler

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.util.Log
import android.widget.ImageView
import androidx.annotation.DrawableRes
import co.droidchef.zfotos.R
import co.droidchef.zfotos.butler.cache.ImageCache
import co.droidchef.zfotos.butler.delivery.DeliveryManager
import co.droidchef.zfotos.butler.delivery.ImageLoadingStatusListener
import co.droidchef.zfotos.butler.delivery.RequestStatus
import co.droidchef.zfotos.butler.procurement.Downloader
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.lang.ref.WeakReference
import java.util.*

class Butler(
    private val imageCache: ImageCache,
    private val deliveryManager: DeliveryManager,
    private val downloader: Downloader<Bitmap>,
    private val context: Context
) {

    private val componentCallbacks = object : ComponentCallbacks2 {

        override fun onLowMemory() {
            imageCache.trim()
        }

        override fun onConfigurationChanged(newConfig: Configuration) {
            // noop
        }

        override fun onTrimMemory(level: Int) {
            // To keep the implementation simple for now we'll ignore the level
            compositeDisposable.dispose()
            imageCache.clear()
            pendingRequestQueue.clear()
        }

    }

    init {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                it.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                    override fun onLost(network: Network) {
                        super.onLost(network)
                        networkStatus =
                            NetworkStatus.UNAVAILABLE
                    }

                    override fun onAvailable(network: Network) {
                        super.onAvailable(network)
                        networkStatus =
                            NetworkStatus.AVAILABLE
                        processQueuedRequests()
                    }
                })
            }
        }
        context.registerComponentCallbacks(componentCallbacks)
    }

    private enum class NetworkStatus {
        AVAILABLE,
        UNAVAILABLE
    }

    private val compositeDisposable = CompositeDisposable()

    private var networkStatus = NetworkStatus.AVAILABLE

    private val pendingRequestQueue = LinkedList<Pair<WeakReference<ImageView>, String>>()

    fun load(
        url: String,
        imageView: ImageView,
        @DrawableRes placeholder: Int,
        statusListener: ImageLoadingStatusListener
    ) {

        imageCache.get(url)?.also { bitmap ->

            deliveryManager.deliver(bitmap, imageView)
            deliveryManager.inform(RequestStatus.SUCCESS, statusListener)

        } ?: run {

            deliveryManager.deliver(placeholder, imageView)
            deliveryManager.inform(RequestStatus.STARTED, statusListener)

            val subscription = createRequestSubscription(url)

            when (networkStatus) {

                NetworkStatus.AVAILABLE -> {
                    val disposable = createRequestDisposableFromSubscription(
                        subscription,
                        imageView,
                        statusListener,
                        url,
                        placeholder
                    )

                    imageView.tag = disposable

                    compositeDisposable.add(disposable)

                }

                NetworkStatus.UNAVAILABLE -> {

                    deliveryManager.deliver(placeholder, imageView)
                    deliveryManager.inform(RequestStatus.FAILURE, statusListener)

                    pendingRequestQueue.add(Pair(WeakReference(imageView), url))

                }
            }
        }
    }

    /**
     * This method is used to tell Butler that the ImageView is no longer visible or has been
     * recycled, so there is no point loading the image into it. If there is a subscription
     * corresponding to it, please cancel it.
     */
    fun cancelLoad(imageView: ImageView) {
        (imageView.tag)?.let {
            it as Disposable
            if (!it.isDisposed) {
                it.dispose()
            }
            imageView.tag = null
        }
    }

    fun shutDown() {
        compositeDisposable.dispose()
        context.unregisterComponentCallbacks(componentCallbacks)
    }

    private fun createRequestDisposableFromSubscription(
        subscription: Single<Bitmap>,
        into: ImageView,
        statusListener: ImageLoadingStatusListener,
        url: String,
        placeholder: Int
    ): Disposable {
        return subscription
            .subscribe({ bitmap ->

                deliveryManager.deliver(bitmap, into)
                deliveryManager.inform(RequestStatus.SUCCESS, statusListener)

                imageCache.put(url, bitmap)

            }, {

                deliveryManager.deliver(placeholder, into)
                deliveryManager.inform(RequestStatus.FAILURE, statusListener)

                pendingRequestQueue.add(Pair(WeakReference(into), url))
            })
    }

    /**
     * This method <b>generates</b> a subscription to download an Image from a URL.
     */
    private fun createRequestSubscription(imageUrl: String): Single<Bitmap> {

        return Single.just(imageUrl)
            .map { downloader.download(imageUrl) }
            .doOnDispose {
                fetchAndCacheImage(imageUrl)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    }

    /**
     * This method fetches an image and caches it in the image cache.
     * It is usually called when the image is recycled while the image was loading. Since we know
     * that the user has scrolled past that image they won't see it now so we just attempt to
     * download and cache that image for later use.
     */
    private fun fetchAndCacheImage(imageUrl: String) {

        compositeDisposable.add(Single.just(imageUrl)
            .map { downloader.download(imageUrl) }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                imageCache.put(imageUrl, it)
            }, {
                Log.e("Butler", "Failed to download image from $imageUrl", it)
            })
        )

    }

    /**
     * This method processes all the requests that were queued when the Network on the device
     * was unavailable.
     */
    private fun processQueuedRequests() {

        while (pendingRequestQueue.isNotEmpty()) {
            pendingRequestQueue.poll()?.let { pair ->

                compositeDisposable.add(Single.just(pair.second)
                    .map { imageUrl -> downloader.download(imageUrl) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ bitmap ->
                        imageCache.put(pair.second, bitmap)
                        pair.first.get()?.apply {
                            setImageBitmap(bitmap)
                        }
                    }, {
                        pair.first.get()?.apply {
                            setImageResource(R.drawable.loading_placeholder)
                        }
                    })
                )
            }
        }

    }
}