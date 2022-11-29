package atonaddons.module.impl.keybinds

import atonaddons.module.Category
import atonaddons.module.Module
import atonaddons.module.ModuleManager
import atonaddons.module.settings.impl.ActionSetting
import atonaddons.module.settings.impl.BooleanSetting
import atonaddons.module.settings.impl.SelectorSetting
import atonaddons.module.settings.impl.StringSetting
import atonaddons.utils.Utils

class KeyBind(name: String) : Module(name, category = Category.KEY_BIND){
    private val modes = arrayListOf("Command","Chat message")

    val bindName = StringSetting("Name",this.name)
    private val mode = SelectorSetting("Mode", modes[0], modes, description = "Action performed by the keybind. Use Command for client side commands and Chat Message for server side commands.\nE.g. to open the Storage select Chat Message and type /storage in the action field.")
    private val action = StringSetting("Action","",50, description = "Name of the command to be executed or chat message to be sent.")
    private val removeButton = ActionSetting("Remove Key Bind", description = "Removes the Key Bind.").apply {
        action = {
            ModuleManager.removeKeyBind(this@KeyBind)
        }
    }
    // Used by the config loader to determine whether a setting is a keybind
    private val flag = BooleanSetting("THIS_IS_A_KEY_BIND", hidden = true)

    init {
        this.addSettings(
            bindName,
            mode,
            action,
            removeButton,
            flag
        )
    }

    override fun keyBind() {
        if (!this.enabled) return
        performAction()
    }

    private fun performAction(){
        when(mode.selected){
            "Command" -> {
                Utils.command(action.text, true)
            }
            "Chat message" -> {
                Utils.sendChat(action.text)
            }
        }
    }
}