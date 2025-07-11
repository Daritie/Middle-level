package me.darity.mid.util

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import java.util.UUID

object PlayerDataManager : Listener {
    private val playersData = mutableMapOf<UUID, PlayerData>()

    fun init(plugin: Plugin) {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    fun get(uuid: UUID): PlayerData {
        return playersData.getOrPut(uuid) { PlayerData() }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        playersData[event.player.uniqueId] = PlayerData()
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        playersData.remove(event.player.uniqueId)
    }
}