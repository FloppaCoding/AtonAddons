package atonaddons.utils

import atonaddons.AtonAddons
import com.google.common.collect.ComparisonChain
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.world.WorldSettings

object TabListUtils {

    private val tabListOrder = Comparator<NetworkPlayerInfo> { o1, o2 ->
        if (o1 == null) return@Comparator -1
        if (o2 == null) return@Comparator 0
        return@Comparator ComparisonChain.start().compareTrueFirst(
            o1.gameType != WorldSettings.GameType.SPECTATOR,
            o2.gameType != WorldSettings.GameType.SPECTATOR
        ).compare(
            o1.playerTeam?.registeredName ?: "",
            o2.playerTeam?.registeredName ?: ""
        ).compare(o1.gameProfile.name, o2.gameProfile.name).result()
    }

    val tabList: List<Pair<NetworkPlayerInfo, String>>
        get() = (AtonAddons.mc.thePlayer?.sendQueue?.playerInfoMap?.sortedWith(tabListOrder) ?: emptyList())
            .map { Pair(it, AtonAddons.mc.ingameGUI.tabList.getPlayerName(it)) }

}