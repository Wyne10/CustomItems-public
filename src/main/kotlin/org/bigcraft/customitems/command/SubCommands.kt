package org.bigcraft.customitems.command

import dev.jorel.commandapi.CommandAPIBukkit
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.EntitySelectorArgument
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.CommandExecutor
import me.wyne.wutils.i18n.I18n
import me.wyne.wutils.i18n.kotlin.placeholderComponent
import me.wyne.wutils.i18n.kotlin.replace
import me.wyne.wutils.i18n.kotlin.replaceComponent
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.item.CustomItemManager
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
            sender.placeholderComponent("success-plugin-reload").sendMessage(sender)
            CustomItems.log.info("Plugin reloaded")
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
            targets.forEach { player ->
                player.inventory.addItem(item.createItemStack(player).also { it.amount = amount }) }
            sender.placeholderComponent(
                "success-item-give",
                "item-key" replace args.key,
                "item-amount" replace amount,
                "targets" replace targets
                    .map { it.name }
                    .reduce(I18n::reduceRawString)
            ).replace("item-name" replaceComponent item.getName(sender).get())
                .sendMessage(sender)
        })
}

fun itemKeyArgument(itemManager: CustomItemManager): Argument<String> =
    StringArgument("key")
        .replaceSuggestions(ArgumentSuggestions.stringCollection { itemManager.mapKeys })

fun assertItemExists(itemManager: CustomItemManager, key: String, sender: CommandSender) {
    if (!itemManager.mapKeys.contains(key))
        throw CommandAPIBukkit.failWithBaseComponents(
            *sender.placeholderComponent("error-item-not-found", "key" replace key).bungee()
        )
}

val CommandArguments.key: String
    get() = getOrDefaultRaw("key", "")

val CommandArguments.amount: Int
    get() = getByClassOrDefault("amount", Int::class.java, 1)

@Suppress("UNCHECKED_CAST")
val CommandArguments.targets: Collection<Player>
    get() = ArrayList(getByClassOrDefault("targets", Collection::class.java, emptyList<Player>()) as Collection<Player>)