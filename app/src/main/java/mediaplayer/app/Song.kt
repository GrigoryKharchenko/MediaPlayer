package mediaplayer.app

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes

data class Song(
    val nameGroup: String,
    val nameSong: String,
    @RawRes
    val song: Int,
    @DrawableRes
    val albumCover: Int
)
