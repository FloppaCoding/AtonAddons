package atonaddons.floppamap.core

import atonaddons.module.impl.render.MapRooms
import java.awt.Color

class Room(x: Int, z: Int, var data: RoomData) : Tile(x, z) {

    var core = 0
    var isSeparator = false

    override val color: Color
        get() = if (this.state == RoomState.UNKNOWN && !visited)
                MapRooms.colorUnexplored.value
            else when (data.type) {
            RoomType.UNKNOWN ->   MapRooms.colorUnexplored.value
            RoomType.BLOOD ->     MapRooms.colorBlood.value
            RoomType.CHAMPION ->  MapRooms.colorMiniboss.value
            RoomType.ENTRANCE ->  MapRooms.colorEntrance.value
            RoomType.FAIRY ->     MapRooms.colorFairy.value
            RoomType.PUZZLE ->    MapRooms.colorPuzzle.value
            RoomType.RARE ->      MapRooms.colorRare.value
            RoomType.TRAP ->      MapRooms.colorTrap.value
            else -> MapRooms.colorRoom.value
        }
}
