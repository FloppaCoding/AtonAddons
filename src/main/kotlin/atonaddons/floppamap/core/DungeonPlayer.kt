package atonaddons.floppamap.core

import atonaddons.floppamap.dungeon.Dungeon
import atonaddons.floppamap.utils.MapUtils
import atonaddons.utils.HypixelApiUtils
import net.minecraft.entity.player.EntityPlayer

data class DungeonPlayer(var player: EntityPlayer, var name: String) {
    var mapX = 0.0
    var mapZ = 0.0
    var yaw = 0f
    var icon = ""
    var dead = false
    var deaths = 0

    /**
     * True when the field player is not the correct entity corresponding to this Player.
     */
    var fakeEntity = false

    // Player tracking
    var secretsAtRunStart: Int? = null

    /**
     * Maps the index of the tile in [Dungeon.dungeonList] to the count of ticks the player spent in that Tile.
     * The key -1 is used for time spent dead in clear.
     * @see getCurrentRoomIndex
     */
    val visitedTileTimes: MutableMap<Int, Int> = mutableMapOf()

    /**
     * Fetches the total collected secrets for the player from the API.
     *
     * Only use this method within a coroutine to not freeze the main thread.
     * @return The total secrets collected by this player, or null if no information could be retrieved.
     */
    suspend fun fetchTotalSecretsFromApi(): Int? {
        return HypixelApiUtils.getSecrets(player.uniqueID.toString())
    }

    /**
     * Fetches the total collected secrets for the player from the API and writes it to the field.
     *
     * Only use this method within a coroutine to not freeze the main thread.
     */
    suspend fun fetchAndSetTotalSecrets() {
        secretsAtRunStart = fetchTotalSecretsFromApi()
    }

    /**
     * Returns the room the player is currently in.
     * Does not include boss room.
     * This method is meant to be used to track the position of the dungeon teammates and not the Player.
     *
     * Not to be confused with [Dungeon.getCurrentRoom].
     */
    @JvmName("getCurrentRoomFromCoordinates")
    fun getCurrentRoom(): Room? {
        val index = getCurrentRoomIndex() ?: return null
        val room = Dungeon.dungeonList.getOrNull(index)
        if (room !is Room) return null
        return room
    }

    /**
     * Return the index of the room the player is currently in within the [Dungeon.dungeonList].
     * This index will still point to the correct tile even when the tile is overwritten by the scan.
     *
     * There is no check what kind of tile this is.
     * Will also return an index when no room is loaded in for the tile yet.
     */
    fun getCurrentRoomIndex(): Int? {
        if (Dungeon.inBoss) return null
        // Note the shr 5 ( / 32 ) instead of the usual shr 4 here. This ensures that only rooms can be pointed to.
        // But also means that the x and z values here are half of the column and row.
        val x = (((mapX + 2 - MapUtils.startCorner.first) / MapUtils.coordMultiplier ).toInt() shr 5)
        val z = (((mapZ + 2 - MapUtils.startCorner.second) / MapUtils.coordMultiplier).toInt() shr 5)
        if (x<0 || x > 5 || z < 0 || z > 5) return null
        return x * 2 + z * 22
    }

    /**
     * Has to be run on a by tick basis!
     *
     * Increments the tick count this player spent in the current Tile in [visitedTileTimes].
     * Dead time is counted with index -1.
     */
    fun updateVisitedTileTimes() {
        val index = if (dead) -1 else getCurrentRoomIndex() ?: return
        visitedTileTimes[index] = (visitedTileTimes[index] ?: 0) + 1
    }
}
