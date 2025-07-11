package me.darity.mid.util.sugar

import org.bukkit.util.Vector

operator fun Vector.plus(other: Vector): Vector = this.add(other)
operator fun Vector.minus(other: Vector): Vector = this.subtract(other)