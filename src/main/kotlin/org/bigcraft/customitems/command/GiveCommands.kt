package org.bigcraft.customitems.command

import dev.jorel.commandapi.CommandAPIBukkit
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.EntitySelectorArgument.ManyPlayers
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandArguments
import me.wyne.wutils.i18n.I18n
import me.wyne.wutils.i18n.kotlin.placeholderComponent
import me.wyne.wutils.i18n.language.replacement.ComponentPlaceholder
import me.wyne.wutils.i18n.language.replacement.Placeholder
import org.bigcraft.customitems.item.CustomItemManager
import org.bukkit.command.CommandSender

private fun CommandArguments.getItemKey(sender: CommandSender): String {
    if (!CustomItemManager.instance.mapKeys.contains(key))
        throw CommandAPIBukkit.failWithBaseComponents(
            *sender.placeholderComponent("error-item-not-found", Placeholder.replace("key", key)).bungee()
        )
    return key
}

object GiveCommand : SubCommand("give") {
    override val command: Argument<String> = super.command
        .withPermission("customitems.give")
        .then(ManyPlayers("targets")
                .then(StringArgument("key")
                        .replaceSuggestions(ArgumentSuggestions.stringCollection { CustomItemManager.instance.mapKeys })
                        .then(IntegerArgument("amount", 1).setOptional(true)
                                .executes(::giveItem))))

    private fun giveItem(sender: CommandSender, args: CommandArguments) {
        val item = CustomItemManager.instance.getItem(args.getItemKey(sender))!!
        val amount = args.amount
        val targets = args.targets
        targets.forEach { player ->
            player.inventory.addItem(item.createItemStack(player).also { it.amount = amount }) }
        sender.placeholderComponent(
            "success-item-give",
            Placeholder.replace("item-key", args.key),
            Placeholder.replace("item-amount", amount),
            Placeholder.replace("targets", targets
                .map { it.name }
                .reduce(I18n::reduceRawString)
            )
        ).replace(ComponentPlaceholder.replace("item-name", item.createItemStack().itemMeta.displayNameComponent))
            .sendMessage(sender)
    }
}