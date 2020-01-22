package co.droidchef.zfotos.feature.gallery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import co.droidchef.zfotos.butler.Butler
import co.droidchef.zfotos.R
import co.droidchef.zfotos.butler.delivery.ImageLoadingStatusListener
import com.facebook.shimmer.ShimmerFrameLayout

class GalleryAdapter(
    private val pictures: ArrayList<Pair<String, Long>>,
    private val gridCellSideSize: Int,
    private val butler: Butler
) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    private val imageViewLayoutParams : FrameLayout.LayoutParams by lazy {
        FrameLayout.LayoutParams(gridCellSideSize, gridCellSideSize)
    }

    fun addPictures(listOfPictures: ArrayList<Pair<String, Long>>) {
        pictures.addAll(listOfPictures)
    }

    override fun getItemCount(): Int = pictures.size

    override fun getItemId(position: Int): Long = pictures[position].second

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.grid_item_preview,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(imageViewLayoutParams, pictures[position])
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        butler.cancelLoad(holder.imageView)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imageView: ImageView = itemView.findViewById(R.id.iv_preview)
        val container: ShimmerFrameLayout = itemView.findViewById(R.id.grid_item_container)

        fun bind(layoutParams: FrameLayout.LayoutParams, picture: Pair<String, Long>) {

            imageView.apply {
                this.layoutParams = layoutParams
            }

            butler.load(picture.first, imageView, R.drawable.loading_placeholder, object :
                ImageLoadingStatusListener {
                override fun onImageLoadingStarted() {
                    container.showShimmer(true)
                }

                override fun onImageLoadingFinished() {
                    container.hideShimmer()
                }

                override fun onImageLoadingFailed() {
                    container.hideShimmer()
                }

                override fun onImageLoadingCanceled() {
                    container.hideShimmer()
                }
            })

        }
    }
}