package me.darity.mid.module

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import me.darity.mid.util.CommandRegistrar
import me.darity.mid.menu.InventoryMenu
import me.darity.mid.util.PluginModule
import me.darity.mid.util.sugar.*
import org.bukkit.entity.Player

object InventorySpy : PluginModule {
    @CommandRegistrar(aliases = ["invsee"])
    private fun invseeCommand(name: String): LiteralCommandNode<CommandSourceStack> {
        fun openInv(ctx: CommandContext<CommandSourceStack>, _target: Player? = null): Int {
            val sender = (ctx.source.sender as? Player) ?: return 0
            val target = _target ?: sender
            val inv = InventoryMenu(5*9, Text.text("Инвентарь игрока ${target.name}")).apply { clickable = false }
                .inventory
            inv.contents = target.inventory.contents
            sender.openInventory(inv)
            return 1
        }

        return Commands.literal(name)
            .executes { ctx ->
                openInv(ctx)
            }
            .then(
                Commands.argument("player", ArgumentTypes.player())
                .executes { ctx ->
                    openInv(ctx, ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source).first())
                })
            .build()
    }
}