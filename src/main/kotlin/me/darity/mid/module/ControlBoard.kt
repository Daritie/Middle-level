package me.darity.mid.module

import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import me.darity.mid.menu.ControlBoardMenu
import me.darity.mid.util.CommandRegistrar
import me.darity.mid.util.PluginModule
import org.bukkit.entity.Player

object ControlBoard : PluginModule {
    @CommandRegistrar(aliases = ["info"])
    private fun openingCommand(name: String): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal(name)
            .executes { ctx ->
                val player = (ctx.source.sender as? Player) ?: return@executes 0
                val holder = ControlBoardMenu(player.uniqueId)
                player.openInventory(holder.inventory)
                1
            }
            .build()
    }
}