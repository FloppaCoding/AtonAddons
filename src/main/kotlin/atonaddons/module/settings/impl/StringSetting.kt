package atonaddons.module.settings.impl

import atonaddons.module.settings.Setting

class StringSetting(
    name: String,
    var default: String = "",
    var length: Int = 30,
    hidden: Boolean = false,
    description: String? = null,
) : Setting(name, hidden, description) {

    var text: String = default
        set(newStr) {
            field = newStr
            if (newStr.length > length) {
                field = field.substring(0, length - 1)
            }
        }

    override fun reset() {
        text = default
        super.reset()
    }
}