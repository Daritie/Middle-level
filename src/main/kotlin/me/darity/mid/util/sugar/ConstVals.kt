package me.darity.mid.util.sugar

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration

val PLAIN_TEXT_STYLE = Style.style()
    .color(NamedTextColor.WHITE)
    .decoration(TextDecoration.ITALIC, false)