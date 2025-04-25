package org.bigcraft.customitems.attribute

import me.wyne.wutils.i18n.I18n
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

val MetaAttributes = AttributeMap(
    mutableMapOf("name" to AttributeFactory { _, config -> DisplayNameAttribute(config) })
)

abstract class MetaAttribute<V>(key: String) : AttributeBase<V>(key) {
    override fun apply(item: ItemStack): ItemStack = item.also { it.editMeta(::apply) }
    abstract fun apply(meta: ItemMeta)
}

@Suppress("DEPRECATION")
class DisplayNameAttribute(
    config: ConfigurationSection
) : MetaAttribute<String>("name"), PlayerAwareAttribute<String> {

    override val value: String = config.getString(key)!!

    override fun apply(meta: ItemMeta) =
        meta.setDisplayNameComponent(I18n.global.getPlaceholderComponent(null, value).bungee())

    override fun apply(item: ItemStack, player: Player): ItemStack =
        item.also { it.editMeta { meta -> meta.setDisplayNameComponent(I18n.global.getPlaceholderComponent(player.locale(), player, value).bungee()) } }

}