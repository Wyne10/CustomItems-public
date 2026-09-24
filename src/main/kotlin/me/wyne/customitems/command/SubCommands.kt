package me.wyne.customitems.command

import dev.jorel.commandapi.CommandAPIBukkit
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.EntitySelectorArgument
import dev.jorel.commandapi.arguments.GreedyStringArgument
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.CommandExecutor
import me.wyne.wutils.common.command.CommandUtils
import me.wyne.wutils.common.kotlin.inventory.addOrDrop
import me.wyne.wutils.i18n.kotlin.placeholderComponent
import me.wyne.wutils.i18n.kotlin.placeholderString
import me.wyne.wutils.i18n.kotlin.replace
import me.wyne.wutils.i18n.kotlin.replaceComponent
import me.wyne.customitems.CustomItems
import me.wyne.customitems.CustomKeyedLoot
import me.wyne.customitems.item.CustomItemManager
import me.wyne.lootpool.api.Loot
import me.wyne.lootpool.api.LootPoolApi
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.ArrayList

abstract class SubCommand(argument: String) {
    open val command: CommandAPICommand = CommandAPICommand(argument)
    operator fun invoke() = command
}

class ReloadCommand(plugin: CustomItems) : SubCommand("reload") {
    override val command: CommandAPICommand = super.command
        .withPermission("customitems.reload")
        .executes(CommandExecutor { sender, _ ->
            plugin.reload()
            sender.placeholderComponent("success-plugin-reload").sendMessagePlayer(sender)
            CustomItems.logger.info("Plugin reloaded")
        })
}

class GiveCommand(private val itemManager: CustomItemManager) : SubCommand("give") {
    override val command: CommandAPICommand = super.command
        .withPermission("customitems.give")
        .withArguments(EntitySelectorArgument.ManyPlayers("targets"))
        .withArguments(itemKeyArgument(itemManager))
        .withOptionalArguments(IntegerArgument("amount", 1))
        .executes(CommandExecutor { sender, args ->
            assertItemExists(itemManager, args.key, sender)
            val item = itemManager.getItem(args.key)!!
            val amount = args.amount
            val targets = args.targets
            if (targets.isEmpty()) return@CommandExecutor
            targets.forEach { player ->
                player.addOrDrop(true, item.createItemStack(player).also { it.amount = amount }) }
            sender.placeholderComponent(
                "success-item-give",
                "item-key" replace args.key,
                "item-amount" replace amount,
                "targets" replace targets
                    .map { it.name }
                    .reduce { s1, s2 -> "$s1, $s2" }
            ).replace("item-name" replaceComponent item.getName(sender).get())
                .sendMessagePlayer(sender)
        })
}

class ExchangeCommand(private val itemManager: CustomItemManager) : SubCommand("exchange") {
    override val command: CommandAPICommand = super.command
        .withPermission("customitems.exchange")
        .withArguments(CommandUtils.onlinePlayer("target"))
        .withArguments(itemKeyArgument(itemManager))
        .withArguments(IntegerArgument("amount", 1))
        .withArguments(GreedyStringArgument("execute"))
        .executes(CommandExecutor { sender, args ->
            assertItemExists(itemManager, args.key, sender)
            val item = itemManager.getItem(args.key)!!
            val amount = args.amount
            val target = args.getByClass("target", Player::class.java)!!
            val stacks = item.getItems(target.inventory)
            val have = stacks.sumOf { it.amount }
            if (have < amount) {
                target.placeholderComponent("error-insufficient-funds", "price" replace amount, "key" replace args.key)
                    .sendMessage(target)
                return@CommandExecutor
            }
            var remain = amount
            stacks.forEach { stack ->
                if (remain <= 0) return@forEach
                val stackAmount = stack.amount
                stack.amount -= remain
                remain -= stackAmount
            }
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), target.placeholderString(args.getOrDefaultRaw("execute", "")).get())
        })
}

class LootCommand(private val itemManager: CustomItemManager) : SubCommand("loot") {
    override val command: CommandAPICommand = super.command
        .withPermission("lootpool.create")
        .withArguments(itemKeyArgument(itemManager))
        .withOptionalArguments(IntegerArgument("minAmount", 1, 64))
        .withOptionalArguments(IntegerArgument("maxAmount", 1, 64))
        .withOptionalArguments(IntegerArgument("weight", 0))
        .executes(CommandExecutor { sender, args ->
            assertItemExists(itemManager, args.key, sender)
            val key = args.key
            val item = itemManager.getItem(args.key)!!
            val minAmount = args.getByClassOrDefault("minAmount", Int::class.java, Loot.EMPTY.minAmount)
            val maxAmount = args.getByClassOrDefault("maxAmount", Int::class.java, Loot.EMPTY.maxAmount)
            val weight = args.getByClassOrDefault("weight", Int::class.java, Loot.EMPTY.weight)
            if (LootPoolApi.getProvider().getLootPool(key) != null)
                throw CommandAPIBukkit.failWithBaseComponents(
                    *sender.placeholderComponent("error-lootpool-already-exists", "key" replace key).bungee()
                )
            LootPoolApi.getProvider()
                .writeLootPool(CustomKeyedLoot(key, Loot(item.createItemStack(null), weight,
                            minAmount.coerceAtMost(maxAmount), maxAmount.coerceAtLeast(minAmount))))
            sender.placeholderComponent("success-lootpool-create", "key" replace key).sendMessage(sender)
        })
}

fun itemKeyArgument(itemManager: CustomItemManager): Argument<String> =
    StringArgument("key")
        .replaceSuggestions(ArgumentSuggestions.stringCollection { itemManager.itemPaths })

fun assertItemExists(itemManager: CustomItemManager, key: String, sender: CommandSender) {
    if (!itemManager.contains(key))
        throw CommandAPIBukkit.failWithBaseComponents(
            *sender.placeholderComponent("error-item-not-found", "key" replace key).bungee()
        )
}

val CommandArguments.key: String
    get() = getOrDefaultRaw("key", "")
        .substringAfterLast('.')

val CommandArguments.amount: Int
    get() = getByClassOrDefault("amount", Int::class.java, 1)

@Suppress("UNCHECKED_CAST")
val CommandArguments.targets: Collection<Player>
    get() = ArrayList(getByClassOrDefault("targets", Collection::class.java, emptyList<Player>()) as Collection<Player>)