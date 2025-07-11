package me.darity.mid.menu

import me.darity.mid.util.sugar.Text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class ConfirmMenu(title: Text, val confirmButtonTitle: Text, parent: InventoryMenu, val onConfirm: (InventoryClickEvent) -> Unit) : InventoryMenu(9, title, parent) {
    private val confirmButton = addButton(
        ItemStack.of(Material.GREEN_CONCRETE).apply {
            itemMeta = itemMeta.apply {
                displayName(confirmButtonTitle)
            }
        },
        2
    ) { event, _ ->
        inventory.close()
        openParent(event.view.player as Player)
        onConfirm(event)
    }

    private val cancelButton = addButton(
        ItemStack.of(Material.RED_CONCRETE).apply {
            itemMeta = itemMeta.apply {
                displayName(Text.text("Отмена").color(NamedTextColor.RED))
            }
        },
        6
    ) { event, _ ->
        inventory.close()
        openParent(event.view.player as Player)
    }
}