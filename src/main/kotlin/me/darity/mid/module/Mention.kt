package me.darity.mid.module

import io.papermc.paper.event.player.AsyncChatEvent
import me.darity.mid.util.PluginModule
import me.darity.mid.util.sugar.Text
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.Sound.Source
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import java.util.regex.Pattern

object Mention : PluginModule {
    @EventHandler
    private fun onChat(event: AsyncChatEvent) {
        event.renderer { player, displayName, message, viewer ->
            Text.empty()
                .append(displayName)
                .append(Text.text(": "))
                .append(
                    message.replaceText(
                        TextReplacementConfig.builder()
                            .match(Pattern.compile("""@(\w+)"""))
                            .replacement { result, builder ->
                                val mentionedPlayer = Bukkit.getPlayer(result.group(1))
                                val mention = builder
                                    .hoverEvent(HoverEvent.showText(Text.text("ЛКМ")))
                                    .clickEvent(ClickEvent.suggestCommand("@${ mentionedPlayer?.name ?: result.group(1) }, "))
                                mention.color(
                                    when (mentionedPlayer) {
                                        player -> {
                                            player.playSound(Sound.sound(
                                                Key.key("entity.player.levelup"),
                                                Source.MASTER,
                                                1f,
                                                1f
                                            ))
                                            NamedTextColor.GOLD
                                        }
                                        else -> NamedTextColor.AQUA
                                    }
                                ).build()
                            }
                            .build()
                    )
                )
        }
    }
}