package me.darity.mid

import org.bukkit.World
import org.bukkit.util.BoundingBox

data class SpawnRegion(
    val world: World?,
    val box: BoundingBox
) {
    companion object {
        fun regionFromConfig(): SpawnRegion {
            val config = MidPlugin.instance.config
            return SpawnRegion(
                config.getString("spawn.world")?.let { MidPlugin.instance.server.getWorld(it) },
                BoundingBox(
                    config.getDouble("spawn.pos1.x"),
                    config.getDouble("spawn.pos1.y"),
                    config.getDouble("spawn.pos1.z"),

                    config.getDouble("spawn.pos2.x"),
                    config.getDouble("spawn.pos2.y"),
                    config.getDouble("spawn.pos2.z")
                )
            )
        }
    }
}