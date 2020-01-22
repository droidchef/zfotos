package co.droidchef.zfotos.butler.cache

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import co.droidchef.zfotos.butler.cache.DiskCache
import co.droidchef.zfotos.md5
import com.jakewharton.disklrucache.DiskLruCache
import io.mockk.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.IOException
import java.io.InputStream

class DiskCacheTest {

    private val diskLruCache = mockk<DiskLruCache>(relaxed = true)

    private val diskCache = spyk(DiskCache(diskLruCache))

    private val key = "sexy"

    @Test
    fun `verify lru cache is accessed with md5 hashed key only when writing a bitmap`() {
        val bmp = mockk<Bitmap>(relaxed = true)

        mockkStatic("co.droidchef.zfotos.StringExtKt")

        diskCache.put(key, bmp)

        assertEquals("348a448a51d1e0f0f5eee42337d12adc", key.md5())

        verify { diskLruCache.edit("348a448a51d1e0f0f5eee42337d12adc") }
    }

    @Test
    fun `verify a successful put operation in disk cache `() {

        val bmp = mockk<Bitmap>(relaxed = true)

        val editor = mockk<DiskLruCache.Editor>(relaxed = true)

         val key = "sexy"

        every { diskLruCache.edit("348a448a51d1e0f0f5eee42337d12adc") } returns editor

        every { diskCache.writeBitmapToFile(bmp, editor) } returns true

        diskCache.put("sexy", bmp)

        verify {
            key.md5()
            diskLruCache.edit("348a448a51d1e0f0f5eee42337d12adc")
            diskCache.writeBitmapToFile(bmp, editor)
            diskLruCache.flush()
            editor.commit()
        }

    }

    @Test
    fun `verify editor aborts gracefully when it cannot write Bitmap to file`() {

        val bmp = mockk<Bitmap>(relaxed = true)

        val editor = mockk<DiskLruCache.Editor>(relaxed = true)

        every { diskLruCache.edit("348a448a51d1e0f0f5eee42337d12adc") } returns editor

        every { diskCache.writeBitmapToFile(bmp, editor) } returns false

        diskCache.put("sexy", bmp)

        verify(ordering = Ordering.SEQUENCE) {
            diskCache.put("sexy", bmp)
            key.md5()
            diskLruCache.edit("348a448a51d1e0f0f5eee42337d12adc")
            diskCache.writeBitmapToFile(bmp, editor)
            editor.abort()
        }
    }

    @Test
    fun `verify editor tries to abort again when exception is throw during abort process`() {

        val bmp = mockk<Bitmap>(relaxed = true)

        val editor = mockk<DiskLruCache.Editor>(relaxed = true)

        every { diskCache.writeBitmapToFile(bmp, editor) } answers {
            false
        }
        every { diskLruCache.edit("348a448a51d1e0f0f5eee42337d12adc") } returns editor

        every { editor.abort() } answers {
            throw IOException()
        }

        diskCache.put("sexy", bmp)

        verify(exactly = 2) {
            editor.abort()
        }
    }

    @Test
    fun `verify cache is cleared correctly`() {

        diskCache.clear()

        verify {
            diskLruCache.delete()
        }

    }

    @Test
    fun `verify lru cache is accessed when getting a bitmap`() {
        val bmp = mockk<Bitmap>(relaxed = true)

        val snapshot = mockk<DiskLruCache.Snapshot>(relaxed = true)

        val inputStream = mockk<InputStream>(relaxed = true)

        mockkStatic("co.droidchef.zfotos.StringExtKt")

        every { diskLruCache.get("348a448a51d1e0f0f5eee42337d12adc") } returns snapshot

        every { snapshot.getInputStream(0) } returns inputStream

        mockkStatic(BitmapFactory::class)

        every { BitmapFactory.decodeStream(any()) } returns bmp

        val output = diskCache.get(key)

        verify {
            key.md5()
            diskLruCache.get("348a448a51d1e0f0f5eee42337d12adc")
        }

        assertEquals(output, bmp)

        assertEquals("348a448a51d1e0f0f5eee42337d12adc", key.md5())
    }

}