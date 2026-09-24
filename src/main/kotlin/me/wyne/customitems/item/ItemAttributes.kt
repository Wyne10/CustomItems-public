package me.wyne.customitems.item

import de.tr7zw.nbtapi.NBT
import de.tr7zw.nbtapi.iface.ReadableNBT
import me.wyne.wutils.common.Args
import me.wyne.wutils.common.kotlin.config.COMMA_DELIMITER
import me.wyne.wutils.common.range.ClosedIntRange
import me.wyne.wutils.config.configurables.attribute.AttributeBase
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.customitems.spi.ItemAttributeRegistry
import me.wyne.wutils.config.configurables.attribute.CompositeAttribute
import me.wyne.wutils.config.configurables.attribute.CompositeAttributeFactory
import me.wyne.wutils.config.configurables.attribute.common.StringCollectionAttribute
import me.wyne.wutils.config.configurables.item.ItemStackAttribute
import me.wyne.wutils.config.configurables.item.MetaAttribute
import me.wyne.customitems.CustomItems
import org.bukkit.NamespacedKey
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import kotlin.collections.get

internal fun registerItemAttributes(registry: ItemAttributeRegistry) {
    registry.registerNative("nbt", AttributeFactory { key, config -> NbtTagAttribute(key, NBT.parseNBT(config.getString(key))) })
    registry.registerNative("pdc-tag", PdcTagAttribute.Factory)
    registry.registerNative("pdc-tags", AttributeFactory(::PdcTagsAttribute))
    registry.registerNative("compositor", StringCollectionAttribute.SetFactory())
    registry.registerNative("random-amount", AttributeFactory { key, config -> RandomAmountAttribute(key, ClosedIntRange.getIntRange(config.getString(key)!!)) })
    registry.registerNative("random-durability", AttributeFactory { key, config -> RandomDurabilityAttribute(key, ClosedIntRange.getIntRange(config.getString(key)!!)) })
}

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
        meta.persistentDataContainer.set(NamespacedKey.fromString(key, CustomItems.instance)!!, value.type, value.value)
    }

    data class PdcTag(val type: PersistentDataType<Any, Any>, val value: Any)

    @Suppress("UNCHECKED_CAST")
    object Factory : CompositeAttributeFactory<PdcTagAttribute> {
        override fun fromSection(key: String, section: ConfigurationSection): PdcTagAttribute {
            return PdcTagAttribute(
                key,
                PdcTag(
                    DATA_TYPES[section.getString("type", "STRING")]!!.first as PersistentDataType<Any, Any>,
                    DATA_TYPES[section.getString("type", "STRING")]!!.second(section.getString("value", key)!!)
                )
            )
        }

        override fun fromString(key: String, string: String, config: ConfigurationSection): PdcTagAttribute {
            val args = Args(string)
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
    constructor(key: String, tags: Set<PdcTagAttribute>) : super(key, tags)
    constructor(key: String, config: ConfigurationSection) : super(key, config, PdcTagAttribute.Factory)

    override fun apply(meta: ItemMeta) = value.forEach { it.apply(meta) }
}

class RandomAmountAttribute(
    key: String,
    amount: ClosedIntRange
) : AttributeBase<ClosedIntRange>(key, amount), ItemStackAttribute {
    override fun apply(item: ItemStack) {
        item.amount = value.random
    }
}

class RandomDurabilityAttribute(
    key: String,
    durability: ClosedIntRange
) : AttributeBase<ClosedIntRange>(key, durability), ItemStackAttribute {
    override fun apply(item: ItemStack) {
        val maxDurability = item.type.maxDurability.toInt()
        item.editMeta { meta ->
            if (meta is Damageable) meta.damage = (maxDurability - value.random).coerceIn(0, maxDurability)
        }
    }
}