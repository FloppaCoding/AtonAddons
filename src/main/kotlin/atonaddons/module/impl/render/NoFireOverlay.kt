package atonaddons.module.impl.render

import atonaddons.AtonAddons.Companion.mc
import atonaddons.module.Category
import atonaddons.module.Module

/**
 * Prevents the fire overlay from rendering when burning.
 * @author Aton
 */
object NoFireOverlay : Module(
    "No Fire Overlay",
    category = Category.MISC,
    description = "Removes the fire overlay when burning completely."
) {
    /**
     * Referenced by the ItemRenderer Mixin to determine whether the fire overlay should be rendered.
     */
    fun shouldDisplayBurnOverlayHook(): Boolean =
        if (this.enabled)  false else mc.thePlayer.isBurning
}