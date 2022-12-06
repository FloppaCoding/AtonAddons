package atonaddons.module.impl.dungeon

import atonaddons.AtonAddons.Companion.inDungeons
import atonaddons.events.DungeonEndEvent
import atonaddons.floppamap.dungeon.Dungeon
import atonaddons.module.Category
import atonaddons.module.Module
import atonaddons.utils.ChatUtils
import net.minecraft.util.StringUtils.stripControlCodes
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * Show extra stats at the end of a dungeon run.
 * @author Aton
 */
object ExtraStats : Module(
    "Extra Stats",
    category = Category.DUNGEON,
    description = "Automatically clicks > EXTRA STATS < at the end of a run."
){
    /**
     * Triggers on the [DungeonEndEvent] fired in [Dungeon.onChat] when the
     * "                             > EXTRA STATS <" chat message is received.
     * @see Dungeon.onChat
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if ( !inDungeons || event.type.toInt() == 2) return
        when (stripControlCodes(event.message.unformattedText)) {
            "                             > EXTRA STATS <" -> {
                ChatUtils.command("showextrastats")
                return
            }
        }
    }
}

//"§r                             §6> §e§lEXTRA STATS §6<"
//"                             > EXTRA STATS <"

/*
@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onDungeonEnd(event: DungeonEndEvent) {
        ChatUtils.command("showextrastats", false)
        return
    }
 */