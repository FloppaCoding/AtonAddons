package atonaddons.floppamap.dungeon

import atonaddons.AtonAddons.Companion.mc
import atonaddons.floppamap.core.*
import atonaddons.floppamap.utils.MapUtils
import atonaddons.floppamap.utils.MapUtils.calibrated
import atonaddons.floppamap.utils.MapUtils.mapX
import atonaddons.floppamap.utils.MapUtils.mapZ
import atonaddons.floppamap.utils.MapUtils.yaw
import atonaddons.utils.Utils.modMessage
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.util.StringUtils

/**
 * This object provides a collection of methods to update the dungeon information from the map item in the hotbar and tab list.
 *
 * @author Aton, Harry282
 */
object MapUpdate {
    /*
    Colors in the map item:
85:  grey        - Unknown / unexplored room with questionmark in center. (color of the room not the questionmark)
63:  brown;      - Normal room.
30:  green       - Entracne room. and green checks.
18:  red         - Blood room. and failed puzzle X and blood door.
74:  yellow      -  Mini room.
66:  purple      - Puzzle room.
82:  pink        - Fairy room.
0:   blank       - blank fully undiscovered.
119: black       - question marks and wither doors.
34:  white       - used for checkmarks
62:  orange      - trap
     */

    /**
     * Used to only update puzzle names when a new one was revealed.
     */
    private var unmappedPuzz = false

    fun calibrate() {
        MapUtils.roomSize = MapUtils.getRoomSizeFromMap() ?: return
        MapUtils.startCorner = MapUtils.getStartCornerFromMap() ?: return

        MapUtils.coordMultiplier = (MapUtils.roomSize + 4.0) / Dungeon.roomSize

        calibrated = true
        modMessage("Map Calibrated")
    }

    fun preloadHeads() {
        val tabEntries = Dungeon.getDungeonTabList() ?: return
        for (i in listOf(5, 9, 13, 17, 1)) {
            // Accessing the skin locations to load in skin
            tabEntries[i].first.locationSkin
        }
    }

    // Changing this method to not only update the players, but also get missing players
    fun updatePlayers(tabEntries: List<Pair<NetworkPlayerInfo, String>>) {
        var iconNum = 0
        for (i in listOf(5, 9, 13, 17, 1)) {
            val tabText = StringUtils.stripControlCodes(tabEntries[i].second).trim()
            val name = tabText.split(" ").getOrNull(1) ?: ""
            if (name == "") continue
            // if the player is not in the list add it
            var player = Dungeon.dungeonTeammates.find { it.name == name }
            if ((player == null) || player.fakeEntity) {
                Dungeon.dungeonTeammates.remove(player)
                val potPlayer = mc.theWorld.playerEntities.find { it.name == name }
                val fake = potPlayer == null
                (potPlayer ?: EntityOtherPlayerMP(mc.theWorld, tabEntries[i].first.gameProfile))
                    .let {
                        Dungeon.dungeonTeammates.add(DungeonPlayer(it, name).apply {
                            this.fakeEntity = fake
                        })
                    }
            }

            player = Dungeon.dungeonTeammates.find { it.name == name } ?: continue
            player.dead = tabText.contains("(DEAD)")
            if (!player.dead) {
                player.icon = "icon-${iconNum}"
                iconNum++
            } else {
                player.icon = ""
            }
        }

        // Changes here to make player positions and head rotations work before dungeon start
        val decor = MapUtils.getMapData()?.mapDecorations
        Dungeon.dungeonTeammates.forEach { dungeonPlayer ->
            if (dungeonPlayer.player == mc.thePlayer) {
                dungeonPlayer.yaw = dungeonPlayer.player.rotationYawHead
            } else {
                val player = mc.theWorld.playerEntities.find { it.name == dungeonPlayer.name }
                // when the player is in render distance, use that data instead of the map item
                if (player != null) {
                    // check whether the player is in the map; probably not needed
                    if ( player.posX > -200 && player.posX < -10 && player.posZ > -200 && player.posZ < -10) {
                        dungeonPlayer.mapX = (player.posX - Dungeon.startX + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.first - 2
                        dungeonPlayer.mapZ = (player.posZ - Dungeon.startZ + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.second - 2
                        dungeonPlayer.yaw = player.rotationYawHead
                    }
                }else {
                    //if no date from the map item is present go to the next player
                    if (decor == null) return@forEach
                    decor.entries.find { (icon, _) -> icon == dungeonPlayer.icon }?.let { (_, vec4b) ->
                        dungeonPlayer.mapX = vec4b.mapX.toDouble()
                        dungeonPlayer.mapZ = vec4b.mapZ.toDouble()
                        dungeonPlayer.yaw = vec4b.yaw
                    }
                }
            }
        }
    }

    /**
     * Updates the names of revealed puzzles from the tab list.
     */
    fun updatePuzzleNames() {
        val puzzles = Dungeon.uniqueRooms.filter { room ->  room.data.type == RoomType.PUZZLE && room.state.revealed  }
            .sortedBy { room -> room.column*11 + room.row }
        if (RunInformation.puzzles.size == puzzles.size) {
            RunInformation.puzzles.withIndex().forEach { (index, name) -> puzzles[index].data.name = name }
            unmappedPuzz = false
        }
    }

    /**
     * Updates the dungeon info from the hotbar map item.
     * This includes adding newly discovered rooms to teh dungeonList as well as the unique rooms list
     * as well as updating the room states and door states based on check marks and door color.
     */
    fun updateRooms() {
        if (!calibrated) return
        val mapColors = MapUtils.getMapData()?.colors ?: return
        if (mapColors[0].toInt() != 0) return

        val startX = MapUtils.startCorner.first
        val startZ = MapUtils.startCorner.second
        val centerOffset = (MapUtils.roomSize shr 1)
        val increment = (MapUtils.roomSize shr 1) + 2

        for (x in 0..10) {
            for (z in 0..10) {

                var room = Dungeon.dungeonList[z * 11 + x]

                 //If room unknown try to get it from the map item.
                if ((room as? Door)?.type == DoorType.NONE || (room.state == RoomState.UNKNOWN && !room.scanned)) {
                    getRoomFromMap(z, x, mapColors)?.let {
                        Dungeon.dungeonList[z * 11 + x] = it
                        if ((it as? Room)?.data?.type == RoomType.PUZZLE)
                            unmappedPuzz = true
                    }
                }

                room = Dungeon.dungeonList[z * 11 + x]

                // Scan the room centers on the map for check marks.
                val centerX = startX + x * increment + centerOffset
                val centerZ = startZ + z * increment + centerOffset
                if (centerX >= 128 || centerZ >= 128) continue
                room.state = when (mapColors[(centerZ shl 7) + centerX].toInt()) {
                    0 -> RoomState.UNDISCOVERED
                    85 -> if (room is Door)
                        RoomState.DISCOVERED
                    else
                        RoomState.UNDISCOVERED // should not happen
                    119 ->  if (room is Room)
                                RoomState.UNKNOWN
                            else
                                RoomState.DISCOVERED // wither door
                    18 -> if (room is Room) when (room.data.type) {
                        RoomType.BLOOD -> RoomState.DISCOVERED
                        RoomType.PUZZLE -> RoomState.FAILED
                        else -> room.state
                    } else RoomState.DISCOVERED
                    30 -> if (room is Room) when (room.data.type) {
                        RoomType.ENTRANCE -> RoomState.DISCOVERED
                        else -> RoomState.GREEN
                    } else room.state
                    34 -> RoomState.CLEARED
                    else -> {
                        if (room is Door)
                            room.opened = true
                        RoomState.DISCOVERED
                    }
                }
            }
        }
        if (unmappedPuzz)
            updatePuzzleNames()
    }

    /**
     * Gets a dungeon tile from the map item.
     * Also takes care of adding the room to the unique rooms list and combining the data of neighbouring rooms.
     */
    fun getRoomFromMap(row: Int, column: Int, mapColors: ByteArray): Tile? {

        val startX = MapUtils.startCorner.first
        val startZ = MapUtils.startCorner.second
        val increment = (MapUtils.roomSize shr 1) + 2
        val centerOffset = (MapUtils.roomSize shr 1)

        val cornerX = startX + column * increment
        val cornerZ = startZ + row * increment

        val centerX = cornerX + centerOffset
        val centerZ = cornerZ + centerOffset

        if (cornerX >= 128 || cornerZ >= 128) return null
        if (centerX >= 128 || centerZ >= 128) return null

        val xPos = Dungeon.startX + column * (Dungeon.roomSize shr 1)
        val zPos = Dungeon.startZ + row * (Dungeon.roomSize shr 1)

        val rowEven = row and 1 == 0
        val columnEven = column and 1 == 0

        return when {
            rowEven && columnEven -> { // room
                val roomType = when (mapColors[(cornerZ shl 7) + cornerX].toInt()) {
                    0       -> return null
                    18      -> RoomType.BLOOD
                    30      -> RoomType.ENTRANCE
                    85      -> RoomType.UNKNOWN
                    63      -> RoomType.NORMAL
                    74      -> RoomType.CHAMPION
                    66      -> RoomType.PUZZLE
                    82      -> RoomType.FAIRY
                    62      -> RoomType.TRAP
                    else    -> RoomType.NORMAL
                }

                // Connect the tile to neighboring ones.
                // The left most and highest cell will be the one with the check mark, so use that as the main one.
                // left gets prioritized over top.
                val left = if (column > 0) (Dungeon.dungeonList[row * 11 + column-1] as? Room) else null
                val top = if (row > 0) (Dungeon.dungeonList[(row-1) * 11 + column] as? Room) else null
                var isUnique = false
                val data = when {
                    left?.isSeparator == true && top?.isSeparator == true -> { // get the data from the left and update it to the two tiles up top
                        top.data = left.data
                        val topRoom = (Dungeon.dungeonList[(row-2) * 11 + column] as? Room)
                        Dungeon.uniqueRooms.remove(topRoom)
                        topRoom?.data = left.data
                        left.data
                    }
                    left?.isSeparator == true -> left.data
                    top?.isSeparator == true -> top.data
                    else -> { // If no rooms on top or left, then this one will be used as the main cell and added to the unique rooms list
                        isUnique = true
                        RoomData("Unknown$column$row", roomType, 0, 0, listOf(), 0, 0)
                    }
                }
                val room = Room(xPos, zPos, data)
                if (isUnique) {
                    val candidate = Dungeon.uniqueRooms.find { match -> !match.isSeparator && match.data.name == room.data.name }
                    Dungeon.uniqueRooms.remove(candidate)
                    Dungeon.uniqueRooms.add(room)
                }
                room
            }
            !rowEven && !columnEven -> { // possible separator (only for 2x2)
                if(mapColors[(centerZ shl 7) + centerX].toInt() != 0){
                    Dungeon.dungeonList[(row - 1) * 11 + column - 1].let {
                        if (it is Room) {
                            Room(xPos, zPos, it.data).apply { isSeparator = true }
                        } else null
                    }
                }else null
            }
            else -> { // door or separator
                if (mapColors[( (if (rowEven) cornerZ else centerZ) shl 7) + (if (rowEven) centerX else cornerX)].toInt() != 0) { // separator
                    Dungeon.dungeonList[if (rowEven) row * 11 + column - 1 else (row - 1) * 11 + column].let {
                        if (it is Room) {
                            Room(xPos, zPos, it.data).apply { isSeparator = true }
                        } else null
                    }
                } else { // door or nothing
                    val doorType = when(mapColors[(centerZ shl 7) + centerX].toInt()) {
                        0 -> return null
                        119 -> DoorType.WITHER
                        30 -> DoorType.ENTRANCE
                        18 -> DoorType.BLOOD
                        else -> DoorType.NORMAL
                    }
                    Door(xPos, zPos).apply {
                        type = doorType
                    }
                }
            }
        }?.apply { scanned = false }
    }
}