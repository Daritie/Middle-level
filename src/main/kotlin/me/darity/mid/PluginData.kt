package me.darity.mid

import com.google.gson.GsonBuilder
import java.io.File
import java.util.UUID

object PluginData {
    private val file = File(MidPlugin.instance.dataFolder, "midData.json")
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    data class DataStructure(
        val spawnOwners: MutableList<UUID> = mutableListOf()
    )
    private var data = DataStructure()
    val spawnOwners: MutableList<UUID>
        get() = data.spawnOwners

    fun load() {
        if (!MidPlugin.instance.dataFolder.exists()) MidPlugin.instance.dataFolder.mkdir()
        if (!file.exists()) {
            file.createNewFile()
            save()
        } else {
            try {
                data = gson.fromJson(file.readText(), DataStructure::class.java)
            } catch (e: Exception) {
                MidPlugin.instance.logger.warning("PluginData.load(): $e")
            }
        }
    }

    fun save() {
        file.writeText(gson.toJson(data))
    }
}