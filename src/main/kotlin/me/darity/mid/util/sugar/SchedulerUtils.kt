package me.darity.mid.util.sugar

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.scheduler.BukkitTask

val scheduler: BukkitScheduler get() = Bukkit.getScheduler()

fun BukkitScheduler.runTaskLater(plugin: Plugin, delay: Long, task: () -> Unit): BukkitTask =
    runTaskLater(plugin, task, delay)

fun BukkitScheduler.runTaskTimer(plugin: Plugin, delay: Long, period: Long, task: () -> Unit): BukkitTask =
    runTaskTimer(plugin, task, delay, period)