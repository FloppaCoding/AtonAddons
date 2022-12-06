package atonaddons.utils

import atonaddons.AtonAddons
import atonaddons.AtonAddons.Companion.mc
import atonaddons.module.impl.render.ClickGui
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.IChatComponent
import net.minecraft.util.StringUtils
import net.minecraftforge.client.ClientCommandHandler

/**
 * A collection of utility functions for creating and sending or displaying chat messages.
 *
 * @author Aton
 */
object ChatUtils {
    /**
     * Pattern to replace formatting codes with & with the § equivalent.
     *
     * This Regex will match all "&" that are directly followed by one of 0,1,2,3,4,5,6,7,8,9,a,b,c,d,e,f,k,l,m,n,o,r.
     *
     * This regex is the same as Regex("(?i)&(?=[0-9A-FK-OR])").
     */
    private val formattingCodePattern = Regex("&(?=[0-9A-FK-OR])", RegexOption.IGNORE_CASE)

    /**
     * Replaces chat formatting codes using "&" as escape character with "§" as the escape character.
     * Example: "&aText &r" as input return "§aText §r".
     */
    private fun reformatString(text: String): String {
        return formattingCodePattern.replace(text, "§")
    }

    /**
     * Remove control codes from the string with the vanilla function for it.
     */
    fun String.stripControlCodes(): String {
        return StringUtils.stripControlCodes(this)
    }

    /**
     * Puts a message in chat client side with the mod prefix.
     * @param reformat Replace the "&" in formatting strings with "§".
     */
    fun modMessage(text: String, reformat: Boolean = true) {
        val message: IChatComponent = ChatComponentText(if (reformat) reformatString(text) else text)
        modMessage(message)
    }

    /**
     * Puts a message in chat client side with the mod prefix.
     */
    fun modMessage(iChatComponent: IChatComponent) = chatMessage(
        ChatComponentText(
            when (ClickGui.prefixStyle.index) {
                0 -> AtonAddons.CHAT_PREFIX; 1 -> AtonAddons.SHORT_PREFIX
                else -> reformatString( ClickGui.customPrefix.text)
            }
        ).appendSibling(iChatComponent)
    )

    /**
     * Print a message in chat client side.
     * @param reformat Replace the "&" in formatting strings with "§".
     */
    fun chatMessage(text: String, reformat: Boolean = true) {
        val message: IChatComponent = ChatComponentText(if (reformat) reformatString(text) else text)
        chatMessage(message)
    }

    /**
     * Print a message in chat client side.
     */
    fun chatMessage(iChatComponent: IChatComponent) {
        mc.thePlayer.addChatMessage(iChatComponent)
    }

    fun sendChat(message: String) {
        mc.thePlayer.sendChatMessage(message)
    }

    /**
     * Runs the specified command. Per default sends it to the server  but has client side option.
     */
    fun command(text: String, clientSide: Boolean = true) {
        if (clientSide) ClientCommandHandler.instance.executeCommand(mc.thePlayer, "/$text")
        else mc.thePlayer?.sendChatMessage("/$text")
    }

    /**
     * Creates a new IChatComponent displaying [text] and showing [hoverText] when it is hovered.
     * [hoverText] can include "\n" for new lines.
     *
     * Use [IChatComponent.appendSibling] to combine multiple Chat components into one.
     * Use the formatting characters to format the text.
     */
    fun createHoverableText(text: String, hoverText: String): IChatComponent {
        val message: IChatComponent = ChatComponentText(text)
        val style = ChatStyle()
        style.chatHoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText(hoverText))
        message.chatStyle = style
        return message
    }
}