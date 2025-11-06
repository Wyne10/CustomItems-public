package org.bigcraft.customitems.command

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.CommandAPICommand
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.item.CustomItemManager

@Singleton
class CustomItemsCommand @Inject constructor(
    private val plugin: CustomItems,
    private val itemManager: CustomItemManager
) {

    init {
        registerCommand()
    }

    private fun registerCommand() {
        CommandAPICommand("customitems")
            .withAliases("ci")
            .withSubcommand(ReloadCommand(plugin)())
            .withSubcommand(GiveCommand(itemManager)())
            .withSubcommand(ExchangeCommand(itemManager)())
            .register(plugin)
    }

}
