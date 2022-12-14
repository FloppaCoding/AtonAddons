package atonaddons.module

import atonaddons.AtonAddons
import atonaddons.module.impl.keybinds.AddKeybind
import atonaddons.events.PreKeyInputEvent
import atonaddons.events.PreMouseInputEvent
import atonaddons.module.impl.dungeon.*
import atonaddons.module.impl.keybinds.KeyBind
import atonaddons.module.impl.misc.*
import atonaddons.module.impl.render.*
import atonaddons.module.settings.Setting
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * This object handles all the modules. After making a module it just has to be added to the "modules" list and
 * everything else will be taken care of automatically. This entails:
 *
 * It will be added to the click gui in the order it is put in here. But keep in mind that the category is set within
 * the module. The comments here are only for readability.
 *
 * All settings that are registered within the module will be saved to and loaded the module config.
 * For this to properly work remember to register the settings to the module.
 *
 * The module will be registered and unregistered to the forge eventbus when it is enabled / disabled.
 *
 * The module will be informed of its keybind press.
 *
 *
 * @author Aton
 * @see Module
 * @see Setting
 */
object ModuleManager {
    /**
     * All modules have to be added to this list to function!
     */
    val modules: ArrayList<Module> = arrayListOf(
        //DUNGEON
        SecretChime,
        ExtraStats,
        LeapHighlights,
        PartyTracker,

        //RENDER
        ClickGui,
        EditHud,
        DungeonWarpTimer,
        DungeonMap,
        MapRooms,
        DoorESP,
        CoordinateDisplay,
        NoFireOverlay,
        ItemAnimations,

        //MISC
        ToggleSprint,
        RemoveFrontView,

        //KEYBIND
        AddKeybind,
    )

    /**
     * Creates a new keybind module and adds it to the list.
     */
    fun addNewKeybind(): KeyBind {
        val number = (modules
            .filter{module -> module.name.startsWith("New Keybind")}
            .map {module -> module.name.filter { c -> c.isDigit() }.toIntOrNull()}
            .maxByOrNull { it ?: 0} ?: 0) + 1
        val keyBind = KeyBind("New Keybind $number")
        modules.add(keyBind)
        return keyBind
    }

    fun removeKeyBind(bind: KeyBind) {
        modules.remove(bind)
        AtonAddons.clickGUI.setUpPanels()
    }

    /**
     * Handles the key binds for the modules.
     * Note that the custom event fired in the minecraft mixin is used here and not the forge event.
     * That is done to run this code before the vanilla minecraft code.
     */
    @SubscribeEvent
    fun activateModuleKeyBinds(event: PreKeyInputEvent) {
        modules.stream().filter { module -> module.keyCode == event.key }.forEach { module -> module.keyBind() }
    }

    /**
     * Handles the key binds for the modules.
     * Note that the custom event fired in the minecraft mixin is used here and not the forge event.
     * That is done to run this code before the vanilla minecraft code.
     */
    @SubscribeEvent
    fun activateModuleMouseBinds(event: PreMouseInputEvent) {
        modules.stream().filter { module -> module.keyCode + 100 == event.button }.forEach { module -> module.keyBind() }
    }

    fun getModuleByName(name: String): Module? {
        return modules.stream().filter{module -> module.name.equals(name, ignoreCase = true)}.findFirst().orElse(null)
    }
}