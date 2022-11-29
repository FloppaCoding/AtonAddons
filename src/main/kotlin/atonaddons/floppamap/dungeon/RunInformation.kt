package atonaddons.floppamap.dungeon

import net.minecraft.client.network.NetworkPlayerInfo
import kotlin.math.floor

object RunInformation {

    var deathCount = 0
    var secretCount = 0
    var cryptsCount = 0
    var totalSecrets: Int? = null
    val puzzles = mutableListOf<String>()

    private val deathsPattern = Regex("§r§a§lDeaths: §r§f\\((?<deaths>\\d+)\\)§r")
    private val puzzleCountPattern = Regex("§r§b§lPuzzles: §r§f\\((?<count>\\d)\\)§r")

    /**
     * Matches all three states a puzzle can be in (unfinished, completed, failed).
     * Also matches unknown puzzle (???).
     * To change that behaviour change the regex to "§r (?<puzzle>.+): §r§7\\[§r§[ac6]§l[✔✖✦]§r§7] §.+".
     */
    val puzzlePattern = Regex("§r (?<puzzle>.+): §r§7\\[§r§[ac6]§l[✔✖✦]§r§7].+")
    private val failedPuzzlePattern = Regex("§r (?<puzzle>.+): §r§7\\[§r§c§l✖§r§7] §.+")
    private val solvedPuzzlePattern = Regex("§r (?<puzzle>.+): §r§7\\[§r§a§l✔§r§7] §.+")
    private val secretsFoundPattern = Regex("§r Secrets Found: §r§b(?<secrets>\\d+)§r")
    private val secretsFoundPercentagePattern = Regex("§r Secrets Found: §r§[ae](?<percentage>[\\d.]+)%§r")
    private val cryptsPattern = Regex("§r Crypts: §r§6(?<crypts>\\d+)§r")

    fun updateRunInformation(tabEntries: List<Pair<NetworkPlayerInfo, String>>) {
        /** Used to determine whether the current line is a puzzle */
        var readingPuzzles = false
        tabEntries.forEach { pair ->
            val text = pair.second
            when {
                readingPuzzles -> {
                    val matcher = puzzlePattern.find(text) ?: return@forEach
                    matcher.groups["puzzle"]?.value?.let { name -> if (!name.contains("???")) puzzles.add(name) }
                }
                text.contains("Deaths: ") -> {
                    val matcher = deathsPattern.find(text) ?: return@forEach
                    deathCount = matcher.groups["deaths"]?.value?.toIntOrNull() ?: deathCount
                }
                text.contains("Secrets Found: ") -> {
                    if (text.contains("%")) {
                        val matcher = secretsFoundPercentagePattern.find(text) ?: return@forEach
                        val percentagePer = (matcher.groups["percentage"]?.value?.toDoubleOrNull() ?: 0.0)
                        totalSecrets = if (secretCount > 0 && percentagePer > 0) floor(100f / percentagePer * secretCount + 0.5).toInt() else null
                    } else {
                        val matcher = secretsFoundPattern.find(text) ?: return@forEach
                        secretCount = matcher.groups["secrets"]?.value?.toIntOrNull() ?: secretCount
                    }
                    val matcher = secretsFoundPattern.find(text) ?: return@forEach
                    secretCount = matcher.groups["secrets"]?.value?.toIntOrNull() ?: secretCount
                }
                text.contains("Crypts: ") -> {
                    val matcher = cryptsPattern.find(text) ?: return@forEach
                    cryptsCount = matcher.groups["crypts"]?.value?.toIntOrNull() ?: cryptsCount
                }
                text.contains("Puzzles: ") -> {
                    readingPuzzles = true
                    puzzles.clear()
                }
            }
        }
    }

    fun reset() {
        totalSecrets = null
        puzzles.clear()
    }
}
/*
§r ???: §r§7[§r§6§l✦§r§7]§r
§r Quiz: §r§7[§r§6§l✦§r§7] §r
§r Ice Path: §r§7[§r§6§l✦§r§7] §r
*/