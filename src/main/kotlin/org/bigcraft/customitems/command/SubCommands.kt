package org.bigcraft.customitems.command

import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.LiteralArgument
import dev.jorel.commandapi.executors.CommandArguments
import org.bukkit.entity.Player
import java.util.ArrayList

abstract class SubCommand(argument: String) {
    open val command: Argument<String> = LiteralArgument(argument)
    operator fun invoke() = command
}

val CommandArguments.key: String
    get() = getOrDefaultRaw("key", "")

val CommandArguments.amount: Int
    get() = getByClassOrDefault("amount", Int::class.java, 1)

@Suppress("UNCHECKED_CAST")
val CommandArguments.targets: Collection<Player>
    get() = ArrayList(getByClassOrDefault("targets", Collection::class.java, emptyList<Player>()) as Collection<Player>)