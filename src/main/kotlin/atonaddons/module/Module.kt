package atonaddons.module

import atonaddons.AtonAddons
import atonaddons.module.settings.Setting
import atonaddons.utils.ChatUtils
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import net.minecraftforge.common.MinecraftForge

open class Module(
    name: String,
    keyCode: Int = 0,
    category: Category = Category.MISC,
    toggled: Boolean = false,
    settings: ArrayList<Setting> = ArrayList(),
    description: String = ""
){

    @Expose
    @SerializedName("name")
    val name: String

    /**
     * Key code of the corresponding key bind.
     * Mouse binds will be negative: -100 + mouse button.
     * This is the same way as minecraft treats mouse binds.
     */
    @Expose
    @SerializedName("key")
    var keyCode: Int
    val category: Category

    /**
     * Dont set this value directly, instead use toggle()
     */
    @Expose
    @SerializedName("enabled")
    var enabled: Boolean = toggled
        private set
    @Expose
    @SerializedName("settings")
    val settings: ArrayList<Setting>

    /**
     * Will be used for an advanced info gui
     */
    var description: String

    init {
        this.name = name
        this.keyCode = keyCode
        this.category = category
        this.settings = settings
        this.description = description
    }

    open fun onEnable() {
        MinecraftForge.EVENT_BUS.register(this)
    }
    open fun onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this)
    }

    /**
     * Call to perform the key bind action for this module.
     * By default, this will toggle the module and send a chat message.
     * It can be overwritten in the module to change that behaviour.
     */
    open fun keyBind() {
        this.toggle()
        ChatUtils.modMessage("$name ${if (enabled) "§aenabled" else "§cdisabled"}.")
    }

    /**
     * Will toggle the module
     */
    fun toggle() {
        enabled = !enabled
        if (enabled)
            onEnable()
        else
            onDisable()
    }

    /**
     * Adds all settings in the input to the settings field of the module.
     * This is required for saving and loading these settings to / from a file.
     * Keep in mind, that these settings are passed by reference, which will get lost if the original setting is reassigned.
     */
    fun addSettings(setArray: ArrayList<Setting>) {
        setArray.forEach {
            settings.add(it)
        }
    }

    /**
     * Adds all settings in the input to the settings field of the module.
     * This is required for saving and loading these settings to / from a file.
     * Keep in mind, that these settings are passed by reference, which will get lost if the original setting is reassigned.
     */
    fun addSettings(vararg setArray: Setting) {
        this.addSettings(ArrayList(setArray.asList()))
    }

    fun getSettingByName(name: String): Setting? {
        for (set in settings) {
            if (set.name.equals(name, ignoreCase = true)) {
                return set
            }
        }
        System.err.println("[" + AtonAddons.MOD_NAME + "] Error Setting NOT found: '" + name + "'!")
        return null
    }
}