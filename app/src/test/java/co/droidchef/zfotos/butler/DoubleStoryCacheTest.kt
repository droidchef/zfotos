package co.droidchef.zfotos.butler

import android.graphics.Bitmap
import co.droidchef.zfotos.butler.cache.DoubleStoryCache
import co.droidchef.zfotos.butler.cache.ImageCache
import io.mockk.*
import org.junit.Assert.assertEquals
import org.junit.Test


class DoubleStoryCacheTest {

    private val memCache = mockk<ImageCache>(relaxed = true)

    private val diskCache = mockk<ImageCache>(relaxed = true)

    private val doubleStoryCache = spyk(DoubleStoryCache(memCache, diskCache))

    @Test
    fun `verify that writing to double story cache, updates both underlying caches`() {

        val key = "stinking-cat.png"

        val bitmap = mockk<Bitmap>(relaxed = true)

        doubleStoryCache.put(key, bitmap)

        verify(ordering = Ordering.SEQUENCE) {
            memCache.put(key, bitmap)
            diskCache.put(key, bitmap)
        }

    }

    @Test
    fun `verify that reading from double story cache, short circuits at level 1 cache correctly`() {

        val key = "stinking-cat.png"

        val bitmap = mockk<Bitmap>(relaxed = true)

        every { memCache.get(key) } answers { bitmap }

        val bitmapRetrieved = doubleStoryCache.get(key)

        verify(exactly = 1) {
            memCache.get(key)
        }

        verify { diskCache wasNot Called }

        assertEquals(bitmap, bitmapRetrieved)

    }

    @Test
    fun `verify that level 2 cache is check if level 1 returns null`() {

        val key = "stinking-cat.png"
        val bitmap = mockk<Bitmap>(relaxed = true)

        every { memCache.get(key) } answers { null }

        every { diskCache.get(key) } answers { bitmap }

        val bitmapRetrieved = doubleStoryCache.get(key)

        verify(exactly = 1) { memCache.get(key) }

        verify(exactly = 1) { diskCache.get(key) }

        assertEquals(bitmap, bitmapRetrieved)
    }

    @Test
    fun `verify that trim only clears level 1 cache`() {

        doubleStoryCache.trim()

        verify {
            memCache.clear()
            diskCache wasNot Called
        }
    }

    @Test
    fun `verify that clear, clears both the underlying caches`() {

        doubleStoryCache.clear()

        verify(ordering = Ordering.SEQUENCE) {
            memCache.clear()
            diskCache.clear()
        }
    }

}