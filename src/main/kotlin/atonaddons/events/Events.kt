package atonaddons.events

import atonaddons.floppamap.core.DungeonPlayer
import atonaddons.floppamap.core.Room
import atonaddons.floppamap.core.RoomState
import atonaddons.floppamap.core.Tile
import atonaddons.floppamap.dungeon.Dungeon
import atonaddons.floppamap.dungeon.MapUpdate
import net.minecraft.client.audio.ISound
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.Entity
import net.minecraft.inventory.Container
import net.minecraft.inventory.Slot
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

open class GuiContainerEvent(val container: Container, val gui: GuiContainer) : Event() {
    @Cancelable
    class DrawSlotEvent(container: Container, gui: GuiContainer, var slot: Slot) :
        GuiContainerEvent(container, gui)

    @Cancelable
    class SlotClickEvent(container: Container, gui: GuiContainer, var slot: Slot?, var slotId: Int) :
        GuiContainerEvent(container, gui)
}

class PlaySoundEventPre(val p_sound: ISound) : Event()

/**
 * Fired when an entity is removed from the world.
 */
class EntityRemovedEvent(val entity: Entity) : Event()

//<editor-fold desc="Dungeon Events">

/**
 * Fired in Dungeon.kt whenever the room is changed.
 */
class RoomChangeEvent(val newRoom: Room?, val oldRoom: Room?) : Event()

/**
 * Fired when a secret is picked up in dungeons.
 * Currently gets fired whenever SecretChime plays its sound.
 */
class DungeonSecretEvent : Event()

/**
 * Fired in [MapUpdate.updatePlayers] when a new dungeon teammate is added to the start of the run.
 */
class DungeonTeammateAddEvent(val dungeonPlayer: DungeonPlayer) : Event()

/**
 * Fired in [Dungeon.onChat] when the "> EXTRA STATS <" message is received.
 */
class DungeonEndEvent : Event()

/**
 * Posted in [MapUpdate.updateRooms] right before the state of a Tile is changed.
 * The old state is still contained in [tile] as [tile.state].
 */
class DungeonRoomStateChangeEvent(val tile: Tile, val newState: RoomState) : Event()

//</editor-fold>


