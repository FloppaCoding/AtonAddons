package atonaddons.commands

import atonaddons.AtonAddons
import atonaddons.AtonAddons.Companion.mc
import atonaddons.floppamap.dungeon.Dungeon
import atonaddons.utils.ChatUtils.chatMessage
import atonaddons.utils.Utils.equalsOneOf
import atonaddons.utils.ChatUtils.modMessage
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.Vec3
import kotlin.math.floor

/**
 * This command is there to give info about where in skyblock you currently are and which automated actions are defined there.
 *
 * @author Aton
 */
class WhereCommand : CommandBase() {
    override fun getCommandName(): String {
        return "where"
    }

    override fun getCommandUsage(sender: ICommandSender?): String {
        return "/$commandName"
    }

    override fun getRequiredPermissionLevel(): Int {
        return 0
    }

    override fun processCommand(sender: ICommandSender?, args: Array<out String>?) {
        try {
            if (!AtonAddons.inDungeons) modMessage("§cNot in Dungeon!")
            val room = Dungeon.currentRoom ?: AtonAddons.currentRegion ?: return modMessage("§cRoom not recognized!")
            val pos = Vec3(floor(mc.thePlayer.posX), floor(mc.thePlayer.posY), floor(mc.thePlayer.posZ))
            val key = getKey(
                pos,
                room.x,
                room.z,
                0
            )
            modMessage("Room Information")
            chatMessage("§r&eCurrent room: §r" + room.data.name)
            chatMessage("§r&eRoom coordinates: §r" + room.x + ", " + room.z)
            chatMessage("§r§eRoomState: ${room.state.name}")
            chatMessage("§r&eRelative Player coordinates: §r" + key.joinToString())
        }catch (e: Throwable) {
            modMessage("§cCould not get data!")
        }
    }

    /**
     * Returns a mutable list of 3 Integers representing the players position in the current room.
     */
    private fun getKey(vec: Vec3, roomX: Int, roomZ: Int, rotation: Int): MutableList<Int> {
        getRelativeCoords(vec, roomX, roomZ, rotation).run {
            return mutableListOf(this.xCoord.toInt(), this.yCoord.toInt(), this.zCoord.toInt())
        }
    }

    /**
     * Gets the relative player position in a room
     */
    private fun getRelativeCoords(vec: Vec3, roomX: Int, roomZ: Int, rotation: Int): Vec3 {
        return getRotatedCoords(vec.subtract(roomX.toDouble(), 0.0, roomZ.toDouble()), -rotation)
    }

    /**
     * Returns the real rotations of the given vec in a room with given rotation.
     * To get the relative rotation inside a room use 360 - rotation.
     */
    private fun getRotatedCoords(vec: Vec3, rotation: Int): Vec3 {
        return when {
            rotation.equalsOneOf(90, -270) -> Vec3(-vec.zCoord, vec.yCoord, vec.xCoord)
            rotation.equalsOneOf(180, -180) -> Vec3(-vec.xCoord, vec.yCoord, -vec.zCoord)
            rotation.equalsOneOf(270, -90) -> Vec3(vec.zCoord, vec.yCoord, -vec.xCoord)
            else -> vec
        }
    }
}