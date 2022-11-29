package atonaddons.module.impl.misc

import atonaddons.AtonAddons.Companion.mc
import atonaddons.events.PositionUpdateEvent
import atonaddons.module.Category
import atonaddons.module.Module
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * Module that hold down sprint key for you.
 * @author Aton
 */
object ToggleSprint : Module(
    "Toggle Sprint",
    category = Category.MISC,
    description = "A simple toggle sprint module that toggles sprinting when moving forwards and not collided " +
            "with anything."
) {
    @SubscribeEvent
    fun onPositionUpdate(event: PositionUpdateEvent.Pre) {
        if (!mc.thePlayer.isCollidedHorizontally && mc.thePlayer.moveForward > 0) {
            mc.thePlayer.isSprinting = true
        }
    }
}