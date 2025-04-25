package org.bigcraft.customitems.module

import com.google.inject.AbstractModule
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.command.CustomItemsCommand

private class OptionalModule(
    private val className: String,
    private val exceptionMessage: String = "$className not found, module ignored",
    private vararg val modules: Class<out Any>
) : AbstractModule() {
    override fun configure() {
        try {
            Class.forName(className)
            modules.forEach { bind(it) }
        } catch (e: ClassNotFoundException) {
            CustomItems.log.warn(exceptionMessage)
        }
    }
}

val PlaceholderModule: AbstractModule = OptionalModule(
    className = "me.clip.placeholderapi.PlaceholderAPI",
    exceptionMessage = "PlaceholderAPI not found, placeholders are not registered"
)

val CommandModule: AbstractModule = OptionalModule(
    className = "dev.jorel.commandapi.CommandAPI",
    exceptionMessage = "CommandAPI not found, commands are not registered",
    CustomItemsCommand::class.java
)