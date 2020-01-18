package co.droidchef.zfotos.butler.cache

import android.graphics.Bitmap

class DoubleStoryCache(private val level1Cache: ImageCache, private val level2Cache: ImageCache) :
    ImageCache {

    override fun put(url: String, bitmap: Bitmap) {
        level1Cache.put(url, bitmap)
        level2Cache.put(url, bitmap)
    }

    override fun get(url: String): Bitmap? {
        return level1Cache.get(url) ?: level2Cache.get(url)
    }

    override fun clear() {
        level1Cache.clear()
        level2Cache.clear()
    }

    override fun trim() {
        level1Cache.clear()
    }

}