package atonaddons.utils

import atonaddons.module.impl.render.ClickGui
import com.google.gson.JsonParser
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils


object HypixelApiUtils {

    /**
     * Get the total amount of secrets a player has collected.
     * Only use this method in a coroutine to not freeze the main thread.
     *
     * Based on a method provided by Harry282
     * https://github.com/Harry282
     */
    suspend fun getSecrets(uuid: String): Int? {
        val response = fetch("https://api.hypixel.net/player?key=${ClickGui.apiKey.text}&uuid=${uuid}")
        return if (response == null) null else try {
            val jsonObject = JsonParser().parse(response).asJsonObject
            if (jsonObject.getAsJsonPrimitive("success")?.asBoolean == true) {
                jsonObject.getAsJsonObject("player")?.getAsJsonObject("achievements")
                    ?.getAsJsonPrimitive("skyblock_treasure_hunter")?.asInt
            }else
                null
        }catch (_: Exception){
            null
        }
    }

    @Suppress("RedundantSuspendModifier")
    private suspend fun fetch(uri: String): String? {
        HttpClients.createMinimal().use {
            try {
                val httpGet = HttpGet(uri)
                return EntityUtils.toString(it.execute(httpGet).entity)
            }catch (_: Exception) {
                return null
            }
        }
    }
}