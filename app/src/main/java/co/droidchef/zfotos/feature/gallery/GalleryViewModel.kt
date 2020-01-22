package co.droidchef.zfotos.feature.gallery

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.droidchef.zfotos.butler.Butler
import co.droidchef.zfotos.networking.service.PhotosService
import co.droidchef.zfotos.utils.SchedulersProvider
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import java.util.concurrent.atomic.AtomicLong

class GalleryViewModel(
    private val photosService: PhotosService,
    val butler: Butler,
    private val schedulersProvider: SchedulersProvider
) : ViewModel() {

    enum class GalleryViewState {
        LOADING,
        SUCCESS,
        ERROR
    }

    private val atomicLong = AtomicLong(0L)

    private val compositeDisposable = CompositeDisposable()

    private val _picturesLiveData = MutableLiveData<ArrayList<Pair<String, Long>>>()

    val pictures: LiveData<ArrayList<Pair<String, Long>>> = _picturesLiveData

    private val _galleryViewStateLiveData = MutableLiveData<GalleryViewState>()

    val galleryViewState: LiveData<GalleryViewState> = _galleryViewStateLiveData

    init {
        fetchPhotos()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun fetchPhotos() {
        _galleryViewStateLiveData.postValue(GalleryViewState.LOADING)
        compositeDisposable.add(photosService.getPhotos()
            .flatMapIterable { it.results }
            // If you're wondering, why am I tinkering with an atomicLong here.
            // The simple reason is that the test api (randomuser.me) doesn't provide unique IDs
            // I need them to establish and satisfy a stable IDs contract with my RecyclerAdapter
            // So I am just creating fake incrementing ID for now. This can go away when we change the
            // data source.
            .map { result -> Pair(result.picture.large, atomicLong.getAndIncrement()) }
            .toList()
            .subscribeOn(schedulersProvider.io())
            .observeOn(schedulersProvider.ui())
            .subscribeBy({
                _galleryViewStateLiveData.postValue(GalleryViewState.ERROR)
                _picturesLiveData.postValue(ArrayList())
            }, {
                _galleryViewStateLiveData.postValue(GalleryViewState.SUCCESS)
                _picturesLiveData.postValue(ArrayList(it))

            })
        )
    }

    fun refresh() {
        fetchPhotos()
    }

    override fun onCleared() {
        butler.shutDown()
        super.onCleared()
    }
}
