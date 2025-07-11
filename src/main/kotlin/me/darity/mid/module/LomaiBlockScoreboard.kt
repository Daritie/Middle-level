package me.darity.mid.module

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import me.darity.mid.MidPlugin
import me.darity.mid.util.CommandRegistrar
import me.darity.mid.util.PersonalScoreboard
import me.darity.mid.util.PlayerDataManager
import me.darity.mid.util.PluginModule
import me.darity.mid.util.sugar.*
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.RenderType

object LomaiBlockScoreboard : PluginModule {
    private val PERSONAL_STAT_TEXT = "Очки за блоки: %s"

    private val playersScores = Bukkit.getScoreboardManager().newScoreboard

    override fun init(plugin: Plugin) {
        playersScores.registerNewObjective(
            "playersScores",
            Criteria.DUMMY,
            Text.text("Кто больше понавыкапывал"),
            RenderType.INTEGER
        ).apply { displaySlot = DisplaySlot.SIDEBAR }
        super.init(plugin)
    }

    fun getBlockPoints(block: Block): Int {
        return when (block.type) {
            Material.AIR -> 0
            in Tag.DIRT.values -> 2
            Material.STONE -> 3
            in Tag.LOGS.values -> 4
            else -> 1
        }
    }

    fun addPlayerPoints(player: Player, block: Block) {
        val objective = playersScores.getObjective("playersScores") ?: return
        val playerData = PlayerDataManager.get(player.uniqueId)
        playerData.lomaiblockPoints += getBlockPoints(block)
        objective.getScore(player).score = playerData.lomaiblockPoints
        playerData.personalScoreboard?.setLineText("statBlocks", Text.text(PERSONAL_STAT_TEXT.format(playerData.lomaiblockPoints)))
    }

    private fun getDefaultScoreboard(points: Int = 0): PersonalScoreboard =
        PersonalScoreboard(Text.text("Персональный")).apply {
            addNewLine("statHeader",2, Text.text("Статистика:"))
            addNewLine("statBlocks",1, Text.text(PERSONAL_STAT_TEXT.format(points)))
        }

    @EventHandler
    private fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        PlayerDataManager.get(player.uniqueId).personalScoreboard = getDefaultScoreboard()
        player.scoreboard = playersScores
    }

    @EventHandler
    private fun onQuit(event: PlayerQuitEvent) {
        MidPlugin.playersData.remove(event.player.uniqueId)
    }

    @EventHandler
    private fun lomaiBlock(event: BlockBreakEvent) {
        if (!event.isCancelled) addPlayerPoints(event.player, event.block)
    }

    @CommandRegistrar(aliases = ["score"])
    private fun scoreCommand(name: String): LiteralCommandNode<CommandSourceStack> {
        fun action(ctx: CommandContext<CommandSourceStack>, action: String, amount: Int = 0): Int {
            val player = ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source)[0]
            val objective = playersScores.getObjective("playersScores") ?: return 0
            val score = objective.getScore(player)
            val playerData = PlayerDataManager.get(player.uniqueId)
            var points = playerData.lomaiblockPoints
            when (action) {
                "set" -> points = amount
                "add" -> points += amount
                "remove" -> points = 0
            }
            if (action == "remove") score.resetScore()
            else score.score = points
            playerData.personalScoreboard?.setLineText("statBlocks", Text.text(PERSONAL_STAT_TEXT.format(points)))
            playerData.lomaiblockPoints = points
            return 1
        }

        fun addSetBranch(action: String): LiteralArgumentBuilder<CommandSourceStack> {
            return Commands.literal(action)
                .then(
                    Commands.argument("player", ArgumentTypes.player())
                    .then(
                        Commands.argument("amount", IntegerArgumentType.integer())
                        .executes { ctx ->
                            action(ctx, action, ctx.getArgument("amount", Int::class.java))
                        }
                    )
                )
        }

        return Commands.literal(name)
            .then(addSetBranch("set"))
            .then(addSetBranch("add"))
            .then(
                Commands.literal("remove")
                .then(
                    Commands.argument("player", ArgumentTypes.player())
                    .executes { ctx -> action(ctx, "remove") }
                )
            )
            .then(
                Commands.literal("show")
                    .executes { ctx ->
                        val player = (ctx.source.sender as? Player) ?: return@executes 0
                        val data = PlayerDataManager.get(player.uniqueId)
                        val scoreboard = data.personalScoreboard ?: getDefaultScoreboard().also { data.personalScoreboard = it }
                        player.scoreboard = scoreboard.scoreboard
                        1
                    }
            )
            .then(
                Commands.literal("hide")
                    .executes { ctx ->
                        (ctx.source.sender as? Player)?.apply {
                            scoreboard = playersScores
                        } ?: 0
                        1
                    }
            )
            .build()
    }
}