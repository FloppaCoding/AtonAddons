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

    private val roomList: Set<RoomData> = try {
        Gson().fromJson(
            mc.resourceManager.getResource(ResourceLocation(AtonAddons.RESOURCE_DOMAIN, "floppamap/rooms.json"))
                .inputStream.bufferedReader(),
            object : TypeToken<Set<RoomData>>() {}.type
        )
    } catch (e: JsonSyntaxException) {
        println("Error parsing FloppaMap room data.")
        setOf()
    } catch (e: JsonIOException) {
        println("Error reading FloppaMap room data.")
        setOf()
    }

    fun scanDungeon() {
        var allLoaded = true

        scan@ for (x in 0..10) {
            for (z in 0..10) {
                val xPos = Dungeon.startX + x * (Dungeon.roomSize shr 1)
                val zPos = Dungeon.startZ + z * (Dungeon.roomSize shr 1)

                if (!mc.theWorld.getChunkFromChunkCoords(xPos shr 4, zPos shr 4).isLoaded) {
                    allLoaded = false
                    continue
                }
                if (isColumnAir(xPos, zPos)) continue

                if (Dungeon.dungeonList[z * 11 + x]?.scanned == true) continue
                getRoom(xPos, zPos, z, x)?.let {
                    Dungeon.dungeonList[z * 11 + x] = it
                }
            }
        }
        if (allLoaded) {
            Dungeon.fullyScanned = true
        }
    }

    private fun getRoom(x: Int, z: Int, row: Int, column: Int): Tile? {
        val rowEven = row and 1 == 0
        val columnEven = column and 1 == 0

        return when {
            rowEven && columnEven -> {
                getRoomData(x, z)?.let {
                    Room(x, z, it).apply {
                        // Quite inefficient as we are scanning the core twice
                        // Ideally we would save the core from getting room data
                        core = getCore(x, z)


                        val candidate = Dungeon.uniqueRooms.find { match -> match.data.name == data.name }
                        // If room not in list, or a cell further towards south east is in the list replace it with this one
                        if (candidate == null || !candidate.scanned || candidate.x > x || (candidate.z > z && candidate.x == x)) {
                            Dungeon.uniqueRooms.remove(candidate)
                            Dungeon.uniqueRooms.add(this)
                            if (candidate == null) {
                                when (data.type) {
                                    RoomType.PUZZLE -> Dungeon.puzzles.add(data.name)
                                    else -> {}
                                }
                            }
                        }
                        if (candidate != null) {
                            visited = candidate.visited
                        }
                    }
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

    fun getRoomData(x: Int, z: Int): RoomData? {
        return getRoomData(getCore(x, z))
    }

    private fun getRoomData(hash: Int): RoomData? {
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
