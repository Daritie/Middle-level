package me.darity.mid.menu

import me.darity.mid.MidPlugin
import me.darity.mid.module.DestroyingBows
import me.darity.mid.module.PlayersDecorations
import me.darity.mid.util.sugar.*
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.*

class ControlBoardMenu(val playerId: UUID) : InventoryMenu(3*9, Text.text("Меню")) {
    companion object {
        private val SHOWING_PARTICLES_NAME = Text.text("Частицы").decoration(TextDecoration.ITALIC, false)
            .appendSpace() + Text.text("включены").color(NamedTextColor.GREEN)
        private val HIDING_PARTICLES_NAME = Text.text("Частицы").decoration(TextDecoration.ITALIC, false)
            .appendSpace() + Text.text("выключены").color(NamedTextColor.RED)
    }

    var tpsItem: ButtonData
    var decorationsItem: ButtonData

    init {
        tpsItem = addButton(
            ItemStack.of(Material.CLOCK).apply {
                itemMeta = itemMeta.apply {
                    displayName(Text.empty())
                }
            },
            10
        ).second

        decorationsItem = addButton(
            ItemStack.of(Material.PHANTOM_MEMBRANE).apply {
                itemMeta = itemMeta.apply {
                    displayName(Text.empty())
                }
            },
            12
        ) { event, _ ->
            PlayersDecorations.switchDecorations(event.view.player.uniqueId)
            updateParticlesItem()
            updateContent()
        }.second

        addButton(
            ItemStack.of(Material.BOW).apply {
                itemMeta = itemMeta.apply {
                    displayName(Text.text("Луки").decoration(TextDecoration.ITALIC, false))
                }
            },
            14
        ) { event, _ ->
            val inv = BowsMenu(this).inventory
            val player = event.view.player as Player
            scheduler.runTask(MidPlugin.instance, Runnable {
                player.takeIf { it.isValid }?.openInventory(inv)
            })
        }

        addButton(
            ItemStack.of(Material.ZOMBIE_HEAD).apply {
                itemMeta = itemMeta.apply {
                    displayName(Text.text("Патрули").decoration(TextDecoration.ITALIC, false))
                }
            },
            16
        ) { event, _ ->
            val player = event.view.player as Player
            scheduler.runTask(MidPlugin.instance, Runnable {
                player.takeIf { it.isValid }?.openInventory(PatrolsMenu(player.uniqueId, this).inventory)
            })
        }

        updateContent()
    }

    override fun updater() {
        updateTpsItem(MidPlugin.instance.server.tps.first())
        updateParticlesItem()
        updateContent()
    }

    private fun updateTpsItem(tps: Double) {
        tpsItem.item.apply {
            itemMeta = itemMeta.apply {
                displayName(
                    Text.text(
                        MidPlugin.instance.config.getString("menus.tps.format")?.format(Locale.US, tps)
                            ?: "конфига нема $tps")
                        .style(PLAIN_TEXT_STYLE)
                )
            }
        }
    }

    private fun updateParticlesItem() {
        decorationsItem.item.apply {
            itemMeta = itemMeta.apply {
                displayName(
                    if (playerId in PlayersDecorations.enabledNimbuses)
                        SHOWING_PARTICLES_NAME
                    else
                        HIDING_PARTICLES_NAME
                )
            }
        }
    }
}