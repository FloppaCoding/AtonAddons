package atonaddons.module.settings.impl

import atonaddons.module.settings.Setting

class ActionSetting(
    name: String,
    hidden: Boolean = false,
    description: String? = null,
) : Setting(name, hidden, description) {

    var action: () -> Unit = {}

    fun doAction() {
        action()
    }
}