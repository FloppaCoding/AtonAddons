package atonaddons.module.impl.render

import atonaddons.AtonAddons.Companion.inDungeons
import atonaddons.floppamap.core.Door
import atonaddons.floppamap.core.DoorType
import atonaddons.floppamap.core.RoomState
import atonaddons.floppamap.dungeon.Dungeon
import atonaddons.module.Category
import atonaddons.module.Module
import atonaddons.module.settings.impl.ColorSetting
import atonaddons.utils.WorldRenderUtils
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.awt.Color

object DoorESP : Module(
    "Door ESP",
    category = Category.RENDER,
    description = "Renders an outline for Dungeon Wither Doors."
){
    private val color = ColorSetting("Color", Color(255,0,0 ), false, description = "Color of the outline.")

    init {
        this.addSettings(
            color
        )
    }

    private var doors = listOf<Door>()

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        if (!inDungeons) return
        doors = Dungeon.getDungeonTileList<Door>().filter {
            (it.type == DoorType.WITHER || it.type == DoorType.BLOOD) && !it.opened
                && (it.state == RoomState.DISCOVERED || it.visited)
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!inDungeons) return
        doors.forEach {
            WorldRenderUtils.drawCustomSizedBoxAt(it.x-1.0, 69.0, it.z - 1.0, 3.0, color.value)
        }
    }
}