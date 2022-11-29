package atonaddons.module.impl.render

import atonaddons.module.Category
import atonaddons.module.Module
import atonaddons.module.settings.impl.BooleanSetting
import atonaddons.module.settings.impl.ColorSetting
import atonaddons.module.settings.impl.NumberSetting
import atonaddons.module.settings.impl.SelectorSetting
import java.awt.Color

/**
 * This module serves as a setting storage for the dungeon map. the implementation is elsewhere.
 */
object MapRooms : Module(
    "Map Room Settings",
    category = Category.RENDER,
    description = "Appearance settings for the dungeon map."
){
    val darkenUndiscovered  = BooleanSetting("Darken Undiscovered",true, description = "Darkens unentered rooms.")
    val mapRoomNames        = SelectorSetting("Room Names", "Puzzles", arrayListOf("None", "Puzzles", "All known"), description = "Shows names of rooms on map.")
    val mapColorText        = BooleanSetting("Color Text",false, description = "Colors name and secret count based on room state.")
    val mapCheckmark        = SelectorSetting("Room Checkmarks", "Default", arrayListOf("None", "Default", "NEU"), description = "Adds room checkmarks based on room state.")
    val mapRoomTransparency = NumberSetting("Room Opacity",1.0,0.0, 1.0, 0.01)
    val mapDarkenPercent    = NumberSetting("DarkenMultiplier",0.4,0.0, 1.0, 0.01, description = "How much to darken undiscovered rooms")
    val colorBloodDoor      = ColorSetting("Blood Door", Color(252, 0, 0), true)
    val colorEntranceDoor   = ColorSetting("Entrance Door", Color(0, 123, 0), true)
    val colorRoomDoor       = ColorSetting("Normal Door", Color(92, 52, 14), true)
    val colorWitherDoor     = ColorSetting("Wither Door", Color(0, 0, 0), true)
    val colorOpenWitherDoor = ColorSetting("Opened Wither Door", Color(92, 52, 14), true)
    val colorBlood          = ColorSetting("Blood Room", Color(255, 0, 0), true)
    val colorEntrance       = ColorSetting("Entrance Room", Color(20, 133, 0), true)
    val colorFairy          = ColorSetting("Fairy Room", Color(224, 0, 255), true)
    val colorMiniboss       = ColorSetting("Miniboss Room", Color(226, 226, 50), true)
    val colorRoom           = ColorSetting("Normal Room", Color(107, 58, 17), true)
    val colorPuzzle         = ColorSetting("Puzzle Room", Color(176, 75, 213), true)
    val colorRare           = ColorSetting("Rare Room", Color(255, 203, 89), true)
    val colorTrap           = ColorSetting("Trap Room", Color(216, 127, 51), true)
    val colorUnexplored     = ColorSetting("Unexplored", Color(64, 64, 64), true)

    init {
        this.addSettings(
            darkenUndiscovered,
            mapRoomNames,
            mapColorText,
            mapCheckmark,
            mapRoomTransparency,
            mapDarkenPercent,
            colorBloodDoor,
            colorEntranceDoor,
            colorRoomDoor,
            colorWitherDoor,
            colorOpenWitherDoor,
            colorBlood,
            colorEntrance,
            colorFairy,
            colorMiniboss,
            colorRoom,
            colorPuzzle,
            colorRare,
            colorTrap,
            colorUnexplored
        )
    }

    /**
     * Automatically disable it again and open the gui
     */
    override fun onEnable() {
        super.onEnable()
        toggle()
    }

    /**
     * Prevent keybind Action.
     */
    override fun keyBind() {

    }
}