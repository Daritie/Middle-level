package me.darity.mid.module

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import me.darity.mid.util.CommandRegistrar
import me.darity.mid.MidPlugin
import me.darity.mid.util.PluginModule
import me.darity.mid.util.sugar.*
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Zombie
import org.bukkit.event.EventHandler
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.plugin.Plugin
import java.util.UUID

object PatrolZombie : PluginModule {
    private const val ZOMBIE_NAME = "Очень-очень опасный %s"
    private val EXISTING_PATROL_ERROR = Text.text("Патруль с таким именем уже существует").color(NamedTextColor.RED)
    private val NOT_EXISTING_PATROL_ERROR = Text.text("Нет патруля с таким именем").color(NamedTextColor.RED)
    private val NOT_OWNER_PATROL_ERROR = Text.text("Вы не владеете этим патрулём").color(NamedTextColor.RED)

    val patrols = hashMapOf<String, PatrolData>()

    data class PatrolData(
        val ownerId: UUID,
        var zombieId: UUID?,
        var points: MutableList<Location>,
        var currentPointIndex: Int = 0
    ) {
        fun getPoint(): Location? {
            if (points.isEmpty()) return null
            if (currentPointIndex >= points.size) {
                currentPointIndex = 0
                return points[0]
            }
            return points[currentPointIndex]
        }
    }

    override fun init(plugin: Plugin) {
        scheduler.runTaskTimer(plugin, 0, 3) { zombieBrains() }
        super.init(plugin)
    }

    private fun getDestinationPoint(zombie: Zombie, data: PatrolData): Location? {
        val point = data.getPoint() ?: return null
        if (zombie.location.distance(point) <= 1) data.currentPointIndex++
        return data.getPoint()
    }

    private fun zombieBrains() {
        patrols.forEach { (name, patrolData) ->
            (patrolData.zombieId?.let {MidPlugin.instance.server.getEntity(it) } as? Zombie)?.apply {
                target = location.getNearbyLivingEntities(5.0).filter {
                    if (it is Player) patrolData.ownerId != it.uniqueId
                    else patrolData.zombieId != it.uniqueId
                }.randomOrNull()
                if (target == null) getDestinationPoint(this, patrolData)?.let { pathfinder.moveTo(it) }
            } ?: run {
                patrolData.zombieId = spawnZombie(name, patrolData.getPoint()!!)
            }
        }
    }

    private fun spawnZombie(patrolName: String, location: Location): UUID {
        location.world.spawnEntity(location, EntityType.ZOMBIE, false).apply {
            customName(Text.text(ZOMBIE_NAME.format(patrolName)))
            isCustomNameVisible = true
            isInvulnerable = true
            return uniqueId
        }
    }

    fun addPatrol(player: Player, name: String) {
        if (name in patrols) {
            player.sendMessage(EXISTING_PATROL_ERROR)
            return
        }
        val location = player.location
        patrols[name] = PatrolData(player.uniqueId, spawnZombie(name, location), mutableListOf(location))
    }

    fun pointPatrol(player: Player, name: String) {
        val data = patrols[name]
        if (data == null) player.sendMessage(NOT_EXISTING_PATROL_ERROR)
        else if (data.ownerId != player.uniqueId) player.sendMessage(NOT_OWNER_PATROL_ERROR)
        else data.points.add(player.location)
    }

    fun removePatrol(player: Player, name: String) {
        val data = patrols[name]
        if (data == null) player.sendMessage(NOT_EXISTING_PATROL_ERROR)
        else if (data.ownerId != player.uniqueId) player.sendMessage(NOT_OWNER_PATROL_ERROR)
        else {
            patrols.remove(name)
            data.zombieId?.let { MidPlugin.instance.server.getEntity(it)?.remove() }
        }
    }

    @EventHandler
    fun onDisableServer(event: PluginDisableEvent) {
        patrols.forEach { (_, data) ->
            data.zombieId?.let { MidPlugin.instance.server.getEntity(it)?.remove() }
        }
    }

    @CommandRegistrar(aliases = ["patrol"])
    private fun patrolCommand(name: String): LiteralCommandNode<CommandSourceStack> {
        fun actionsBranch(action: String): LiteralArgumentBuilder<CommandSourceStack> {
            return Commands.literal(action)
                .then(
                    Commands.argument("name", StringArgumentType.string())
                        .executes { ctx ->
                            val name = ctx.getArgument("name", String::class.java)
                            val player = (ctx.source.sender as? Player) ?: return@executes 0
                            when (action) {
                                "add" -> addPatrol(player, name)
                                "point" -> pointPatrol(player, name)
                                "remove" -> removePatrol(player, name)
                            }
                            1
                        }
                )
        }

        return Commands.literal(name)
            .then(actionsBranch("add"))
            .then(actionsBranch("point"))
            .then(actionsBranch("remove"))
            .build()
    }
}