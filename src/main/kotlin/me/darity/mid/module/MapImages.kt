package me.darity.mid.module

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import me.darity.mid.MidPlugin
import me.darity.mid.util.CommandRegistrar
import me.darity.mid.util.ImageOnMap
import me.darity.mid.util.PluginModule
import me.darity.mid.util.sugar.*
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapPalette
import java.net.URL
import javax.imageio.ImageIO

object MapImages : PluginModule {
    private val IMAGE_LOAD_ERROR = Text.text("Не удалось загрузить изображение").color(NamedTextColor.RED)

    private fun loadImage(player: Player, url: String) {
        scheduler.runTaskAsynchronously(MidPlugin.instance, Runnable {
            try {
                val image = ImageIO.read(URL(url))
                scheduler.runTask(MidPlugin.instance, Runnable {
                    if (!player.isValid) return@Runnable
                    val map = Bukkit.createMap(player.world).apply {
                        addRenderer(ImageOnMap(MapPalette.resizeImage(image)))
                    }
                    player.inventory.addItem(ItemStack.of(Material.FILLED_MAP).apply {
                        val meta = (itemMeta as MapMeta)
                        meta.mapView = map
                        itemMeta = meta
                    })
                })
            } catch (e: Exception) {
                scheduler.runTask(MidPlugin.instance, Runnable {
                    if (player.isValid) player.sendMessage(IMAGE_LOAD_ERROR)
                })
            }
        })
    }

    @CommandRegistrar(aliases = ["img"])
    private fun imageCommand(name: String): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal(name)
            .then(
                Commands.argument("url", StringArgumentType.greedyString())
                    .executes { ctx ->
                        val player = (ctx.source.sender as? Player) ?: return@executes 0
                        val url = ctx.getArgument("url", String::class.java)
                        loadImage(player, url)
                        1
                    }
            )
            .build()
    }
}