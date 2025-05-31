package org.bigcraft.customitems.attribute

import de.tr7zw.nbtapi.NBT
import de.tr7zw.nbtapi.iface.ReadableNBT
import me.wyne.wutils.common.Args
import me.wyne.wutils.config.configurables.attribute.AttributeBase
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.CompositeAttributeFactory
import me.wyne.wutils.config.configurables.item.CompositeAttribute
import me.wyne.wutils.config.configurables.item.ItemStackAttribute
import me.wyne.wutils.config.configurables.item.MetaAttribute
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.extension.COLON_DELIMITER
import org.bigcraft.customitems.extension.COMMA_DELIMITER
import org.bukkit.NamespacedKey
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

val ITEM_ATTRIBUTE_MAP = mapOf(
    "nbt" to AttributeFactory { key, config -> NbtTagAttribute(key, NBT.parseNBT(config.getString(key))) },
    "pdcTag" to PdcTagAttribute.Factory,
    "pdcTags" to AttributeFactory { key, config -> PdcTagsAttribute(key, config) }
)

class NbtTagAttribute(
    key: String,
    tag: ReadableNBT,
) : AttributeBase<ReadableNBT>(key, tag), ItemStackAttribute {

    override fun apply(item: ItemStack) =
        NBT.modify(item) { nbt -> nbt.mergeCompound(value) }

}

class PdcTagAttribute(
    key: String,
    tag: PdcTag
) : AttributeBase<PdcTagAttribute.PdcTag>(key, tag), MetaAttribute {

    override fun apply(meta: ItemMeta) {
        meta.persistentDataContainer.set(NamespacedKey(CustomItems.instance, key), value.type, value.value)
    }

    data class PdcTag(val type: PersistentDataType<Any, Any>, val value: Any)

    @Suppress("UNCHECKED_CAST")
    object Factory : CompositeAttributeFactory {
        override fun fromSection(key: String, section: ConfigurationSection): PdcTagAttribute {
            return PdcTagAttribute(
                key,
                PdcTag(
                    DATA_TYPES[section.getString("type", "STRING")]!!.first as PersistentDataType<Any, Any>,
                    DATA_TYPES[section.getString("type", "STRING")]!!.second(section.getString("value", key)!!)
                )
            )
        }

        override fun fromString(key: String, string: String): PdcTagAttribute {
            val args = Args(string, COLON_DELIMITER)
            return PdcTagAttribute(
                key,
                PdcTag(
                    DATA_TYPES[args.get(0, "STRING")]!!.first as PersistentDataType<Any, Any>,
                    DATA_TYPES[args.get(0, "STRING")]!!.second(args.get(1, key))
                )
            )
        }
    }

    companion object {
        val DATA_TYPES = mapOf(
            "BYTE" to (PersistentDataType.BYTE to { s: String -> s.toByte() }),
            "SHORT" to (PersistentDataType.SHORT to { s: String -> s.toShort() }),
            "INTEGER" to (PersistentDataType.INTEGER to { s: String -> s.toInt() }),
            "LONG" to (PersistentDataType.LONG to { s: String -> s.toLong() }),
            "FLOAT" to (PersistentDataType.FLOAT to { s: String -> s.toFloat() }),
            "DOUBLE" to (PersistentDataType.DOUBLE to { s: String -> s.toDouble() }),
            "STRING" to (PersistentDataType.STRING to { s: String -> s }),
            "BYTE_ARRAY" to (PersistentDataType.BYTE_ARRAY to { s: String -> s.asArray.map(String::toByte).toByteArray() }),
            "INTEGER_ARRAY" to (PersistentDataType.INTEGER_ARRAY to { s: String -> s.asArray.map(String::toInt).toIntArray() }),
            "LONG_ARRAY" to (PersistentDataType.LONG_ARRAY to { s: String -> s.asArray.map(String::toLong).toLongArray() }),
        )

        private val String.asArray
            get() = removeSurrounding("[", "]").split(COMMA_DELIMITER).map(String::trim)
    }

}

class PdcTagsAttribute : CompositeAttribute<PdcTagAttribute>, MetaAttribute {
    constructor(key: String, tags: Map<String, PdcTagAttribute>) : super(key, tags)
    constructor(key: String, config: ConfigurationSection) : super(key, config, PdcTagAttribute.Factory)

    override fun apply(meta: ItemMeta) = value.values.forEach { it.apply(meta) }
}
