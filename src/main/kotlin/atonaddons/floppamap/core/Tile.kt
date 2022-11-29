package atonaddons.floppamap.core

import java.awt.Color

abstract class Tile(open var x: Int, open var z: Int) {
    var state = RoomState.UNDISCOVERED
    var visited = false
    var scanned = true
    abstract val color: Color
}
