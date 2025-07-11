package me.darity.mid.util

import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import me.darity.mid.MidPlugin
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

/**
 * Объекты с этим интерфейсом регистрируются как слушатели ивентов и могут регистрировать свои команды
 */
interface PluginModule : Listener {
    fun init(plugin: Plugin) {
        registerCommands()
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    private fun registerCommands() {
        val annotationClass = CommandRegistrar::class.java
        MidPlugin.instance.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
            this::class.java.declaredMethods
                .filter { it.isAnnotationPresent(annotationClass) }
                .forEach { func ->
                    val annotation = func.getAnnotation(annotationClass)
                    val aliases = annotation.aliases.plus(MidPlugin.instance.config.getStringList(annotation.pathToAliases))
                    func.isAccessible = true
                    aliases.forEach {
                        (func.invoke(this, it) as? LiteralCommandNode<CommandSourceStack>)
                            ?.let { commandNode ->
                                commands.registrar().register(commandNode)
                            }
                    }
                }
        }
    }
}