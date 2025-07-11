package me.darity.mid.util.sugar

import net.kyori.adventure.text.Component

typealias Text = Component
operator fun Component.plus(other: Component) = append(other)