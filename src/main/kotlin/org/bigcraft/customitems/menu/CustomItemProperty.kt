package org.bigcraft.customitems.menu

import org.bigcraft.customitems.item.CustomItemManager
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import ru.abstractmenus.api.inventory.ItemProperty
import ru.abstractmenus.api.inventory.Menu
import ru.abstractmenus.hocon.api.ConfigNode
import ru.abstractmenus.hocon.api.serialize.NodeSerializer

class CustomItemProperty(private val customItemKey: String) : ItemProperty {

    override fun canReplaceMaterial(): Boolean = true

    override fun isApplyMeta(): Boolean = false

    override fun apply(item: ItemStack, meta: ItemMeta, player: Player, menu: Menu) {
        CustomItemManager.instance.getItem(customItemKey)?.applyItemStack(item, player)
    }

    object Factory : NodeSerializer<CustomItemProperty> {
        override fun deserialize(type: Class<CustomItemProperty>, node: ConfigNode): CustomItemProperty =
            CustomItemProperty(node.string)
    }

}