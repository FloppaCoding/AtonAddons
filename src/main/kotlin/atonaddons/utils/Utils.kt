package atonaddons.utils

import atonaddons.AtonAddons
import atonaddons.AtonAddons.Companion.mc
import atonaddons.floppamap.core.DungeonPlayer
import atonaddons.floppamap.dungeon.Dungeon
import atonaddons.mixins.MinecraftAccessor
import atonaddons.utils.ItemUtils.itemID
import atonaddons.utils.ScoreboardUtils.sidebarLines
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.network.NetHandlerPlayClient
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.util.BlockPos
import net.minecraft.util.StringUtils
import net.minecraft.util.Timer
import java.util.*

/**
 * A collection of general utility functions that have not been sorted into individual classes.
 *
 * @author Aton
 */
object Utils {

    /**
     * Referenced in the sound manager hook.
     */
    var shouldBypassVolume: Boolean = false

    fun Any?.equalsOneOf(vararg other: Any): Boolean {
        return other.any {
            this == it
        }
    }

    /**
     * Returns the actual block pos of the player. The value obtained by .position is shifted by 0.5 before flooring.
     */
    val EntityPlayerSP.flooredPosition: BlockPos
        get() = BlockPos(this.posX, this.posY, this.posZ)

    /**
     * Test whether the String contains one of the stings in the list.
     */
    fun String.containsOneOf(options: List<String>, ignoreCase: Boolean = false): Boolean {
        return this.containsOneOf(options.toSet(),ignoreCase)

    }

    /**
     * Test whether the String contains one of the stings in the list.
     */
    fun String.containsOneOf(options: Set<String>, ignoreCase: Boolean = false): Boolean {
        options.forEach{
            if (this.contains(it, ignoreCase)) return true
        }
        return false
    }

    fun <K, V> MutableMap<K, V>.removeIf(filter: (Map.Entry<K, V>) -> Boolean) : Boolean {
        Objects.requireNonNull(filter)
        var removed = false
        val each: MutableIterator<Map.Entry<K, V>> = this.iterator()
        while (each.hasNext()) {
            if (filter(each.next())) {
                each.remove()
                removed = true
            }
        }
        return removed
    }

    /**
     * The current dungeon floor (1..7) or null if not in dungeon
     */
    val currentFloor: Int?
        get() {
            sidebarLines.forEach {
                val line = ScoreboardUtils.cleanSB(it)
                if (line.contains("The Catacombs (")) {
                    return line.substringAfter("(").substringBefore(")").last().digitToIntOrNull()
                }
            }
            return null
        }

    fun inF7Boss(): Boolean {
        if(currentFloor == 7) { // check whether floor is 7
            if(mc.thePlayer.posZ > 0 ) { //check whether in boss room
                return true
            }}
        return false
    }

    fun isFloor(floor: Int): Boolean {
        sidebarLines.forEach {
            val line = ScoreboardUtils.cleanSB(it)
            if (line.contains("The Catacombs (")) {
                if (line.substringAfter("(").substringBefore(")").equalsOneOf("F$floor", "M$floor")) {
                    return true
                }
            }
        }
        return false
    }

    fun getDungeonClass(tabEntries: List<Pair<NetworkPlayerInfo, String>>, playerName: String = mc.thePlayer.name): String? {
        for (i in listOf(5, 9, 13, 17, 1)) {
            val tabText = StringUtils.stripControlCodes(tabEntries[i].second).trim()
            val name = tabText.split(" ").getOrNull(1) ?: ""

            // Here the stuff to get the class
            // first check whether it is the correct player
            if (name != playerName) continue
            // this will still contain some formatting. iirc it should look like (Mage but maybe (MageVL)
            val classWithFormatting = tabText.split(" ").getOrNull(2) ?: return null
            if (classWithFormatting.contains("(DEAD)")) return null
            return classWithFormatting.drop(1)
        }
        return null
    }

    /**
     * Returns the first dungeon Teammate with the chose class. Or null if not found / dead
     */
    fun dungeonTeammateWithClass(targetClass: String, allowSelf: Boolean = false): DungeonPlayer? {
        Dungeon.getDungeonTabList()?.let{ tabList ->
            Dungeon.dungeonTeammates.forEach {
                if (!allowSelf && it.name == mc.thePlayer.name) return@forEach
                if (getDungeonClass(tabList, it.name) == targetClass) return it
            }
        }
        return null
    }


    /**
     * Returns the current area from the tab list info.
     * If no info can be found return null.
     */
    fun getArea(): String? {
        if (!AtonAddons.inSkyblock) return null
        val nethandlerplayclient: NetHandlerPlayClient = mc.thePlayer?.sendQueue ?: return null
        val list = nethandlerplayclient.playerInfoMap ?: return null
        var area: String? = null
        var extraInfo: String? = null
        for (entry in list) {
            //  "Area: Hub"
            val areaText = entry?.displayName?.unformattedText ?: continue
            if (areaText.startsWith("Area: ")) {
                area = areaText.substringAfter("Area: ")
                if (!area.contains("Private Island")) break
            }
            if (areaText.contains("Owner:")){
                extraInfo = areaText.substringAfter("Owner:")
            }

        }
        return if (area == null)
            null
        else
            area + (extraInfo ?: "")
    }


    /**
     * Check whether the player is holding the given item.
     * Checks both the name and item ID.
     * @param name The name or item ID.
     * @param ignoreCase Applies for the item name check.
     */
    fun EntityPlayerSP?.isHolding(name: String, ignoreCase: Boolean = false): Boolean {
        return this?.heldItem?.run { displayName.contains(name, ignoreCase) || itemID == name } == true
    }

    /**
     * Check whether the player is holding one of the given items.
     * Checks both the name and item ID.
     * @param names The names or item IDs.
     * @param ignoreCase Applies for the item name check.
     */
    fun EntityPlayerSP?.isHoldingOneOf(vararg names: String, ignoreCase: Boolean = false): Boolean {
        names.forEach {
            if (this.isHolding(it, ignoreCase)) return true
        }
        return false
    }

    /**
     * Taken from Skytils:
     * Taken from SkyblockAddons under MIT License
     * https://github.com/BiscuitDevelopment/SkyblockAddons/blob/master/LICENSE
     * @author BiscuitDevelopment
     */
    fun playLoudSound(sound: String?, volume: Float, pitch: Float) {
        shouldBypassVolume = true
        mc.thePlayer?.playSound(sound, volume, pitch)
        shouldBypassVolume = false
    }

    val Minecraft.timer: Timer
        get() = (this as MinecraftAccessor).timer
}
