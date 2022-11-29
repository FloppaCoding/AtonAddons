package atonaddons.module.impl.render

import atonaddons.AtonAddons
import atonaddons.module.Category
import atonaddons.module.Module
import atonaddons.ui.hud.EditHudGUI

/**
 * Open the edit hid gui.
 * @author Aton
 */
object EditHud : Module(
    "Edit Hud",
    category = Category.RENDER,
    description = "Opens the eidt hud gui."
){

    /**
     * Overridden to prevent the chat message from being sent.
     */
    override fun keyBind() {
        this.toggle()
    }

    /**
     * Automatically disable it again and open the gui
     */
    override fun onEnable() {
        AtonAddons.display = EditHudGUI
        toggle()
        super.onEnable()
    }
}