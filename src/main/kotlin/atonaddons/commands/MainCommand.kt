package atonaddons.commands

import atonaddons.AtonAddons
import atonaddons.AtonAddons.Companion.clickGUI
import atonaddons.AtonAddons.Companion.display
import atonaddons.AtonAddons.Companion.mc
import atonaddons.AtonAddons.Companion.scope
import atonaddons.floppamap.core.Room
import atonaddons.floppamap.core.RoomConfigData
import atonaddons.floppamap.dungeon.Dungeon
import atonaddons.floppamap.dungeon.DungeonScan
import atonaddons.module.impl.render.ClickGui
import atonaddons.utils.ChatUtils.chatMessage
import atonaddons.utils.ChatUtils.modMessage
import atonaddons.utils.TabListUtils
import atonaddons.utils.HypixelApiUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.client.gui.GuiScreen
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos


object MainCommand : CommandBase() {
    override fun getCommandName(): String {
        return "atonaddons"
    }

    override fun getCommandAliases(): List<String> {
        return listOf(
            "aa"
        )
    }

    override fun getCommandUsage(sender: ICommandSender): String {
        return "/$commandName"
    }

    override fun getRequiredPermissionLevel(): Int {
        return 0
    }

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (args.isEmpty()) {
            display = clickGUI
            return
        }
        when (args[0].lowercase()) {
            "gui"           -> display = clickGUI
            "scan"          -> DungeonScan.scanDungeon()
            "roomdata"      -> DungeonScan.getRoomCentre(mc.thePlayer.posX.toInt(), mc.thePlayer.posZ.toInt()).let {
                DungeonScan.getRoomConfigData(it.first, it.second) ?: DungeonScan.getCore(it.first, it.second)
            }.run {
                GuiScreen.setClipboardString(this.toString())
                modMessage(
                    if (this is RoomConfigData) "Copied room data to clipboard."
                    else "Existing room data not found. Copied room core to clipboard."
                )
            }
            "reload"        -> {
                modMessage("Reloading config files.")
                AtonAddons.moduleConfig.loadConfig()
                clickGUI.setUpPanels()
            }
            "resetgui"      -> {
                modMessage("Resetting positions in the click gui.")
                ClickGui.resetPositions()
            }
            "dungeontablist" -> {
                Dungeon.getDungeonTabList()?.let { tablist ->
                    tablist.forEach { chatMessage(it.second) }
                }
            }
            "tablist"       -> {
                TabListUtils.tabList.forEach { chatMessage(it.second) }
            }
            "secrets" -> scope.launch(Dispatchers.IO) {
                val uuid = if (args.size > 1 ) {
                    args[1]
                }else {
                    mc.thePlayer.uniqueID.toString()
                }
                val secrets = HypixelApiUtils.getSecrets(uuid)
                modMessage("$secrets")
            }
            "test" -> {
                if (args.size <= 1 ) return
                val name = args[1]
                val tiles = Dungeon.getDungeonTileList<Room>()
                    .filter {it.data.name.contains(name) }
                val uniques = tiles.filter { it.isUnique }
                modMessage("${uniques.size} uniques out of ${tiles.size} total")
                tiles.forEach {
                    val same = it.data === uniques.firstOrNull()?.data
                    modMessage("$same, ${it.data.currentSecrets}, ${it.x}/${it.z}")
                }


            }
            else            -> {
                modMessage("Command not recognized!")
            }
        }
    }

    override fun addTabCompletionOptions(
        sender: ICommandSender,
        args: Array<String>,
        pos: BlockPos
    ): MutableList<String> {
        if (args.size == 1) {
            return getListOfStringsMatchingLastWord(
                args,
                mutableListOf("gui", "scan", "roomdata", "reload" , "resetgui" , "tablist")
            )
        }
        return mutableListOf()
    }
}
