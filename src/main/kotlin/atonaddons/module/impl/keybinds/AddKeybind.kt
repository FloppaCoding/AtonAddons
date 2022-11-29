package atonaddons.module.impl.keybinds

import atonaddons.AtonAddons
import atonaddons.module.Category
import atonaddons.module.Module
import atonaddons.module.ModuleManager

object AddKeybind : Module(
    "Add New Key Bind",
    category = Category.KEY_BIND,
    description = "Adds a new key bind you can customize.",
    toggled = true
){
    override fun onEnable() {}

    override fun onDisable() {
        ModuleManager.addNewKeybind()
        toggle()
        AtonAddons.clickGUI.setUpPanels()
    }

    override fun keyBind() {}
}