package atonaddons.floppamap.utils

import atonaddons.floppamap.core.Room
import atonaddons.floppamap.core.RoomData
import atonaddons.floppamap.core.RoomType

object RoomUtils {
    fun instanceBossRoom(floor: Int): Room {
        return Room(0,0, RoomData("Boss $floor", RoomType.BOSS, 0, 1, listOf(0), 0,0))
    }

    fun instanceRegionRoom(region: String): Room {
        return Room(0,0, RoomData(region, RoomType.REGION, 0, 1, listOf(0), 0,0))
    }
}