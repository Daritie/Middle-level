package me.darity.mid.menu

import me.darity.mid.MidPlugin
import me.darity.mid.module.PatrolZombie
import me.darity.mid.util.sugar.PLAIN_TEXT_STYLE
import me.darity.mid.util.sugar.Text
import me.darity.mid.util.sugar.runTaskTimer
import me.darity.mid.util.sugar.scheduler
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.*

class PatrolsMenu(val playerId: UUID, parent: InventoryMenu) : InventoryMenu(5 * 9, Text.text("Патрули"), parent) {
    val backButton = addButton(
        ItemStack.of(Material.SPECTRAL_ARROW).apply {
            itemMeta = itemMeta.apply {
                displayName(Text.text("Назад"))
            }
        },
        36
    ) { event, _ ->
        (event.inventory.holder as InventoryMenu).openParent(event.view.player as Player)
    }

    private fun confirmRemove(patrolName: String, player: Player) {
        val inv = ConfirmMenu(
            Text.text("Подтверждение удаления патруля"),
            Text.text("Удалить патруль").color(NamedTextColor.RED),
            this
        ) { _ ->
            PatrolZombie.removePatrol(player, patrolName)
        }.inventory
        player.openInventory(inv)
    }

    override fun updater() {
        buttons = hashMapOf(backButton)
        var slot = 0
        val patrols = PatrolZombie.patrols.filter { (_, patrolData) -> patrolData.ownerId == playerId }
            .forEach { (name, patrolData) ->
                if (slot < size-8) {
                    val location = patrolData.zombieId?.let { MidPlugin.instance.server.getEntity(it)?.location }
                    val locationText = location?.let { "x: %.1f, y: %.1f, z: %.1f".format(Locale.US, it.x, it.y, it.z) }
                        ?: "x: ?, y: ?, z: ?"
                    val pointsCount = patrolData.points.size
                    addButton(
                        ItemStack(Material.ZOMBIE_HEAD).apply {
                            itemMeta = itemMeta.apply {
                                displayName(Text.text("Патруль $name"))
                            }
                            lore(
                                listOf(
                                    Text.text(locationText).style(PLAIN_TEXT_STYLE),
                                    Text.text("Кол-во точек: $pointsCount").style(PLAIN_TEXT_STYLE)
                                )
                            )
                        },
                        slot
                    ) { event, _ -> confirmRemove(name, event.view.player as Player) }
                    slot++
                }
            }
        if (slot < size-8)
            for (i in slot..<size-8)
                inventory.setItem(i, null)

        updateContent()
    }
}