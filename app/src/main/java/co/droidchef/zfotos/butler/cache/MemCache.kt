package co.droidchef.zfotos.butler.cache

import android.graphics.Bitmap
import android.util.LruCache

class MemCache(maxMemory: Long) : ImageCache {

    private val cache: LruCache<String, Bitmap>

    init {
        val cacheSize: Int = (maxMemory / 4).toInt()

        cache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String?, value: Bitmap?): Int {
                return value?.byteCount ?: 0 / 1024
            }
        }

    }


    override fun put(url: String, bitmap: Bitmap) {
        cache.put(url, bitmap)
    }

    override fun get(url: String): Bitmap? {
        return cache.get(url)
    }

    override fun clear() {
        cache.evictAll()
    }

    override fun trim() {

    }

}