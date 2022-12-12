package atonaddons.floppamap.dungeon

import atonaddons.AtonAddons.Companion.mc
import atonaddons.floppamap.core.*
import atonaddons.utils.Utils.equalsOneOf
import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import atonaddons.AtonAddons
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation

object DungeonScan {

    private val roomList: Set<RoomConfigData> = try {
        Gson().fromJson(
            mc.resourceManager.getResource(ResourceLocation(AtonAddons.RESOURCE_DOMAIN, "floppamap/rooms.json"))
                .inputStream.bufferedReader(),
            object : TypeToken<Set<RoomConfigData>>() {}.type
        )
    } catch (e: JsonSyntaxException) {
        println("Error parsing FloppaMap room data.")
        setOf()
    } catch (e: JsonIOException) {
        println("Error reading FloppaMap room data.")
        setOf()
    }

    /**
     * Scans the dungeon from the loaded chunks in the world and updates [Dungeon.dungeonList] based on that.
     * When all chunks are loaded [Dungeon.fullyScanned] will be set to true afterwards.
     */
    fun scanDungeon() {
        var allLoaded = true
        var updateConnection = false

        scan@ for (x in 0..10) {
            for (z in 0..10) {
                val xPos = Dungeon.startX + x * (Dungeon.roomSize shr 1)
                val zPos = Dungeon.startZ + z * (Dungeon.roomSize shr 1)

                if (!mc.theWorld.getChunkFromChunkCoords(xPos shr 4, zPos shr 4).isLoaded) {
                    allLoaded = false
                    continue
                }

                if (Dungeon.dungeonList[z * 11 + x]?.scanned == true) continue
                getRoomFromWorld(xPos, zPos, z, x)?.let { newTile ->
                    val oldTile = Dungeon.dungeonList[z * 11 + x]
                    // When the tile is already scanned from the map item make sure to not overwrite it.
                    // Instead just update the values.
                    if (oldTile != null) {
                        /*
                         NOTE: The following check does not account for the case when newTile and oldTile
                          are of different type. This should not happen and when it does the newTile is likely faulty.
                         */
                        if (oldTile is Room && newTile is Room) { // Rooms
                            oldTile.data.configData = newTile.data.configData
                            oldTile.core = newTile.core
                        }else { // Doors
                            oldTile.scanned = true
                        }
                    }else {
                        Dungeon.dungeonList[z * 11 + x] = newTile
                        if (newTile is Room && newTile.data.type == RoomType.NORMAL) updateConnection = true
                    }
                }
            }
        }

        if (updateConnection) {
            MapUpdate.synchConnectedRooms()
        }

        if (allLoaded) {
            Dungeon.fullyScanned = true
        }
    }

    /**
     * Creates a dungeon Tile instance from the World.
     * This is achieved by scanning the blocks in the column specified by [x] and [z].
     * Returns null when the column is air.
     */
    private fun getRoomFromWorld(x: Int, z: Int, row: Int, column: Int): Tile? {
        if (isColumnAir(x, z)) return null
        val rowEven = row and 1 == 0
        val columnEven = column and 1 == 0

        return when {
            rowEven && columnEven -> {
                val core = getCore(x, z)
                getRoomConfigData(core)?.let { configData ->
                    val data = RoomData(configData)

                    Room(x, z, data).apply { this.core = core }
                }
            }
            !rowEven && !columnEven -> {
                Dungeon.dungeonList[(row - 1) * 11 + column - 1]?.let {
                    if (it is Room) {
                        Room(x, z, it.data).apply { isSeparator = true }
                    } else null
                }
            }
            isDoor(x, z) -> {
                val bState = mc.theWorld.getBlockState(BlockPos(x, 69, z))
                val doorType = when {
                    bState.block == Blocks.coal_block -> DoorType.WITHER
                    bState.block == Blocks.monster_egg -> DoorType.ENTRANCE
                    bState.block == Blocks.stained_hardened_clay && Blocks.stained_hardened_clay.getMetaFromState(
                        bState
                    ) == 14 -> DoorType.BLOOD
                    else -> DoorType.NORMAL
                }
                Door(x, z, doorType)
            }
            else -> {
                Dungeon.dungeonList[if (rowEven) row * 11 + column - 1 else (row - 1) * 11 + column]?.let {
                    if (it is Room) {
                        if (it.data.type == RoomType.ENTRANCE) {
                            Door(x, z, DoorType.ENTRANCE)
                        } else {
                            Room(x, z, it.data).apply { isSeparator = true }
                        }
                    } else null
                }
            }
        }
    }

    fun getRoomConfigData(x: Int, z: Int): RoomConfigData? {
        return getRoomConfigData(getCore(x, z))
    }

    private fun getRoomConfigData(hash: Int): RoomConfigData? {
        return roomList.find { hash in it.cores }
    }

    fun getRoomCentre(posX: Int, posZ: Int): Pair<Int, Int> {
        val roomX = (posX - Dungeon.startX) shr 5
        val roomZ = (posZ - Dungeon.startZ) shr 5
        var x = 32 * roomX + Dungeon.startX
        if (x !in posX - 16..posX + 16) x += 32
        var z = 32 * roomZ + Dungeon.startZ
        if (z !in posZ - 16..posZ + 16) z += 32
        return Pair(x, z)
    }

    private fun isColumnAir(x: Int, z: Int): Boolean {
        for (y in 12..140) {
            if (mc.theWorld.getBlockState(BlockPos(x, y, z)).block != Blocks.air) {
                return false
            }
        }
        return true
    }

    private fun isDoor(x: Int, z: Int): Boolean {
        val xPlus4 = isColumnAir(x + 4, z)
        val xMinus4 = isColumnAir(x - 4, z)
        val zPlus4 = isColumnAir(x, z + 4)
        val zMinus4 = isColumnAir(x, z - 4)
        return xPlus4 && xMinus4 && !zPlus4 && !zMinus4 || !xPlus4 && !xMinus4 && zPlus4 && zMinus4
    }

    fun getCore(x: Int, z: Int): Int {
        val blocks = arrayListOf<Int>()
        for (y in 140 downTo 12) {
            val id = Block.getIdFromBlock(mc.theWorld.getBlockState(BlockPos(x, y, z)).block)
            if (!id.equalsOneOf(5, 54)) {
                blocks.add(id)
            }
        }
        return blocks.joinToString("").hashCode()
    }
}
