package me.darity.mid.util

import io.papermc.paper.scoreboard.numbers.NumberFormat
import me.darity.mid.util.sugar.Text
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.RenderType

class PersonalScoreboard(val title: Text = Text.empty()) {
    companion object {
        const val EMPTY_SYMBOL = "\u200C"
        private var uniqueId = 0
        private val releasedIDs = mutableListOf<Int>()
        private val serializer = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build()
    }

    val lines = mutableMapOf<String,Int>()
    val scoreboard = Bukkit.getScoreboardManager().newScoreboard.apply {
        registerNewObjective(
            "main",
            Criteria.DUMMY,
            title,
            RenderType.INTEGER
        ).apply {
            displaySlot = DisplaySlot.SIDEBAR
            numberFormat(NumberFormat.blank())
        }
    }

    private fun getUniqueId(): Int {
        val uid = if (releasedIDs.isNotEmpty()) releasedIDs.removeAt(0)
        else uniqueId++
        if (uniqueId > 0xFFFFFF) throw IllegalStateException("Ого, да у тебя >16 000 000 строк")
        return uid
    }

    private fun idToEntry(id: Int): String {
        val text = Text.empty()
            .append(Text.text(EMPTY_SYMBOL).color(TextColor.color(0x6FF429)))
            .append(Text.text(EMPTY_SYMBOL).color(TextColor.color(id)))
        return serializer.serialize(text)
    }

    fun addNewLine(name: String, score: Int, text: Text? = null) {
        if (name in lines) return
        val objective = scoreboard.getObjective("main") ?: return
        val team = scoreboard.registerNewTeam(name)
        val id = getUniqueId()
        lines[name] = id
        val entry = idToEntry(id)
        team.addEntries(entry)
        objective.getScore(entry).score = score
        text?.let { team.prefix(it) }
    }

    fun removeLine(name: String) {
        val objective = scoreboard.getObjective("main") ?: return
        val id = lines.remove(name) ?: return
        objective.getScore(idToEntry(id)).resetScore()
        scoreboard.getTeam(name)?.unregister()
        releasedIDs.add(id)
    }

    fun setLineText(name: String, text: Text) {
        scoreboard.getTeam(name)?.prefix(text)
    }
}