package atonaddons.config

import atonaddons.config.jsonutils.SettingDeserializer
import atonaddons.config.jsonutils.SettingSerializer
import atonaddons.module.Module
import atonaddons.module.ModuleManager
import atonaddons.module.settings.Setting
import atonaddons.module.settings.impl.*
import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import atonaddons.AtonAddons.Companion.CONFIG_DIR
import java.awt.Color
import java.io.File
import java.io.IOException

class ModuleConfig(path: File) {

    private val gson = GsonBuilder()
        .registerTypeAdapter(object : TypeToken<Setting>(){}.type, SettingSerializer())
        .registerTypeAdapter(object : TypeToken<Setting>(){}.type, SettingDeserializer())
        .excludeFieldsWithoutExposeAnnotation()
        .setPrettyPrinting().create()


    private val configFile = File(path, "${CONFIG_DIR}Config.json")

    init {
        try {
            // This gets run before the pre initialization event (it gets run when the Companion object from AtonAddons
            // is created)
            // therefore the directory did not get created by the preInit handler.
            // It is created here
            if (!path.exists()) {
                path.mkdirs()
            }
            // create file if it doesn't exist
            configFile.createNewFile()
        } catch (e: Exception) {
            println("Error initializing module config")
        }
    }


    fun loadConfig() {
        try {
            val configModules: ArrayList<Module>
            with(configFile.bufferedReader().use { it.readText() }) {
                if (this == "") {
                    return
                }
                configModules= gson.fromJson(
                    this,
                    object : TypeToken<ArrayList<Module>>() {}.type
                )
            }
            configModules.forEach { configModule ->
                ModuleManager.getModuleByName(configModule.name).run updateModule@{
                    // If the module was not found check whether it can be a keybind
                    val module = this ?: if (configModule.settings.find { (it is BooleanSetting) && it.name == "THIS_IS_A_KEY_BIND" } != null) {
                        ModuleManager.addNewKeybind()
                    }else {
                        return@updateModule
                    }
                    if (module.enabled != configModule.enabled) module.toggle()
                    module.keyCode = configModule.keyCode
                    for (setting in module.settings) {
                        for (configSetting in configModule.settings) {
                            // When the config parsing failed it can result in this being null. The compiler does not know this.
                            // So just ignore the warning here.
                            if (configSetting == null) continue
                            if (setting.name.equals(configSetting.name, ignoreCase = true)) {
                                when (setting) {
                                    is BooleanSetting -> setting.enabled = (configSetting as BooleanSetting).enabled
                                    is NumberSetting -> setting.value = (configSetting as NumberSetting).value
                                    is ColorSetting -> setting.value = Color((configSetting as NumberSetting).value.toInt(), true)
                                    is SelectorSetting -> setting.selected = (configSetting as StringSetting).text
                                    is StringSetting -> setting.text = (configSetting as StringSetting).text
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: JsonSyntaxException) {
            println("Error parsing Aton Addons config.")
            println(e.message)
            e.printStackTrace()
        } catch (e: JsonIOException) {
            println("Error reading Aton Addons config.")
        } catch (e: Exception) {
            println("Aton Addons Config Error.")
            println(e.message)
            e.printStackTrace()
        }
    }

    fun saveConfig() {
        try {
            configFile.bufferedWriter().use {
                it.write(gson.toJson(ModuleManager.modules))
            }
        } catch (e: IOException) {
            println("Error saving Aton Addons config.")
        }
    }
}