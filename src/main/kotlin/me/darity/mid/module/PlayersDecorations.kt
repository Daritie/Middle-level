package me.darity.mid.module

import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import me.darity.mid.util.CommandRegistrar
import me.darity.mid.MidPlugin
import me.darity.mid.util.PluginModule
import me.darity.mid.util.sugar.*
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector
import java.util.UUID
import kotlin.math.*

object PlayersDecorations : PluginModule {
    private const val RADIUS = 0.5
    private const val REVOLUTIONS_PER_SECOND = 0.5
    private const val SPAWN_PERIOD = 3L
    private const val VERTICES_COUNT = 3
    private const val LOCATION_OFFSET = 1.85

    private const val DELTA_ANGLE: Double = (REVOLUTIONS_PER_SECOND * SPAWN_PERIOD * 2 * PI) / 20
    private const val VERTICES_STEP: Double = 2 * PI / VERTICES_COUNT
    private var angle = 0.0
    val enabledNimbuses = mutableSetOf<UUID>()

    override fun init(plugin: Plugin) {
        scheduler.runTaskTimer(plugin, 0, SPAWN_PERIOD) { spawnNimbuses() }
        super.init(plugin)
    }

    private fun spawnNimbuses() {
        if (enabledNimbuses.isEmpty()) return
        val verts = mutableListOf<Vector>()
        for (v in 0..VERTICES_COUNT) {
            val a = angle + VERTICES_STEP * v
            verts.add(Vector(RADIUS*cos(a), LOCATION_OFFSET, RADIUS*sin(a)))
        }
        angle = (angle + DELTA_ANGLE) % (2 * PI)
        enabledNimbuses.toSet().forEach { uuid ->
            MidPlugin.instance.server.getPlayer(uuid)?.let {
                for (v in verts) {
                    it.location.world.spawnParticle(Particle.DRIPPING_LAVA, it.location.add(v), 1)
                }
            } ?: enabledNimbuses.remove(uuid)
        }
    }

    fun showDecorations(uuid: UUID) = enabledNimbuses.add(uuid)
    fun hideDecorations(uuid: UUID) = enabledNimbuses.remove(uuid)
    fun switchDecorations(uuid: UUID) {
        if (uuid in enabledNimbuses)
            enabledNimbuses.remove(uuid)
        else
            enabledNimbuses.add(uuid)
    }

    @CommandRegistrar(aliases = ["particles"])
    private fun particlesCommand(name: String): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal(name)
            .then(
                Commands.literal("show")
                    .executes { ctx ->
                        showDecorations((ctx.source.sender as Player).uniqueId)
                        1
                    }
            )
            .then(
                Commands.literal("hide")
                    .executes { ctx ->
                        hideDecorations((ctx.source.sender as Player).uniqueId)
                        1
                    }
            )
            .build()
    }
}