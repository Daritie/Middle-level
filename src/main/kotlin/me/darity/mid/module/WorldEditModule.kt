package me.darity.mid.module

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.function.mask.RegionMask
import com.sk89q.worldedit.math.Vector3
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.regions.EllipsoidRegion
import com.sk89q.worldedit.regions.Region
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import me.darity.mid.MidPlugin
import me.darity.mid.util.CommandRegistrar
import me.darity.mid.util.PluginModule
import me.darity.mid.util.sugar.Text
import me.darity.mid.util.sugar.runTaskTimer
import me.darity.mid.util.sugar.scheduler
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.block.BlockState
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector
import java.util.UUID

object WorldEditModule : PluginModule {
    data class RegionCorners(
        var first: Location? = null,
        var second: Location? = null
    )

    private val PARTICLES_COUNT = 10
    private val cuboidEdges = listOf(
        0b000 to 0b001, 0b000 to 0b100, 0b101 to 0b001, 0b101 to 0b100,
        0b010 to 0b011, 0b010 to 0b110, 0b111 to 0b011, 0b111 to 0b110,
        0b000 to 0b010, 0b001 to 0b011, 0b100 to 0b110, 0b101 to 0b111
    )

    private val REQUIRE_TWO_CORNERS_MSG = Text.text("Выделите 2 угла региона").color(NamedTextColor.RED)

    private val WAND_ITEM_KEY = NamespacedKey(MidPlugin.instance, "wand")
    private val WAND_ITEM = ItemStack(Material.STICK).apply {
        itemMeta = itemMeta.apply {
            persistentDataContainer.set(
                WAND_ITEM_KEY,
                PersistentDataType.STRING,
                "wand"
            )
        }
    }

    private val regions = mutableMapOf<UUID, RegionCorners>() // Владелец, углы

    override fun init(plugin: Plugin) {
        super.init(plugin)
        scheduler.runTaskTimer(plugin, 0, 10) { drawRegionBorder() }
    }

    private fun drawRegionBorder() {
        val server = MidPlugin.instance.server
        for ((uuid, corners) in regions.toMap()) {
            val player = server.getPlayer(uuid)
            if (player == null) {
                regions.remove(uuid)
                continue
            }
            val (corner1, corner2) = corners
            if (corner1 == null || corner2 == null) continue

            val x = arrayOf(Math.min(corner1.x, corner2.x), Math.max(corner1.x, corner2.x)+1)
            val y = arrayOf(Math.min(corner1.y, corner2.y), Math.max(corner1.y, corner2.y)+1)
            val z = arrayOf(Math.min(corner1.z, corner2.z), Math.max(corner1.z, corner2.z)+1)

            val vertices = mutableListOf<Vector>()
            for (i in 0..7) {
                val xIndex1 = (i shr 2) and 1
                val yIndex1 = (i shr 1) and 1
                val zIndex1 = i and 1
                vertices.add(Vector(x[xIndex1],y[yIndex1],z[zIndex1]))
            }

            for ((first, second) in cuboidEdges) {
                drawParticlesLine(player, vertices[first], vertices[second])
            }
        }
    }

    private fun drawParticlesLine(player: Player, firstVector: Vector, secondVector: Vector) {
        for (i in 0..PARTICLES_COUNT) {
            val k = i/PARTICLES_COUNT.toDouble()
            player.spawnParticle(
                Particle.WAX_ON,
                firstVector.x + (secondVector.x-firstVector.x)*k,
                firstVector.y + (secondVector.y-firstVector.y)*k,
                firstVector.z + (secondVector.z-firstVector.z)*k,
                1
            )
        }
    }

    @EventHandler
    private fun onClick(event: PlayerInteractEvent) {
        val itemType = event.item
            ?.itemMeta
            ?.persistentDataContainer
            ?.get(WAND_ITEM_KEY, PersistentDataType.STRING)
        if (itemType == "wand") {
            val player = event.player
            val location = event.clickedBlock?.location ?: return
            val corners = regions.getOrPut(player.uniqueId) { RegionCorners() }
            event.isCancelled = true
            when (event.action) {
                Action.RIGHT_CLICK_BLOCK -> corners.first = location
                Action.LEFT_CLICK_BLOCK -> corners.second = location
                else -> return
            }
        }
    }

    @EventHandler
    private fun onSneak(event: PlayerToggleSneakEvent) {
        val player = event.player
        if (event.isSneaking) {
            val itemType = player.inventory.itemInMainHand
                .itemMeta
                ?.persistentDataContainer
                ?.get(WAND_ITEM_KEY, PersistentDataType.STRING)
            if (itemType == "wand") {
                regions.remove(player.uniqueId)
            }
        }
    }

    @CommandRegistrar(aliases = ["#wand"])
    private fun giveWand(name: String): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal(name)
            .executes { ctx ->
                (ctx.source.sender as? Player)
                    ?.inventory?.addItem(WAND_ITEM)
                    ?: return@executes 0
                1
            }
            .build()
    }

    private fun getCuboidRegion(player: Player, giveErrorMessage: Boolean = false): Region? {
        val corners = regions[player.uniqueId]
        val pos1 = corners?.first
        val pos2 = corners?.second
        if (pos1 == null || pos2 == null) {
            if (giveErrorMessage) player.sendMessage(REQUIRE_TWO_CORNERS_MSG)
            return null
        }
        return CuboidRegion(
            BukkitAdapter.asBlockVector(pos1),
            BukkitAdapter.asBlockVector(pos2)
        )
    }

    @CommandRegistrar(aliases = ["#set"])
    private fun setRegionCommand(name: String): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal(name)
            .then(Commands.argument("block", ArgumentTypes.blockState())
                .executes { ctx ->
                    val player = (ctx.source.sender as? Player)
                    val region = player?.let {
                        getCuboidRegion(it)
                    } ?: return@executes 0
                    val block = BukkitAdapter.adapt(ctx.getArgument("block", BlockState::class.java).blockData)
                    WorldEdit.getInstance().newEditSessionBuilder()
                        .world(BukkitAdapter.adapt(player.world))
                        .fastMode(true)
                        .build()
                        .apply {
                            setBlocks(region, block)
                            commit()
                        }
                    1
                })
            .build()
    }

    @CommandRegistrar(aliases = ["#sphere"])
    private fun setSphereCommand(name: String): LiteralCommandNode<CommandSourceStack> {
        fun setSphere(ctx: CommandContext<CommandSourceStack>, radius: Double): Int {
            val player = (ctx.source.sender as? Player) ?: return 0
            val block = BukkitAdapter.adapt(ctx.getArgument("block", BlockState::class.java).blockData)
            val sphere = EllipsoidRegion(
                BukkitAdapter.asBlockVector(player.location.add(Vector(0.0, radius+1, 0.0))),
                Vector3.at(radius, radius, radius)
            )
            WorldEdit.getInstance().newEditSessionBuilder()
                .world(BukkitAdapter.adapt(player.world))
                .fastMode(true)
                .build()
                .apply {
                    getCuboidRegion(player)?.let { mask = RegionMask(it) }
                    setBlocks(sphere as Region, block)
                    commit()
                }
            return 1
        }

        return Commands.literal(name)
            .then(Commands.argument("block", ArgumentTypes.blockState())
                .executes { ctx ->
                    setSphere(ctx, 2.0)
                }
                .then(Commands.argument("radius", DoubleArgumentType.doubleArg(0.5))
                    .executes { ctx ->
                        val radius = ctx.getArgument("radius", Double::class.java)
                        setSphere(ctx, radius)
                    }
                )
            )
            .build()
    }
}