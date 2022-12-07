package atonaddons.module.impl.dungeon

import atonaddons.AtonAddons.Companion.inDungeons
import atonaddons.AtonAddons.Companion.scope
import atonaddons.events.DungeonEndEvent
import atonaddons.events.DungeonRoomStateChangeEvent
import atonaddons.events.DungeonTeammateAddEvent
import atonaddons.floppamap.core.*
import atonaddons.floppamap.dungeon.Dungeon
import atonaddons.floppamap.dungeon.MapUpdate
import atonaddons.module.Category
import atonaddons.module.Module
import atonaddons.utils.ChatUtils
import atonaddons.utils.TabListUtils
import kotlinx.coroutines.*
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.IChatComponent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object PartyTracker : Module(
    "Party Tracker",
    category = Category.DUNGEON,
    description = "Tracks cleared rooms and secrets gotten by party members and displays the stats at the end of the run."
) {
    /**
     * Used to keep track of which players where in which rooms when they were cleared.
     */
    private val roomClearers: MutableMap<RoomData, List<DungeonPlayer>> = mutableMapOf()

    private val unidentifiedRoomData = RoomData("Unidentified", RoomType.UNKNOWN, 0, 0, listOf(), 0, 0)

    /**
     * Get the total secrets a player has collected from the Hypixel API when the player is added to the list of dungeon
     * teammates and save that value.
     * @see MapUpdate.updatePlayers
     */
    @SubscribeEvent
    fun onTeammateAdd(event: DungeonTeammateAddEvent){
        scope.launch(Dispatchers.IO) { event.dungeonPlayer.fetchAndSetTotalSecrets() }
    }

    /**
     * Update the visited Tiles.
     */
    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        if (!inDungeons) return
        val iterator = Dungeon.dungeonTeammates.iterator()
        try {
            while (iterator.hasNext()) {
                iterator.next().updateVisitedTileTimes()
            }
        }catch (_ : ConcurrentModificationException) {}
    }

    /**
     * Keep track of the rooms a player has cleared.
     */
    @SubscribeEvent
    fun onRoomStateChange(event: DungeonRoomStateChangeEvent) {
        // Filter only those state changes, where the room goes to white check (rooms with missing secrets cleared)
        // or directly to green check without being white checked first (rooms no missing secrets cleared).
        if (event.tile !is Room) return
        if (event.newState == RoomState.CLEARED
            || event.newState == RoomState.GREEN && event.tile.state != RoomState.CLEARED) {
            val playersInRoom = Dungeon.dungeonTeammates.filter { teamMate ->
                !teamMate.dead && teamMate.getCurrentRoom()?.data == event.tile.data
            }
            // also add when the list is empty. This might still be useful information.
            roomClearers[event.tile.data] = playersInRoom
        }
    }

    /**
     * Display the overview of what party members have done when the dungeon ends.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onDungeonEnd(event: DungeonEndEvent){
        scope.launch {
            // fetch the collected secrets parallelized
            val jobMap = mutableMapOf<DungeonPlayer, Deferred<String>>()
            Dungeon.dungeonTeammates.forEach { teammate ->
                val job: Deferred<String> = async {
                    val currentSecrets = teammate.fetchTotalSecretsFromApi()
                    if (teammate.secretsAtRunStart != null && currentSecrets != null) {
                        "${currentSecrets - teammate.secretsAtRunStart!!}"
                    } else "Unknown"
                }
                jobMap.put(teammate, job)
            }


            jobMap.forEach teamMates@{ (teammate, secretsJob) ->
                val clearedRoomsComponent = getClearedRoomsComponent(teammate)
                val compltedPuzzlesComponent = getCompletedPuzzlesComponent(teammate)

                val trapText = if (roomClearers.any { (roomData, playerList) ->
                        roomData.type == RoomType.TRAP && playerList.contains(teammate)
                }) ", did trap " else ""
                val trapComponent: IChatComponent = ChatComponentText(trapText)

                val secretComponent: IChatComponent = ChatComponentText(
                    "and collected ${secretsJob.await()} Secrets. "
                )
                val visitedRoomsComponent = getRoomTimesComponent(teammate)


                val message: IChatComponent = ChatComponentText("${coloredPlayerName(teammate)} ")
                    .appendSibling(clearedRoomsComponent)
                    .appendSibling(compltedPuzzlesComponent)
                    .appendSibling(trapComponent)
                    .appendSibling(secretComponent)
                    .appendSibling(visitedRoomsComponent)

                ChatUtils.modMessage(message)
            } // ^ teammates loop

        }
    }

    /**
     * Reset when changing server.
     */
    @SubscribeEvent
    fun onWarp(event: WorldEvent.Unload) {
        roomClearers.clear()
    }

    /**
     * Gets the formatting Code for the Players name color from the tab list.
     */
    private fun playerRankColor(teammate: DungeonPlayer): String{
        /** This pattern captures the formatting code for the player name color. */
        val pattern = Regex("(?i) §r§(?<color>[0-9a-f])${teammate.name} §r")
        TabListUtils.tabList.forEach{
            val matcher = pattern.find(it.second) ?: return@forEach
            return matcher.groups["color"]?.value ?: "f"
        }
        return "f"
    }

    /**
     * Returns the players names with the according color code from the tab list.
     */
    private fun coloredPlayerName(teammate: DungeonPlayer): String{
        return "§${playerRankColor(teammate)}${teammate.name}§r"
    }

    /**
     * Returns the text formatting for the givern room types
     */
    private fun roomTextFormatting(roomType: RoomType): EnumChatFormatting {
        return when(roomType) {
            RoomType.UNKNOWN -> EnumChatFormatting.DARK_GRAY
            RoomType.BLOOD -> EnumChatFormatting.DARK_RED
            RoomType.CHAMPION -> EnumChatFormatting.YELLOW
            RoomType.PUZZLE -> EnumChatFormatting.DARK_PURPLE
            RoomType.FAIRY -> EnumChatFormatting.LIGHT_PURPLE
            RoomType.ENTRANCE -> EnumChatFormatting.GREEN
            RoomType.TRAP -> EnumChatFormatting.GOLD
            else -> EnumChatFormatting.GRAY
        }
    }

    /**
     * Returns the name of the room with color depending on the room type.
     */
    private fun coloredRoomName(roomData: RoomData): String {
        val roomName = if (roomData.name.startsWith("Unknown")){
            when(roomData.type) {
                RoomType.TRAP -> "Trap"
                RoomType.BLOOD -> "Blood"
                RoomType.CHAMPION -> "Champion"
                RoomType.FAIRY -> "Fairy"
                RoomType.ENTRANCE -> "Entrance"
                else -> roomData.name
            }
        }else roomData.name
        return "${roomTextFormatting(roomData.type)}$roomName§r"
    }

    /**
     * Returns the hoverable ChatComponent for the room times of this player.
     */
    private fun getRoomTimesComponent(teammate: DungeonPlayer): IChatComponent {
        // remap the time spent per tile to the time spent per room (consisting of multiple tiles)
        var ticksDead = 0
        val visitedMap: MutableMap<RoomData, Int> = mutableMapOf()
        teammate.visitedTileTimes.forEach visited@{ (tileIndex, ticks) ->
            if (tileIndex == -1) { // Index -1 is used for time spent dead
                ticksDead = ticks
                return@visited
            }
            val roomData = (Dungeon.dungeonList[tileIndex] as? Room)?.data ?: unidentifiedRoomData
            visitedMap[roomData] = (visitedMap[roomData] ?: 0) + ticks
        }

        // turn the visited rooms map into a list sorted by the time spent in the room (descending)
        val visitedList = visitedMap.toList().sortedBy { -it.second }
        // create the room times hover text
        val hoverTextBuilder = StringBuilder()
        hoverTextBuilder.append("§l${coloredPlayerName(teammate)}'s Room Times [s]:§r")
        visitedList.forEach { (roomData, ticks) ->
            hoverTextBuilder.append(
                "\n${coloredRoomName(roomData)}: ${ticks / 20.0}§r"
            )
        }
        if (ticksDead > 0) {
            hoverTextBuilder.append("\n${EnumChatFormatting.RED}Dead§r: ${ticksDead / 20.0}§r")
        }

        return ChatUtils.createHoverableText(
            "§l§6[Hover for room times] §r",
            hoverTextBuilder.toString()
        )
    }

    /**
     * Returns the hoverable ChatComponent for the cleared rooms of this Player.
     */
    private fun getClearedRoomsComponent(teammate: DungeonPlayer): IChatComponent{
        val clearedRooms = roomClearers.filter { (roomData, playerList) ->
            playerList.contains(teammate)
                    && (roomData.type == RoomType.NORMAL || roomData.type == RoomType.RARE
                    || roomData.type == RoomType.CHAMPION || roomData.type == RoomType.BLOOD)
        }
        /** Number of rooms the player was in ALONE when they got cleared. */
        val minClearedRooms = clearedRooms.filter { (_, playerList) ->
            playerList.none { it.name != teammate.name }
        }.size
        /** Number of rooms the player was in when they got cleared. */
        val maxClearedRooms = clearedRooms.size

        val hoverTextBuilder = StringBuilder()
        hoverTextBuilder.append("§l${coloredPlayerName(teammate)}'s Cleared Rooms:§r")
        clearedRooms.forEach { (roomData, playerList) ->
            val additionalInfo = if (playerList.size > 1)
                playerList.filter { it.name != teammate.name }.joinToString(", ", "§r with: "){ coloredPlayerName(it) }
            else
                ""
            hoverTextBuilder.append(
                "\n${coloredRoomName(roomData)}$additionalInfo§r"
            )
        }
        val roomNumber = if (minClearedRooms == 0 && maxClearedRooms == 0) "no"
        else if (minClearedRooms == maxClearedRooms) "$minClearedRooms"
        else "$minClearedRooms-$maxClearedRooms"
        return ChatUtils.createHoverableText(
            "cleared $roomNumber Room${if (roomNumber == "1") "" else "s"}, ",
            hoverTextBuilder.toString()
        )
    }

    /**
     * Returns the hoeverable ChatComponent for the completed Puzzles of this Player.
     */
    private fun getCompletedPuzzlesComponent(teammate: DungeonPlayer): IChatComponent{
        //TODO Use Tab list instead.
        val completedPuzzles = roomClearers.filter { (roomData, playerList) ->
            playerList.contains(teammate)
                    && roomData.type == RoomType.PUZZLE
        }
        /** Number of puzzle rooms the player was in ALONE when they got cleared. */
        val minCompletedPuzzles = completedPuzzles.filter { (_, playerList) ->
            playerList.none { it.name != teammate.name }
        }.size
        /** Number of puzzle rooms the player was in when they got cleared. */
        val maxCompletedPuzzles = completedPuzzles.size

        val hoverTextBuilder = StringBuilder()
        hoverTextBuilder.append("§l${coloredPlayerName(teammate)}'s Completed Puzzles:§r")
        completedPuzzles.forEach { (roomData, playerList) ->
            val additionalInfo = if (playerList.size > 1)
                playerList.filter { it.name != teammate.name }.joinToString(", ", "§r with: "){ coloredPlayerName(it) }
            else
                ""
            hoverTextBuilder.append(
                "\n${coloredRoomName(roomData)}$additionalInfo§r"
            )
        }
        val puzzleNumber = if (minCompletedPuzzles == 0 && maxCompletedPuzzles == 0) "no"
        else if (minCompletedPuzzles == maxCompletedPuzzles) "$minCompletedPuzzles"
        else "$minCompletedPuzzles-$maxCompletedPuzzles"
        return ChatUtils.createHoverableText(
            "completed $puzzleNumber Puzzle${if (puzzleNumber == "1") "" else "s"} ",
            hoverTextBuilder.toString()
        )
    }
}