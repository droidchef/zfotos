package co.droidchef.zfotos.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import co.droidchef.zfotos.R
import co.droidchef.zfotos.butler.Butler
import co.droidchef.zfotos.data.response.PhotosResponse

class GalleryAdapter(
    private val pictures: ArrayList<PhotosResponse.Result.Picture>,
    private val gridCellSideSize: Int
) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    private val imageViewLayoutParams : FrameLayout.LayoutParams by lazy {
        FrameLayout.LayoutParams(gridCellSideSize, gridCellSideSize)
    }

    fun addPictures(listOfPictures: ArrayList<PhotosResponse.Result.Picture>) {
        pictures.addAll(listOfPictures)
    }

    override fun getItemCount(): Int = pictures.size

    override fun getItemId(position: Int): Long = pictures[position].id

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
        Butler.cancelLoad(holder.imageView)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imageView: ImageView = itemView.findViewById(R.id.iv_preview)

        fun bind(layoutParams: FrameLayout.LayoutParams, picture: PhotosResponse.Result.Picture) {

            imageView.apply {
                this.layoutParams = layoutParams
                tag = picture.id
            }

            Butler.load(picture.large, imageView, R.drawable.ic_launcher_background)

        }
    }
}