package atonaddons.utils

import atonaddons.module.impl.render.ClickGui
import com.google.gson.JsonParser
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

/**
 * This Object is made to collect methods that access the Hypixel API.
 *
 * @author Aton
 */
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
        return try {
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

    /**
     * Fetches data from the specified [uri].
     * @param uri The URI from where you want to fetch a resource.
     *
     * Method provided by Harry282
     * https://github.com/Harry282
     */
    suspend fun fetch(uri: String): String {
        HttpClients.createMinimal().use {
            val httpGet = HttpGet(uri)
            return EntityUtils.toString(it.execute(httpGet).entity)
        }
    }
}