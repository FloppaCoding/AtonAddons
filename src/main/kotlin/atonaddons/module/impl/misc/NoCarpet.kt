package atonaddons.module.impl.misc

import atonaddons.AtonAddons.Companion.inSkyblock
import atonaddons.module.Category
import atonaddons.module.Module

/**
 * Flattens carpet hitboxes to prevent lagback.
 * @author Aton
 */
object NoCarpet : Module(
    "No Carpet",
    category = Category.MISC,
    description = "Removes carpet hitboxes to prevent lagback."
) {
    /**
     * Referenced by the CarpetMixin to determine whether the bounding box should be set to 0.
     */
    fun ignoreCarpet(): Boolean {
        return this.enabled && inSkyblock
    }
}