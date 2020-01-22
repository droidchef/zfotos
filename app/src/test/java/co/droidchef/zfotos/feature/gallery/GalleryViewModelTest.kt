package co.droidchef.zfotos.feature.gallery

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import co.droidchef.zfotos.butler.Butler
import co.droidchef.zfotos.data.response.PhotosResponse
import co.droidchef.zfotos.networking.service.PhotosService
import co.droidchef.zfotos.utils.TestSchedulersProvider
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import io.reactivex.Observable
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.net.UnknownHostException
import kotlin.random.Random

@RunWith(JUnit4::class)
class GalleryViewModelTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @RelaxedMockK
    private lateinit var photosService: PhotosService

    private lateinit var galleryViewModel: GalleryViewModel

    @RelaxedMockK
    private lateinit var lifecycleOwner: LifecycleOwner

    private lateinit var lifecycle: Lifecycle

    @RelaxedMockK
    private lateinit var butler: Butler

    @RelaxedMockK
    private lateinit var picturesObserver: Observer<ArrayList<Pair<String, Long>>>

    @RelaxedMockK
    private lateinit var galleryViewStateObserver: Observer<GalleryViewModel.GalleryViewState>

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        lifecycle = LifecycleRegistry(lifecycleOwner)
        galleryViewModel = GalleryViewModel(photosService, butler, TestSchedulersProvider())
        galleryViewModel.pictures.observeForever(picturesObserver)
        galleryViewModel.galleryViewState.observeForever(galleryViewStateObserver)

    }

    @Test
    fun testLiveDataInitialization() {

        assertNotNull(galleryViewModel.pictures)
        assertNotNull(galleryViewModel.galleryViewState)

        assertTrue(galleryViewModel.pictures.hasObservers())
        assertTrue(galleryViewModel.galleryViewState.hasObservers())

    }

    @Test
    fun testFetchPhotosSuccess() {

        var num = 0

        val BASE_PORTRAIT_URL = "https://randomuser.me/api/portraits"
        val LARGE_URL_FORMAT = "$BASE_PORTRAIT_URL/women/"
        val MEDIUM_URL_FORMAT = "$BASE_PORTRAIT_URL/med/women/"
        val THUMBNAIL_URL_FORMAT = "$BASE_PORTRAIT_URL/thumb/women/"

        val results = ArrayList<PhotosResponse.Result>()

        for (i in 1..10) {
            num = Random(System.nanoTime()).nextInt(0,1000)
            results.add(
                PhotosResponse.Result(
                    PhotosResponse.Result.Picture(
                        "$LARGE_URL_FORMAT$num.jpg",
                        "$MEDIUM_URL_FORMAT$num.jpg",
                        "$THUMBNAIL_URL_FORMAT$num.jpg",
                        0
                    )
                )
            )
        }

        val photosResponse = PhotosResponse(
            results
            ,PhotosResponse.Info("skahdsahjdsalk", 10, 1)
        )

        every { photosService.getPhotos() } returns Observable.just(photosResponse)

        galleryViewModel.fetchPhotos()

        // Verify the size of the list being passed
        assertEquals(10, galleryViewModel.pictures.value?.size)

        // Verify the logic for setting the ID during the Rx Transformation Chain
        assertEquals(5L, galleryViewModel.pictures.value?.get(5)?.second)

        verify {
            galleryViewStateObserver.onChanged(GalleryViewModel.GalleryViewState.LOADING)
            galleryViewStateObserver.onChanged(GalleryViewModel.GalleryViewState.SUCCESS)
        }
    }


    @Test
    fun testFetchPhotosFailure() {

        every { photosService.getPhotos() } returns Observable.error(UnknownHostException())

        galleryViewModel.fetchPhotos()

        // Verify the size of the list being passed
        assertEquals(0, galleryViewModel.pictures.value?.size)

        verify {
            galleryViewStateObserver.onChanged(GalleryViewModel.GalleryViewState.LOADING)
            galleryViewStateObserver.onChanged(GalleryViewModel.GalleryViewState.ERROR)
        }
    }

}