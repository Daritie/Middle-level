package me.darity.mid.module

import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import me.darity.mid.MidPlugin
import me.darity.mid.util.CommandRegistrar
import me.darity.mid.PluginData
import me.darity.mid.util.PluginModule
import java.util.Locale

object BasicCommands : PluginModule {
    @CommandRegistrar(pathToAliases = "commands.mytps.names")
    private fun tpsCommand(name: String): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal(name)
            .executes { ctx ->
                val tps = MidPlugin.instance.server.tps.first()
                ctx.source.sender.sendMessage(
                    MidPlugin.instance.config.getString("commands.mytps.msg")?.format(Locale.US, tps)
                        ?: "конфига нема $tps"
                )
                return@executes 1
            }
            .build()
    }

    @CommandRegistrar(aliases = ["reloadconf"])
    private fun reloadCfg(name: String): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal(name)
            .executes { ctx ->
                MidPlugin.instance.reloadConfig()
                PluginData.load()
                return@executes 1
            }
            .build()
    }
}