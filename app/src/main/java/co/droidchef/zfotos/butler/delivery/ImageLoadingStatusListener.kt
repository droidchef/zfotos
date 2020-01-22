package co.droidchef.zfotos.butler.delivery

interface ImageLoadingStatusListener {
    fun onImageLoadingStarted()
    fun onImageLoadingFinished()
    fun onImageLoadingFailed()
    fun onImageLoadingCanceled()
}