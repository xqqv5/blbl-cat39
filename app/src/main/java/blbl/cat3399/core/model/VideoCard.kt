package blbl.cat3399.core.model

data class VideoCard(
    val bvid: String,
    val cid: Long?,
    val title: String,
    val coverUrl: String,
    val durationSec: Int,
    val ownerName: String,
    val ownerFace: String?,
    val ownerMid: Long? = null,
    val view: Long?,
    val danmaku: Long?,
    val pubDate: Long?,
    val pubDateText: String?,
)
