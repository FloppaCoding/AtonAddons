package atonaddons.floppamap.dungeon

import atonaddons.AtonAddons.Companion.RESOURCE_DOMAIN
import atonaddons.AtonAddons.Companion.inDungeons
import atonaddons.AtonAddons.Companion.mc
import atonaddons.floppamap.core.*
import atonaddons.floppamap.utils.MapUtils
import atonaddons.floppamap.utils.MapUtils.roomSize
import atonaddons.floppamap.utils.RenderUtils
import atonaddons.module.impl.render.DungeonMap
import atonaddons.module.impl.render.MapRooms
import atonaddons.shaders.impl.Chroma2D
import atonaddons.ui.hud.EditHudGUI
import atonaddons.ui.hud.HudElement
import atonaddons.utils.Utils.equalsOneOf
import gg.essential.elementa.utils.withAlpha
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color

object MapRender: HudElement(
    DungeonMap.xHud,
    DungeonMap.yHud,
    128,
    138,
    DungeonMap.mapScale
){

    private val neuGreen        = ResourceLocation(RESOURCE_DOMAIN, "floppamap/neu/green_check.png")
    private val neuWhite        = ResourceLocation(RESOURCE_DOMAIN, "floppamap/neu/white_check.png")
    private val neuCross        = ResourceLocation(RESOURCE_DOMAIN, "floppamap/neu/cross.png")
    private val neuQuestion     = ResourceLocation(RESOURCE_DOMAIN, "floppamap/neu/question.png")
    private val defaultGreen    = ResourceLocation(RESOURCE_DOMAIN, "floppamap/default/green_check.png")
    private val defaultWhite    = ResourceLocation(RESOURCE_DOMAIN, "floppamap/default/white_check.png")
    private val defaultCross    = ResourceLocation(RESOURCE_DOMAIN, "floppamap/default/cross.png")
    private val defaultQuestion = ResourceLocation(RESOURCE_DOMAIN, "floppamap/default/question.png")

    override fun renderHud() {
        super.renderHud()

        if (!inDungeons) return
        if (DungeonMap.hideInBoss.enabled && Dungeon.inBoss) return

        GlStateManager.enableAlpha()
        GlStateManager.enableBlend()

        // Background
        RenderUtils.renderRect(
            0.0,
            0.0,
            128.0,
            if (DungeonMap.showRunInformation.enabled) 142.0 else 128.0,
            DungeonMap.mapBackground.value
        )
        // Border
        if (DungeonMap.chromaBorder.enabled) Chroma2D.useShader()
        RenderUtils.renderRectBorder(
            0.0,
            0.0,
            128.0,
            if (DungeonMap.showRunInformation.enabled) 142.0 else 128.0,
            DungeonMap.mapBorderWidth.value,
            DungeonMap.mapBorder.value
        )
        if (DungeonMap.chromaBorder.enabled) Chroma2D.stopShader()
        // Run Information
        if (mc.currentScreen !is EditHudGUI) {
            if (DungeonMap.showRunInformation.enabled) {
                renderRunInformation()
            }
        }
        // Scissor
        GlStateManager.pushMatrix()
        val scale = mc.displayHeight /  ScaledResolution(mc).scaledHeight.toDouble()
        GL11.glScissor(
            (x*scale).toInt(),
            (mc.displayHeight - y*scale - 128*scale*this.scale.value).toInt() ,
            (128*scale*this.scale.value).toInt(),
            (128*scale*this.scale.value).toInt()
        )
        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        // Spinny map
        if (DungeonMap.spinnyMap.enabled || DungeonMap.centerOnPlayer.enabled) {
            GlStateManager.translate(64.0, 64.0, 0.0)
            if (DungeonMap.spinnyMap.enabled) GlStateManager.rotate(-mc.thePlayer.rotationYawHead + 180f, 0f, 0f, 1f)
        }
        // Room scale
        GlStateManager.scale(DungeonMap.roomScale.value, DungeonMap.roomScale.value, 1.0)
        // Centering
        if (DungeonMap.centerOnPlayer.enabled) {
            GlStateManager.translate(
                -((mc.thePlayer.posX - Dungeon.startX + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.first - 2),
                -((mc.thePlayer.posZ - Dungeon.startZ + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.second - 2),
                0.0
            )
        }else if (DungeonMap.spinnyMap.enabled){
            GlStateManager.translate(-64.0, -64.0, 0.0)
        }

        renderRooms()

        if (mc.currentScreen !is EditHudGUI) {
            renderText()
            renderPlayerHeads()
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST)
        GlStateManager.popMatrix()

    }

    private fun renderRooms() {
        GlStateManager.pushMatrix()
        GlStateManager.translate(MapUtils.startCorner.first.toFloat(), MapUtils.startCorner.second.toFloat(), 0f)

        val connectorSize = roomSize shr 2

        for (y in 0..10) {
            for (x in 0..10) {
                val tile = Dungeon.dungeonList[y * 11 + x] ?: continue
                if (tile.state == RoomState.UNDISCOVERED && !tile.visited) continue

                val xOffset = (x shr 1) * (roomSize + connectorSize)
                val yOffset = (y shr 1) * (roomSize + connectorSize)

                val xEven = x and 1 == 0
                val yEven = y and 1 == 0

                val color = if (MapRooms.darkenUndiscovered.enabled && tile.state == RoomState.UNDISCOVERED) {
                    tile.color.run {
                        Color(
                            (red   * (1 - MapRooms.mapDarkenPercent.value)).toInt(),
                            (green * (1 - MapRooms.mapDarkenPercent.value)).toInt(),
                            (blue  * (1 - MapRooms.mapDarkenPercent.value)).toInt(),
                            (alpha * MapRooms.mapRoomTransparency.value).toInt()
                        )
                    }
                } else tile.color.run { withAlpha((alpha * MapRooms.mapRoomTransparency.value).toInt()) }

                when {
                    xEven && yEven -> if (tile is Room) {
                        RenderUtils.renderRect(
                            xOffset.toDouble(),
                            yOffset.toDouble(),
                            roomSize.toDouble(),
                            roomSize.toDouble(),
                            color
                        )
                    }
                    !xEven && !yEven -> {
                        RenderUtils.renderRect(
                            xOffset.toDouble(),
                            yOffset.toDouble(),
                            (roomSize + connectorSize).toDouble(),
                            (roomSize + connectorSize).toDouble(),
                            color
                        )
                    }
                    else -> drawRoomConnector(
                        xOffset,
                        yOffset,
                        connectorSize,
                        tile is Door,
                        !xEven,
                        color
                    )
                }
            }
        }
        GlStateManager.popMatrix()
    }

    private fun renderText() {
        GlStateManager.pushMatrix()
        GlStateManager.translate(MapUtils.startCorner.first.toFloat(), MapUtils.startCorner.second.toFloat(), 0f)

        val connectorSize = roomSize shr 2

        for (y in 0..10 step 2) {
            for (x in 0..10 step 2) {

                val tile = Dungeon.dungeonList[y * 11 + x] ?: continue

                if (tile.state == RoomState.UNDISCOVERED && !tile.visited) continue

                if (tile is Room && (tile.isUnique || tile.state == RoomState.QUESTION_MARK) ) {

                    val xOffset = (x shr 1) * (roomSize + connectorSize)
                    val yOffset = (y shr 1) * (roomSize + connectorSize)

                    if (MapRooms.mapCheckmark.index != 0) {
                        getCheckmark(tile, MapRooms.mapCheckmark.index)?.let {
                            GlStateManager.enableAlpha()
                            GlStateManager.color(255f, 255f, 255f, 255f)
                            mc.textureManager.bindTexture(it)
                            Gui.drawModalRectWithCustomSizedTexture(
                                xOffset + 2, yOffset + 2,
                                0f, 0f, roomSize - 4, roomSize - 4, roomSize - 4f, roomSize - 4f
                            )
                            GlStateManager.disableAlpha()
                        }
                    }

                    val name = mutableListOf<String>()

                    if ((MapRooms.mapRoomNames.index != 0 && tile.data.type == RoomType.PUZZLE ||
                        (MapRooms.mapRoomNames.index == 2 && tile.data.type.equalsOneOf(
                            RoomType.NORMAL,
                            RoomType.RARE,
                            RoomType.CHAMPION,
                            RoomType.TRAP
                        ) && tile.visited))
                        && (tile.state.revealed || tile.visited) && !tile.data.name.startsWith("Unknown")
                    ) {
                        name.addAll(tile.data.name.split(" "))
                    }
                    // Room Secrets
                    if (tile.canHaveSecrets) {
                        val maxSecrets = if (tile.visited) tile.data.maxSecrets?.toString() ?: "?" else "?"
                        name.add(
                            "${tile.data.currentSecrets}/${maxSecrets}"
                        )
                    }

                    val color = if (MapRooms.mapColorText.enabled) when (tile.state) {
                        RoomState.GREEN -> 0x55ff55
                        RoomState.CLEARED, RoomState.FAILED -> 0xffffff
                        else -> 0xaaaaaa
                    } else 0xffffff

                    // Offset + half of roomsize
                    RenderUtils.renderCenteredText(name, xOffset + (roomSize shr 1), yOffset + (roomSize shr 1), color)
                }
            }
        }
        GlStateManager.popMatrix()
    }

    private fun getCheckmark(tile: Tile, type: Int): ResourceLocation? {
        return when (type) {
            1 -> when (tile.state) {
                RoomState.CLEARED -> defaultWhite
                RoomState.GREEN -> defaultGreen
                RoomState.FAILED -> defaultCross
                RoomState.QUESTION_MARK -> {
                    if (!tile.visited)
                        defaultQuestion
                    else null
                }
                else -> null
            }
            2 -> when (tile.state) {
                RoomState.CLEARED -> neuWhite
                RoomState.GREEN -> neuGreen
                RoomState.FAILED -> neuCross
                RoomState.QUESTION_MARK -> {
                    if (!tile.visited)
                        neuQuestion
                    else null
                }
                else -> null
            }
            else -> null
        }
    }

    private fun renderPlayerHeads() {
        // Try catch because the dungeonTeammates get updated in a coroutine.
        try {
            for (player in Dungeon.dungeonTeammates) {
                RenderUtils.drawPlayerHead(player)
            }
        }catch (_: ConcurrentModificationException) {}
    }

    private fun drawRoomConnector(
        x: Int,
        y: Int,
        doorWidth: Int,
        doorway: Boolean,
        vertical: Boolean,
        color: Color
    ) {
        val doorwayOffset = if (roomSize == 16) 5 else 6
        val width = if (doorway) 6 else roomSize
        var x1 = if (vertical) x + roomSize else x
        var y1 = if (vertical) y else y + roomSize
        if (doorway) {
            if (vertical) y1 += doorwayOffset else x1 += doorwayOffset
        }
        RenderUtils.renderRect(
            x1.toDouble(), y1.toDouble(),
            (if (vertical) doorWidth else width).toDouble(),
            (if (vertical) width else doorWidth).toDouble(),
            color
        )
    }

    private fun renderRunInformation() {
        GlStateManager.pushMatrix()
        GlStateManager.translate(0f, 128f, 0f)
        GlStateManager.scale(0.66, 0.66, 1.0)
        // First line
        mc.fontRendererObj.drawString("Secrets: ${RunInformation.secretCount}/${RunInformation.totalSecrets ?: "?"}", 5, 0, 0xffffff)
        mc.fontRendererObj.drawString("Crypts: ${RunInformation.cryptsCount}", 85, 0, 0xffffff)
        mc.fontRendererObj.drawString("Deaths: ${RunInformation.deathCount}", 140, 0, 0xffffff)
        // Second Line
        mc.fontRendererObj.drawString("Score: ${RunInformation.score}", 5, mc.fontRendererObj.FONT_HEIGHT, 0xffffff)
        GlStateManager.popMatrix()
    }
}
