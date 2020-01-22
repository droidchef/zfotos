package co.droidchef.zfotos.butler.delivery

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView

class ImageDeliveryManager : DeliveryManager {

    override fun deliver(imageParcel: Any, forImageView: ImageView) {
        when (imageParcel) {
            is Bitmap -> forImageView.setImageBitmap(imageParcel)
            is Drawable -> forImageView.setImageDrawable(imageParcel)
            is Int -> forImageView.setImageResource(imageParcel)
            else -> {
                throw IllegalArgumentException("Invalid Image Type")
            }
        }

    }

    override fun inform(requestStatus: RequestStatus, statusListener: ImageLoadingStatusListener) {
        when (requestStatus) {
            RequestStatus.STARTED -> statusListener.onImageLoadingStarted()
            RequestStatus.SUCCESS -> statusListener.onImageLoadingFinished()
            RequestStatus.FAILURE -> statusListener.onImageLoadingFailed()
            RequestStatus.CANCELED -> statusListener.onImageLoadingCanceled()
        }
    }

}