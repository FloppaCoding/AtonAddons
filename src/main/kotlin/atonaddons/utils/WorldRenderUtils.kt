package atonaddons.utils

import atonaddons.AtonAddons.Companion.mc
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11
import java.awt.Color

object WorldRenderUtils {

    private val tessellator: Tessellator = Tessellator.getInstance()
    private val worldRenderer: WorldRenderer = tessellator.worldRenderer
    private val renderManager = mc.renderManager

    fun drawCustomSizedBoxAt(x: Double, y: Double, z: Double, size: Double, color: Color, thickness: Float = 3f, relocate: Boolean = true) {
        GlStateManager.disableLighting()
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glLineWidth(thickness)
        GlStateManager.disableDepth()
        GlStateManager.disableTexture2D()

        GlStateManager.pushMatrix()

        if (relocate) GlStateManager.translate(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ)
        worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)
        GlStateManager.color(color.red.toFloat() / 255f, color.green.toFloat() / 255f,
            color.blue.toFloat() / 255f, 1f)


        worldRenderer.pos(x+size,y+size,z+size).endVertex()
        worldRenderer.pos(x+size,y+size,z).endVertex()
        worldRenderer.pos(x,y+size,z).endVertex()
        worldRenderer.pos(x,y+size,z+size).endVertex()
        worldRenderer.pos(x+size,y+size,z+size).endVertex()
        worldRenderer.pos(x+size,y,z+size).endVertex()
        worldRenderer.pos(x+size,y,z).endVertex()
        worldRenderer.pos(x,y,z).endVertex()
        worldRenderer.pos(x,y,z+size).endVertex()
        worldRenderer.pos(x,y,z).endVertex()
        worldRenderer.pos(x,y+size,z).endVertex()
        worldRenderer.pos(x,y,z).endVertex()
        worldRenderer.pos(x+size,y,z).endVertex()
        worldRenderer.pos(x+size,y+size,z).endVertex()
        worldRenderer.pos(x+size,y,z).endVertex()
        worldRenderer.pos(x+size,y,z+size).endVertex()
        worldRenderer.pos(x,y,z+size).endVertex()
        worldRenderer.pos(x,y+size,z+size).endVertex()
        worldRenderer.pos(x+size,y+size,z+size).endVertex()

        tessellator.draw()

        GlStateManager.popMatrix()
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GlStateManager.disableBlend()
    }
}