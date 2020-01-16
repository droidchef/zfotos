package co.droidchef.zfotos.data.response

import java.util.*

data class PhotosResponse(val results: ArrayList<Result>, val info: Info) {

    data class Result(val picture: Picture) {
        data class Picture(
            val large: String,
            val medium: String,
            val thumbnail: String,
            var id: Long
        )
    }

    data class Info(val seed: String, val results: Int, val page: Int)

}