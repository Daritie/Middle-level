package me.darity.mid.menu

import me.darity.mid.MidPlugin
import me.darity.mid.util.sugar.Text
import me.darity.mid.util.sugar.scheduler
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

open class InventoryMenu(val size: Int, val title: Text, val parent: InventoryMenu? = null) : InventoryHolder {
    companion object {
        val ITEM_UUID_KEY = NamespacedKey(MidPlugin.instance, "menu_item")
    }

    data class ButtonData(
        var item: ItemStack,
        var slot: Int,
        val onClick: ((InventoryClickEvent, ButtonData) -> Unit)?
    )

    val uniqueId = UUID.randomUUID()
    var clickable: Boolean = true
    var buttons = hashMapOf<UUID, ButtonData>()
    val children = mutableSetOf<InventoryMenu>()
    private var inventory: Inventory = Bukkit.createInventory(this, size, title)

    init {
        scheduler.runTask(MidPlugin.instance, Runnable {
            parent?.children?.add(this)
            InventoryMenuManager.menus.add(this)
        })
    }

    override fun getInventory(): Inventory {
        return inventory
    }

    fun addButton(item: ItemStack, slot: Int, onClick: ((InventoryClickEvent, ButtonData) -> Unit)? = null): Pair<UUID, ButtonData> {
        val buttonData = ButtonData(item, slot, onClick)
        val uuid = UUID.randomUUID()
        item.apply {
            itemMeta = itemMeta.apply {
                persistentDataContainer.set(ITEM_UUID_KEY, PersistentDataType.STRING, uuid.toString())
            }
        }
        buttons[uuid] = buttonData
        inventory.setItem(slot, item)
        return uuid to buttonData
    }

    fun updateContent() {
        buttons.forEach { (_, buttonData) ->
            inventory.setItem(buttonData.slot, buttonData.item)
        }
    }

    fun updateContent(buttonData: ButtonData) {
        inventory.setItem(buttonData.slot, buttonData.item)
    }

    fun openParent(player: Player) {
        scheduler.runTask(MidPlugin.instance, Runnable {
            parent?.inventory?.let { parent -> player.takeIf { it.isValid }?.openInventory(parent) }
        })
    }

    fun clickHandler(event: InventoryClickEvent) {
        if (!clickable) event.isCancelled = true
        event.currentItem
            ?.itemMeta
            ?.persistentDataContainer
            ?.get(ITEM_UUID_KEY, PersistentDataType.STRING)
            ?.let {
                val btn = buttons[runCatching { UUID.fromString(it) }.getOrNull()] ?: return
                event.isCancelled = true
                btn.onClick?.let { it(event, btn) }
            }
    }

    fun dragHandler(event: InventoryDragEvent) {
        if (!clickable) event.isCancelled = true
    }

    open fun updater() {}
}