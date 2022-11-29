package atonaddons.floppamap.core

data class RoomData(
    var name: String,
    var type: RoomType,
    var secrets: Int,
    var size: Int,
    var cores: List<Int>,
    var crypts: Int,
    var trappedChests: Int
)
