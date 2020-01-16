package co.droidchef.zfotos.ui.main

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.droidchef.zfotos.R
import co.droidchef.zfotos.data.response.PhotosResponse

class GalleryAdapter(
    private val context: Context,
    private val pictures: ArrayList<PhotosResponse.Result.Picture>,
    private val gridCellSideSize: Int
) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.grid_item_preview,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = pictures.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(gridCellSideSize, position, context, pictures[position])
    }

    override fun getItemId(position: Int): Long {
        return pictures[position].id
    }

    fun addPictures(listOfPictures: ArrayList<PhotosResponse.Result.Picture>) {
        pictures.addAll(listOfPictures)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageView: ImageView = itemView.findViewById(R.id.iv_preview)
        private val textView: TextView = itemView.findViewById(R.id.iv_text)

        fun bind(
            size: Int,
            position: Int,
            context: Context,
            picture: PhotosResponse.Result.Picture
        ) {

            // Adds a simple drawable to verify the recycler performance
            // with the size being pre calculated and set on the image view
            // measure's shouldn't happen for each cell while scroll
            imageView.apply {
                val layoutParamsWithSize = FrameLayout.LayoutParams(size, size)
                layoutParams = layoutParamsWithSize
                setImageDrawable(context.getDrawable(R.drawable.ic_launcher_background))
            }


            // Adds a text overlay to see how the grid items are being drawn so far.
            textView.text = "$position"
            textView.textSize = 36.0f
            textView.setTextColor(Color.WHITE)

            // This is the thumbnail that will be loaded into the view.
            println("Thumbnail to be loaded -> ${picture.thumbnail}")

        }
    }
}