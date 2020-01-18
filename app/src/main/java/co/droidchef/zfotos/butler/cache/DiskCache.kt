package co.droidchef.zfotos.butler.cache

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.VisibleForTesting
import co.droidchef.zfotos.md5
import com.jakewharton.disklrucache.DiskLruCache
import java.io.*

class DiskCache(private val cache: DiskLruCache) : ImageCache {

    override fun put(url: String, bitmap: Bitmap) {
        val key: String = url.md5()
        cache.edit(key)?.let {
            try {
                if (writeBitmapToFile(bitmap, it)) {
                    cache.flush()
                    it.commit()
                } else {
                    it.abort()
                }
            } catch (exception: IOException) {
                try {
                    it.abort()
                } catch (ignored: IOException) {
                }
            }

        }

    }

    override fun get(url: String): Bitmap? {
        val key = url.md5()
        val snapshot: DiskLruCache.Snapshot? = cache.get(key)
        return if (snapshot != null) {
            val inputStream: InputStream = snapshot.getInputStream(0)
            val buffIn = BufferedInputStream(inputStream, 8 * 1024)
            BitmapFactory.decodeStream(buffIn)
        } else {
            null
        }
    }

    override fun clear() {
        cache.delete()
    }

    override fun trim() {
        // noop
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun writeBitmapToFile(bitmap: Bitmap, editor: DiskLruCache.Editor): Boolean {
        var out: OutputStream? = null
        try {
            out = BufferedOutputStream(editor.newOutputStream(0), 8 * 1024)
            return bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        } finally {
            out?.close()
        }
    }

}