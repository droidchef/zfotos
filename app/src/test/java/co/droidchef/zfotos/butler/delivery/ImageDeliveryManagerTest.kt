package co.droidchef.zfotos.butler.delivery

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test

class ImageDeliveryManagerTest {


    private val deliveryManager = spyk<ImageDeliveryManager>()

    private val imageView = mockk<ImageView>(relaxed = true)

    @Test
    fun testBitmapDelivery() {

        val bitmap = mockk<Bitmap>(relaxed = true)

        deliveryManager.deliver(bitmap, imageView)

        verify { imageView.setImageBitmap(bitmap) }

    }

    @Test
    fun testDrawableDelivery() {

        val drawable = mockk<Drawable>(relaxed = true)

        deliveryManager.deliver(drawable, imageView)

        verify { imageView.setImageDrawable(drawable) }

    }

    @Test
    fun testResourceDelivery() {

        @DrawableRes val placeholder = 23

        deliveryManager.deliver(placeholder, imageView)

        verify { imageView.setImageResource(placeholder) }

    }




}