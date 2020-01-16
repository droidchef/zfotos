package co.droidchef.zfotos.networking.service

import co.droidchef.zfotos.data.response.PhotosResponse
import io.reactivex.Observable
import retrofit2.http.GET

interface PhotosService {

    @GET("api/?results=500")
    fun getPhotos() : Observable<PhotosResponse>

}