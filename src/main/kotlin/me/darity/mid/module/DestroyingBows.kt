package me.darity.mid.module

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import me.darity.mid.module.LomaiBlockScoreboard.addPlayerPoints
import me.darity.mid.MidPlugin
import me.darity.mid.util.CommandRegistrar
import me.darity.mid.util.PluginModule
import me.darity.mid.util.sugar.*
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import java.util.UUID

object DestroyingBows : PluginModule {
    private val BOW_TYPE_KEY = NamespacedKey(MidPlugin.instance, "bow_type")
    private val ARROW_TYPE_KEY = NamespacedKey(MidPlugin.instance, "arrow_type")
    private val HAS_HIT_KEY = NamespacedKey(MidPlugin.instance, "has_hit")
    const val WITHOUT_TYPE = "none"
    const val BREAK_TYPE = "lomaiblock"
    const val EXPLOSIVE_TYPE = "tnt"

    private val arrowsWithTrack = mutableListOf<UUID>()

    override fun init(plugin: Plugin) {
        scheduler.runTaskTimer(plugin, 0, 1) { spawnTracks() }
        super.init(plugin)
    }

    private fun spawnTracks() {
        for (uuid in arrowsWithTrack) {
            val entity = (MidPlugin.instance.server.getEntity(uuid) as? Arrow)
            if (entity == null) {
                arrowsWithTrack.remove(uuid)
                continue
            }
            entity.world.spawnParticle(Particle.WAX_ON, entity.location, 1)
        }
    }

    @EventHandler
    private fun archery(event: EntityShootBowEvent) {
        val entity = event.projectile
        val stringType = PersistentDataType.STRING
        entity.persistentDataContainer.set(
            ARROW_TYPE_KEY,
            stringType,
            event.bow?.itemMeta?.persistentDataContainer?.get(
                BOW_TYPE_KEY,
                stringType
            ) ?: return)
        entity.persistentDataContainer.set(HAS_HIT_KEY, PersistentDataType.BYTE, 0)
        arrowsWithTrack.add(entity.uniqueId)
    }

    @EventHandler
    private fun onArrowInteract(event: ProjectileHitEvent) {
        val entity = (event.entity as? Arrow) ?: return
        val dataContainer = entity.persistentDataContainer
        val arrowType = dataContainer.get(ARROW_TYPE_KEY, PersistentDataType.STRING)
        val hasHit = dataContainer.get(HAS_HIT_KEY, PersistentDataType.BYTE)
        if (hasHit?.toInt() == 0) {
            dataContainer.set(HAS_HIT_KEY, PersistentDataType.BYTE, 1)
            arrowsWithTrack.remove(entity.uniqueId)
            val location = entity.location
            scheduler.runTaskLater(MidPlugin.instance, 1) {
                when (arrowType) {
                    BREAK_TYPE ->  {
                        val block = event.hitBlock.takeIf { it?.isEmpty == false } ?: return@runTaskLater
                        val player = (event.entity.shooter as? Player) ?: return@runTaskLater
                        addPlayerPoints(player, block)
                        block.breakNaturally(true)
                    }
                    EXPLOSIVE_TYPE -> location.createExplosion(entity,5.0f, true)
                }
            }
        }
    }

    @EventHandler
    private fun onArrowExplode(event: EntityExplodeEvent) {
        val entity = (event.entity as? Arrow) ?: return
        val type = entity.persistentDataContainer.get(ARROW_TYPE_KEY, PersistentDataType.STRING)
        if (type == EXPLOSIVE_TYPE) {
            val player = (entity.shooter as? Player) ?: return
            event.blockList().forEach {
                addPlayerPoints(player, it)
            }
        }
    }

    fun giveBow(player: Player, type: String) {
        val bow = ItemStack(Material.BOW).apply {
            if (type != WITHOUT_TYPE)
                itemMeta = itemMeta.apply {
                    persistentDataContainer.set(BOW_TYPE_KEY, PersistentDataType.STRING, type)
                }
        }
        player.inventory.addItem(bow)
    }

    @CommandRegistrar(aliases = ["bow"])
    private fun bowCommand(name: String): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal(name)
            .executes { ctx ->
                giveBow(
                    (ctx.source.sender as? Player) ?: return@executes 0,
                    BREAK_TYPE
                )
                1
            }
            .then(
                Commands.literal("tnt")
                .executes { ctx ->
                    giveBow(
                        (ctx.source.sender as? Player) ?: return@executes 0,
                        EXPLOSIVE_TYPE
                    )
                    1
                }
            )
            .build()
    }
}