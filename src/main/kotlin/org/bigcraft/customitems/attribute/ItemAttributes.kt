package org.bigcraft.customitems.attribute

import de.tr7zw.nbtapi.NBT
import de.tr7zw.nbtapi.iface.ReadableNBT
import org.bigcraft.customitems.extension.getMaterial
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

val ItemAttributes = AttributeMap(
    mutableMapOf(
        "material" to AttributeFactory { key, config -> MaterialAttribute(config.getMaterial(key)) },
        "nbt" to AttributeFactory { key, config -> NbtTagAttribute(NBT.parseNBT(config.getString(key))) }
    )
)

class MaterialAttribute(
    material: Material,
) : AttributeBase<ItemStack, Material>("material", material) {

    override fun apply(obj: ItemStack) {
        obj.type = value
    }

}

class NbtTagAttribute(
    tag: ReadableNBT,
) : AttributeBase<ItemStack, ReadableNBT>("nbt", tag) {

    override fun apply(obj: ItemStack) =
        NBT.modify(obj) { nbt -> nbt.mergeCompound(value) }

}
