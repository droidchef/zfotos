package co.droidchef.zfotos.feature.gallery

import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.droidchef.zfotos.R
import org.koin.android.viewmodel.ext.android.viewModel

class GalleryFragment : Fragment() {

    companion object {
        const val COLUMNS = 3
        const val VERTICAL_SPACE_HEIGHT = 8
        const val HORIZONTAL_SPACE_WIDTH = VERTICAL_SPACE_HEIGHT
        fun newInstance() = GalleryFragment()
    }

    private val viewModel by viewModel<GalleryViewModel>()

    private val picturesGridRecyclerView: RecyclerView by lazy {
        (rootView.findViewById<RecyclerView>(R.id.rvPicturesGrid)).apply {
            addItemDecoration(
                VerticalSpaceItemDecoration(
                    VERTICAL_SPACE_HEIGHT,
                    HORIZONTAL_SPACE_WIDTH
                )
            )
        }
    }

    private val progressBar: ProgressBar by lazy {
        (rootView.findViewById<ProgressBar>(R.id.progress_circular))
    }

    private lateinit var rootView: View

    private val galleryAdapter: GalleryAdapter by lazy {
        val sideLength = COLUMNS.getGridItemSize()
        GalleryAdapter(
            arrayListOf(),
            sideLength,
            viewModel.butler
        ).apply {
            setHasStableIds(true)
        }
    }

    private fun Int.getGridItemSize(): Int {
        val display = activity!!.windowManager.defaultDisplay
        val point = Point()
        display.getSize(point)
        return point.x / this
    }

    private val layoutManager: GridLayoutManager by lazy {
        GridLayoutManager(
            context,
            COLUMNS
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.main_fragment, container, false)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        picturesGridRecyclerView.layoutManager = layoutManager
        picturesGridRecyclerView.adapter = galleryAdapter

        viewModel.pictures.observe(this, Observer {
            galleryAdapter.addPictures(it)
            galleryAdapter.notifyDataSetChanged()
        })

        viewModel.galleryViewState.observe(this, Observer {

            when (it) {
                GalleryViewModel.GalleryViewState.LOADING -> progressBar.visibility = View.VISIBLE
                GalleryViewModel.GalleryViewState.ERROR -> progressBar.visibility = View.GONE
                GalleryViewModel.GalleryViewState.SUCCESS -> progressBar.visibility = View.GONE
                else -> progressBar.visibility = View.GONE
            }

        })

    }

    inner class VerticalSpaceItemDecoration(
        private val verticalSpaceHeight: Int,
        private val horizontalSpaceWidth: Int
    ) :
        RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.bottom = verticalSpaceHeight
            when ((view.layoutParams as GridLayoutManager.LayoutParams).spanIndex) {
                1 -> outRect.left = horizontalSpaceWidth
                2 -> outRect.left = horizontalSpaceWidth
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.gallery_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_item_refresh -> {
                viewModel.refresh()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
