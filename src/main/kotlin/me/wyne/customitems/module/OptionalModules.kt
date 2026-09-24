package me.wyne.customitems.module

import com.google.inject.AbstractModule
import me.wyne.customitems.CustomItems
import me.wyne.customitems.command.CustomItemsCommand
import me.wyne.customitems.menu.AbstractMenusRegistry
import me.wyne.customitems.placeholder.ItemPlaceholders

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
            CustomItems.logger.warn(exceptionMessage)
        }
    }
}

val PlaceholderModule: AbstractModule = OptionalModule(
    className = "me.clip.placeholderapi.PlaceholderAPI",
    exceptionMessage = "PlaceholderAPI not found, placeholders are not registered",
    ItemPlaceholders::class.java
)

val CommandModule: AbstractModule = OptionalModule(
    className = "dev.jorel.commandapi.CommandAPI",
    exceptionMessage = "CommandAPI not found, commands are not registered",
    CustomItemsCommand::class.java
)

val AbstractMenusModule: AbstractModule = OptionalModule(
    className = "ru.abstractmenus.api.AbstractMenusPlugin",
    exceptionMessage = "AbstractMenus not found, item property is not registered",
    AbstractMenusRegistry::class.java
)