package atonaddons

import atonaddons.commands.*
import atonaddons.config.ModuleConfig
import atonaddons.floppamap.core.Room
import atonaddons.floppamap.dungeon.Dungeon
import atonaddons.floppamap.utils.RoomUtils
import atonaddons.module.ModuleManager
import atonaddons.module.impl.render.DungeonWarpTimer
import atonaddons.ui.clickgui.ClickGUI
import atonaddons.utils.ScoreboardUtils
import atonaddons.utils.Utils
import gg.essential.api.EssentialAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent
import java.io.File
import kotlin.coroutines.EmptyCoroutineContext

@Mod(
    modid = AtonAddons.MOD_ID,
    name = AtonAddons.MOD_NAME,
    version = AtonAddons.MOD_VERSION,
    clientSideOnly = true
)
class AtonAddons {
    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        // this seems to be redundant
        val directory = File(event.modConfigurationDirectory, CONFIG_DIR)
        if (!directory.exists()) {
            directory.mkdirs()
        }

    }

    @Mod.EventHandler
    fun onInit(event: FMLInitializationEvent) {

        listOf(
            MainCommand,
            WhereCommand(),
        ).forEach {
            ClientCommandHandler.instance.registerCommand((it))
        }

        listOf(
            this,
            Dungeon,
            ModuleManager,
        ).forEach(MinecraftForge.EVENT_BUS::register)

        clickGUI = ClickGUI()
    }

    @Mod.EventHandler
    fun postInit(event: FMLLoadCompleteEvent) = runBlocking {

        launch {
            moduleConfig.loadConfig()
            clickGUI.setUpPanels()

            //This is required for the Warp cooldown to track in the background whithout the need to enable it first.
            if(!DungeonWarpTimer.enabled && DungeonWarpTimer.trackInBackground.enabled) {
                MinecraftForge.EVENT_BUS.register(DungeonWarpTimer)
            }
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        tickRamp++
        totalTicks++
        if (display != null) {
            mc.displayGuiScreen(display)
            display = null
        }
        if (tickRamp % 20 == 0) {
            if (mc.thePlayer != null) {
                val onHypixel = EssentialAPI.getMinecraftUtil().isHypixel()

                inSkyblock = onHypixel && mc.theWorld.scoreboard.getObjectiveInDisplaySlot(1)
                    ?.let { ScoreboardUtils.cleanSB(it.displayName).contains("SKYBLOCK") } ?: false

                // If alr known that in dungeons dont update the value. It does get reset to false on world change.
                if (!inDungeons) {
                    inDungeons = inSkyblock && ScoreboardUtils.sidebarLines.any {
                        ScoreboardUtils.cleanSB(it).run {
                            (contains("The Catacombs") && !contains("Queue")) || contains("Dungeon Cleared:")
                        }
                    }
                }
            }
            tickRamp = 0
        }
        val newRegion = Utils.getArea()
        if (currentRegion?.data?.name != newRegion){
            currentRegion = newRegion?.let { RoomUtils.instanceRegionRoom(it)}
        }
    }

    @SubscribeEvent
    fun onDisconnect(event: ClientDisconnectionFromServerEvent) {
        inSkyblock = false
        inDungeons = false
        moduleConfig.saveConfig()
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        inDungeons = false
        currentRegion = null
        tickRamp = 18
    }

    companion object {
        const val MOD_ID = "aa"
        const val MOD_NAME = "Aton Addons"
        const val MOD_VERSION = "0.1.2"
        const val CHAT_PREFIX = "§0§l[§4§lAton Addons§0§l]§r"
        const val SHORT_PREFIX = "§0§l[§4§lAA§0§l]§r"
        const val RESOURCE_DOMAIN = "atonaddons"
        const val CONFIG_DIR = RESOURCE_DOMAIN

        @JvmField
        val mc: Minecraft = Minecraft.getMinecraft()

        var display: GuiScreen? = null

        val scope = CoroutineScope(EmptyCoroutineContext)

        val moduleConfig = ModuleConfig(File(mc.mcDataDir, "config/$CONFIG_DIR"))

        lateinit var clickGUI: ClickGUI

        var currentRegion: Room? = null
        var inSkyblock = false
        var inDungeons = false
            get() = inSkyblock && field
        /**
         * Keeps track of elapsed ticks, gets reset at 20
         */
        var tickRamp = 0
        var totalTicks: Long = 0
    }
}
