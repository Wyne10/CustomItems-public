package org.bigcraft.customitems.command

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.LiteralArgument
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.CommandExecutor
import me.wyne.wutils.i18n.kotlin.placeholderComponent
import org.bigcraft.customitems.CustomItems
import org.bukkit.command.CommandSender

@Singleton
class CustomItemsCommand @Inject constructor(private val plugin: CustomItems) {

    init {
        registerCommand()
    }

    private fun registerCommand() {
        CommandTree("ci")
            .then(GiveCommand())
            .then(LiteralArgument("reload")
                    .withPermission("customitems.reload")
                    .executes(CommandExecutor { sender: CommandSender, _: CommandArguments? ->
                        plugin.reload()
                        sender.placeholderComponent("success-plugin-reload").sendMessage(sender)
                    })
            )
            .register(plugin)
    }

}
