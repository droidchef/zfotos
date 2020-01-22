package co.droidchef.zfotos.butler.procurement

interface Downloader<T> {

    fun download(fromSource: Any): T

}