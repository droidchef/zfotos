package co.droidchef.zfotos.networking.service

import co.droidchef.zfotos.data.response.PhotosResponse
import io.reactivex.Observable
import retrofit2.http.GET

interface PhotosService {

    @GET("api/?results=500&seed=942e52eb8a91698d")
    fun getPhotos() : Observable<PhotosResponse>

}