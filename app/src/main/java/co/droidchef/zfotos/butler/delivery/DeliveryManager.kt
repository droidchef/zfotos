package co.droidchef.zfotos.butler.delivery

import android.widget.ImageView

interface DeliveryManager {
    fun deliver(imageParcel: Any, forImageView: ImageView)
    fun inform(requestStatus: RequestStatus, statusListener: ImageLoadingStatusListener)
}