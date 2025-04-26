package org.bigcraft.customitems.attribute

import org.bigcraft.customitems.extension.getMaterial
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

val ItemAttributes = AttributeMap(
    mutableMapOf(
        "material" to AttributeFactory { key, config -> MaterialAttribute(config.getMaterial(key)) },
    )
)

class MaterialAttribute(
    material: Material,
) : AttributeBase<Material>("material", material) {

    override fun apply(item: ItemStack): ItemStack =
        item.also { it.type = value }

}
