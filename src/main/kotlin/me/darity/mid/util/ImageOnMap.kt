package me.darity.mid.util

import org.bukkit.entity.Player
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import java.awt.Image

class ImageOnMap(private val image: Image) : MapRenderer() {
    override fun render(map: MapView, canvas: MapCanvas, player: Player) {
        canvas.drawImage(0, 0, image)
    }
}