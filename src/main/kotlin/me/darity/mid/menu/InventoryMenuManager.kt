package me.darity.mid.menu

import me.darity.mid.util.sugar.runTaskTimer
import me.darity.mid.util.sugar.scheduler
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.plugin.Plugin

object InventoryMenuManager : Listener {
    val menus = mutableSetOf<InventoryMenu>()

    fun init(plugin: Plugin) {
        scheduler.runTaskTimer(plugin, 0, 20) { timer() }
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    private fun timer() {
        for (menu in menus.toSet()) {
            menu.updater()
            checkViewers(menu)
        }
    }

    private fun checkViewers(menu: InventoryMenu) {
        fun rec(menu: InventoryMenu, stack: Int = 0) {
            val children = menu.children
            if (children.size != 0)
                children.toSet().forEach { rec(it, stack+1) }
            if (menu.inventory.viewers.size == 0 && menu.children.size == 0) {
                menu.parent?.children?.remove(menu)
                menus.remove(menu)
            }
        }
        rec(menu)
    }

    private fun getHolder(event: InventoryInteractEvent): InventoryMenu? = (event.inventory.holder as? InventoryMenu)

    @EventHandler
    private fun onInventoryClick(event: InventoryClickEvent) {
        getHolder(event)?.clickHandler(event)
    }

    @EventHandler
    private fun onInventoryDrag(event: InventoryDragEvent) {
        getHolder(event)?.dragHandler(event)
    }

}