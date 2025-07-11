package me.darity.mid.module

import me.darity.mid.PluginData
import me.darity.mid.util.PluginModule
import me.darity.mid.util.SpawnRegion
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent

object SpawnProtect : PluginModule {
    private val spawnRegion = SpawnRegion.regionFromConfig()

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        protectSpawn(event, event.block.location, event.player)
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        protectSpawn(event, event.block.location, event.player)
    }

    private fun protectSpawn(event: Cancellable, location: Location, player: Player) {
        if (spawnRegion.world?.let { location.world?.name == it.name } != false
            && spawnRegion.box.contains(location.toVector())
            && player.uniqueId !in PluginData.spawnOwners
        ) event.isCancelled = true
    }
}