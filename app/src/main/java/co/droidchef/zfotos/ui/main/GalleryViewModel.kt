package co.droidchef.zfotos.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.droidchef.zfotos.data.response.PhotosResponse
import co.droidchef.zfotos.networking.service.PhotosService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicLong

class GalleryViewModel(private val photosService: PhotosService) : ViewModel() {

    private val atomicLong = AtomicLong(0L)

    private val compositeDisposable = CompositeDisposable()

    private val _picturesLiveData = MutableLiveData<ArrayList<PhotosResponse.Result.Picture>>()

    val picturesLiveData: LiveData<ArrayList<PhotosResponse.Result.Picture>> = _picturesLiveData

    init {
        fetchPhotos()
    }

    private fun fetchPhotos() {

        compositeDisposable.add(photosService.getPhotos()
            .flatMapIterable { it.results }
            .map { result ->
                result.picture.apply {
                    id = atomicLong.getAndIncrement()
                }
            }
            .toList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                _picturesLiveData.postValue(ArrayList(it))
            }

        )
    }

    fun refresh() {
        fetchPhotos()
    }

}
