package me.wyne.customitems.command

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.CommandAPICommand
import me.wyne.customitems.CustomItems
import me.wyne.customitems.LootPoolSupport
import me.wyne.customitems.item.CustomItemManager

@Singleton
class CustomItemsCommand @Inject constructor(
    private val plugin: CustomItems,
    private val itemManager: CustomItemManager
) {

    init {
        registerCommand()
    }

    private fun registerCommand() {
        val command = CommandAPICommand("customitems")
            .withAliases("ci")
            .withSubcommand(ReloadCommand(plugin)())
            .withSubcommand(GiveCommand(itemManager)())
            .withSubcommand(ExchangeCommand(itemManager)())
        if (LootPoolSupport.IS_LOOTPOOL_PRESENT)
            command.withSubcommand(LootCommand(itemManager)())
        command.register(plugin)
    }

}
