package org.bigcraft.customitems.command

import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.LiteralArgument
import dev.jorel.commandapi.executors.CommandArguments

abstract class SubCommand(argument: String) {
    open val command: Argument<String> = LiteralArgument(argument)
    operator fun invoke() = command
}

val CommandArguments.key: String
    get() = getOrDefaultRaw("key", "")

