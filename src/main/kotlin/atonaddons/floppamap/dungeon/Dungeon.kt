package atonaddons.floppamap.dungeon

import atonaddons.AtonAddons
import atonaddons.AtonAddons.Companion.inDungeons
import atonaddons.AtonAddons.Companion.mc
import atonaddons.AtonAddons.Companion.scope
import atonaddons.events.DungeonEndEvent
import atonaddons.events.RoomChangeEvent
import atonaddons.floppamap.core.*
import atonaddons.floppamap.utils.MapUtils
import atonaddons.floppamap.utils.RoomUtils
import atonaddons.module.impl.render.DungeonMap
import atonaddons.utils.TabListUtils
import atonaddons.utils.Utils.currentFloor
import atonaddons.utils.Utils.equalsOneOf
import kotlinx.coroutines.launch
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.util.StringUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.abs

/**
 * In this class everything Dungeon map related is dispatched.
 * It is also used to post various dungeon related events to be used within the Modules.
 *
 * Based on FunnyMap by Harry282 https://github.com/Harry282/FunnyMap
 * @author Aton, Harry282
 */
object Dungeon {

    /*
     TODO rework the way how RoomData or tiles in general are handled.
      Good features of the current system that should be kept are:
       - The RoomData is the same object for connected rooms and can be used to identify connected rooms.
       - The RoomData is used flexibly to update Puzzle names from the tab list.
      Problems that have to be resolved:
       - Currently there is the issue that the Dungeon scan will override the map based scan in MapUpdate.
         This overrides the entire Tile and RoomData
       - To make Puzzle names be changeable RoomData fields have become var instead of val. This is bad because the
         RoomData should be only data retrieved from the config and not changeable.
       - The current system does not allow to store extra variable information for the rooms.
         It could in theory be stored for each Room individually but that would have the issue of it being not synced
         between neighboring Tiles of the same room.
      Possible Solution:
       - Replacing the data Field of type RoomData within the Room Class with a new field of a new Type.
         That new type should have one nullable var field that stores RoomData.
       - All values within RoomData should again be val and not var.
       - The RoomData value in the new class can be null when no data from the scan is available.
       - The new type can contain various variable values that can be changed during the run, such as an identifier,
         current secret count, etc.

     */

    const val roomSize = 32
    const val startX = -185
    const val startZ = -185

    private var lastScanTime: Long = 0
    private var isScanning = false
    var fullyScanned = false

    var hasRunStarted = false
    var inBoss = false
    // 6 x 6 room grid, 11 x 11 with connections
    val dungeonList = Array<Tile?>(121) { null }

    /**
     * Contains all the teammates in the current dungeon.
     * Also contains the Player.
     */
    val dungeonTeammates = mutableListOf<DungeonPlayer>()

    var witherDoors = 0

    private val deathPattern = Regex("^ ☠ (?<name>\\w+) .+ and became a ghost")

    /**
     * Contains the current room. Updated every tick.
     */
    var currentRoom: Room? = null

    private val entryMessages = listOf(
        "[BOSS] Bonzo: Gratz for making it this far, but I’m basically unbeatable.",
        "[BOSS] Scarf: This is where the journey ends for you, Adventurers.",
        "[BOSS] The Professor: I was burdened with terrible news recently...",
        "[BOSS] Thorn: Welcome Adventurers! I am Thorn, the Spirit! And host of the Vegan Trials!",
        "[BOSS] Livid: Welcome, you arrive right on time. I am Livid, the Master of Shadows.",
        "[BOSS] Sadan: So you made it all the way here... Now you wish to defy me? Sadan?!",
        "[BOSS] Maxor: WELL WELL WELL LOOK WHO’S HERE!"
    )

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !inDungeons) return
        if (shouldScan()) {
            lastScanTime = System.currentTimeMillis()
            isScanning = true
            DungeonScan.scanDungeon()
            isScanning = false
        }
        val newRoom = getCurrentRoom()
        if (newRoom != currentRoom) {
            MinecraftForge.EVENT_BUS.post(RoomChangeEvent(newRoom, currentRoom))
            currentRoom = newRoom
        }

        scope.launch {
            getDungeonTabList()?.let {
                MapUpdate.updatePlayers(it)
                RunInformation.updateRunInformation(it)
            }
            MapUpdate.updateRooms()
        }

        // added check to determine whether in boss based on coordinates. This is relevant when blood is being skipped.
        // this also makes the chat message based detection obsolete
        if (AtonAddons.tickRamp % 20 == 0) {
            when ( currentFloor ) {
                1 -> inBoss = mc.thePlayer.posX > -71 && mc.thePlayer.posZ > -39
                2,3,4 -> inBoss = mc.thePlayer.posX > -39 && mc.thePlayer.posZ > -39
                5,6 -> inBoss = mc.thePlayer.posX > -39 && mc.thePlayer.posZ > -7
                7 -> inBoss = mc.thePlayer.posX > -7 && mc.thePlayer.posZ > -7
            }
            if (hasRunStarted && !MapUtils.calibrated) MapUpdate.calibrate()
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!inDungeons || event.type.toInt() == 2) return
        val text = StringUtils.stripControlCodes(event.message.unformattedText)
        when {
            text.equalsOneOf(
                "Dungeon starts in 4 seconds.", "Dungeon starts in 4 seconds. Get ready!"
            ) -> MapUpdate.preloadHeads()

            text == "[NPC] Mort: Here, I found this map when I first entered the dungeon." -> {
                MapUpdate.calibrate()
                hasRunStarted = true
            }
            entryMessages.any { it == text } -> inBoss = true
            text == "                             > EXTRA STATS <" -> {
                MinecraftForge.EVENT_BUS.post(DungeonEndEvent())
            }
            text.contains("☠") -> {
                val matcher = deathPattern.find(text)
                val deadName = matcher?.groups?.get("name")?.value
                dungeonTeammates.find {
                    if (deadName.equals("you", true)) it.name == mc.thePlayer.name else it.name == deadName
                }?.apply{ deaths++ }
            }
        }
    }

    /**
     * Update visited rooms when room is changed.
     */
    @SubscribeEvent
    fun onRoomChange(event: RoomChangeEvent) {
        if (event.newRoom == null) return
        if (event.newRoom.data.type == RoomType.BOSS || event.newRoom.data.type == RoomType.REGION) return
        // Update all of the connected rooms and separators to visited.
        dungeonList.forEach {
            if ((it is Room) && it.data.type != RoomType.UNKNOWN && it.data === event.newRoom.data){
                it.visited = true
            }
        }

        // Reveal door connecting the two rooms.
        if (event.oldRoom == null) return
        val doorRow = if (abs(event.newRoom.row - event.oldRoom.row) <= 2)
                (event.newRoom.row + event.oldRoom.row) shr 1
            else return
        val doorColumn = if (abs(event.newRoom.column - event.oldRoom.column) <= 2)
                (event.newRoom.column + event.oldRoom.column) shr 1
            else return
        if (doorRow < 0 || doorRow > 11 || doorColumn < 0 || doorColumn < 11) return
        (dungeonList[doorRow*11 + doorColumn] as? Door)?.let { door ->
            door.visited = true
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Unload) {
        reset()
        MapUtils.calibrated = false
        hasRunStarted = false
        inBoss = false
        fullyScanned = false
    }

    private fun shouldScan() =
        DungeonMap.autoScan.enabled && DungeonMap.enabled && !isScanning && !fullyScanned && !inBoss && currentFloor != null

    fun getDungeonTabList(): List<Pair<NetworkPlayerInfo, String>>? {
        val tabEntries = TabListUtils.tabList
        if (tabEntries.size < 18 || !tabEntries[0].second.contains("§r§b§lParty §r§f(")) {
            return null
        }
        return tabEntries
    }

    /**
     * Returns the room the player is currently in.
     * Includes boss room.
     */
    @JvmName("getCurrentRoomFromCoordinates")
    fun getCurrentRoom(): Room? {
        val room = if (inBoss) {
            val floor = currentFloor
            if (floor != null) {
                RoomUtils.instanceBossRoom(floor)
            }else {
                null
            }
        }else {
            val x = ((mc.thePlayer.posX - startX + 15).toInt() shr 5)
            val z = ((mc.thePlayer.posZ - startZ + 15).toInt() shr 5)
            dungeonList.getOrNull(x * 2 + z * 22)
        }
        if (room !is Room) return null
        return room
    }

    /**
     * Rests most of the dungeon properties. Other properties which are not reset here are reset in onWorlLoad.
     * @see onWorldLoad
     */
    fun reset() {
        currentRoom = null

        dungeonTeammates.clear()

        dungeonList.fill(null)

        witherDoors = 0
        RunInformation.reset()
    }
}
