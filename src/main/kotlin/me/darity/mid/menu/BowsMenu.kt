package me.darity.mid.menu

import me.darity.mid.module.DestroyingBows
import me.darity.mid.util.sugar.Text
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class BowsMenu(parent: InventoryMenu) : InventoryMenu(2 * 9, Text.text("Луки"), parent) {
    init {
        addButton(
            ItemStack.of(Material.BOW).apply {
                itemMeta = itemMeta.apply {
                    displayName(Text.text("Обычный лук"))
                }
            },
            0
        ) { event, _ -> DestroyingBows.giveBow(event.view.player as Player, DestroyingBows.WITHOUT_TYPE) }

        addButton(
            ItemStack.of(Material.BOW).apply {
                itemMeta = itemMeta.apply {
                    displayName(Text.text("Ломающий лук"))
                }
            },
            1
        ) { event, _ -> DestroyingBows.giveBow(event.view.player as Player, DestroyingBows.BREAK_TYPE) }

        addButton(
            ItemStack.of(Material.BOW).apply {
                itemMeta = itemMeta.apply {
                    displayName(Text.text("Взрывающий лук"))
                }
            },
            2
        ) { event, _ -> DestroyingBows.giveBow(event.view.player as Player, DestroyingBows.EXPLOSIVE_TYPE) }

        addButton(
            ItemStack.of(Material.SPECTRAL_ARROW).apply {
                itemMeta = itemMeta.apply {
                    displayName(Text.text("Назад"))
                }
            },
            9
        ) { event, _ -> parent.openParent(event.view.player as Player) }
    }
}