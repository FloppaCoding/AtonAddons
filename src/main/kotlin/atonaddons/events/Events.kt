package atonaddons.events

import atonaddons.floppamap.core.Room
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

/**
 * Fired in Dungeon.kt whenever the room is changed.
 */
class RoomChangeEvent(val newRoom: Room?, val oldRoom: Room?) : Event()

/**
 * Fired when a secret is picked up in dungeons.
 * Currently gets fired whenever SecretChime plays its sound.
 */
class DungeonSecretEvent() : Event()


