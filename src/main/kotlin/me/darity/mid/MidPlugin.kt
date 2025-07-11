package me.darity.mid

import me.darity.mid.menu.InventoryMenuManager
import me.darity.mid.module.*
import me.darity.mid.util.PlayerData
import me.darity.mid.util.PlayerDataManager
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

class MidPlugin : JavaPlugin() {
    companion object {
        lateinit var instance: MidPlugin
        val playersData = mutableMapOf<UUID, PlayerData>()
    }

    override fun onEnable() {
        instance = this
        saveDefaultConfig()
        PluginData.load()
        PlayerDataManager.init(this)
        InventoryMenuManager.init(this)

        SpawnProtect.init(this) // Задания 1-2
        BasicCommands.init(this) // Задание 3
        InventorySpy.init(this) // Задание 4
        LomaiBlockScoreboard.init(this) // Задания 5, 8
        DestroyingBows.init(this) // Задания 6-7
        PlayersDecorations.init(this) // Задание 9
        PatrolZombie.init(this) // Задание 10
        MapImages.init(this) // Задание 11
        ControlBoard.init(this) // Задание 12
        Mention.init(this) // Задание 13
        WorldEditModule.init(this) // Задания 14-15
    }
}