package atonaddons.floppamap.utils

import atonaddons.floppamap.core.Room
import atonaddons.floppamap.core.RoomData
import atonaddons.floppamap.core.RoomType

object RoomUtils {
    fun instanceBossRoom(floor: Int): Room {
        return Room(0,0, RoomData("Boss $floor", RoomType.BOSS))
    }

    fun instanceRegionRoom(region: String): Room {
        return Room(0,0, RoomData(region, RoomType.REGION))
    }
}