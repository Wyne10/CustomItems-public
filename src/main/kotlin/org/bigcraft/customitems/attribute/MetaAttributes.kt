package org.bigcraft.customitems.attribute

import com.destroystokyo.paper.profile.ProfileProperty
import me.wyne.wutils.common.Args
import me.wyne.wutils.i18n.I18n
import org.bigcraft.customitems.attribute.EnchantmentAttribute.EnchantmentData
import org.bigcraft.customitems.extension.COLON_DELIMITER
import org.bigcraft.customitems.extension.SPACE_DELIMITER
import org.bigcraft.customitems.extension.getIntNullable
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.NamespacedKey
import org.bukkit.OfflinePlayer
import org.bukkit.attribute.AttributeModifier
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.*
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import java.util.*
import kotlin.math.min

val MetaAttributes = AttributeMap(
    mutableMapOf(
        "name" to AttributeFactory { key, config -> DisplayNameAttribute(config.getString(key)!!) },
        "lore" to AttributeFactory { key, config -> LoreAttribute(config.getStringList(key)) },
        "flags" to AttributeFactory { key, config -> FlagsAttribute(config.getStringList(key).mapNotNull { ItemFlag.valueOf(it) }.toTypedArray()) },
        "skull" to AttributeFactory { key, config -> SkullAttribute(Bukkit.getPlayerUniqueId(config.getString(key)!!)?.let { Bukkit.getOfflinePlayer(it) }) },
        "skull64" to AttributeFactory { key, config -> Skull64Attribute(config.getString(key)!!) },
        "skullPlayer" to AttributeFactory { _, _ -> PlayerSkullAttribute },
        "unbreakable" to AttributeFactory { key, config -> UnbreakableAttribute(config.getBoolean(key, false)) },
        "enchantment" to EnchantmentAttribute.Factory,
        "enchantments" to AttributeFactory { key, config -> CompositeAttribute(key, config, EnchantmentAttribute.Factory) },
        "attribute" to GenericAttribute.Factory,
        "attributes" to AttributeFactory { key, config -> CompositeAttribute(key, config, GenericAttribute.Factory) },
        "glow" to AttributeFactory { key, config -> GlowAttribute(config.getBoolean(key, false)) },
        "durability" to AttributeFactory { key, config -> DurabilityAttribute(config.getInt(key, 1)) },
        "damage" to AttributeFactory { key, config -> DamageAttribute(config.getInt(key, 0)) },
        "model" to AttributeFactory { key, config -> CustomModelDataAttribute(config.getIntNullable(key, null)) },
        "repairCost" to AttributeFactory { key, config -> RepairableAttribute(config.getInt(key, 1)) },
        "potionColor" to AttributeFactory { key, config -> PotionColorAttribute(Color.fromRGB(config.getInt(key, 0))) },
        "potionType" to AttributeFactory { key, config -> PotionTypeAttribute(PotionType.valueOf(config.getString(key, "UNCRAFTABLE")!!)) },
        "potionModifier" to AttributeFactory { key, config -> PotionModifierAttribute(PotionModifierAttribute.PotionModifier.valueOf(config.getString(key, "NONE")!!)) },
        "potionEffect" to PotionEffectAttribute.Factory,
        "potionEffects" to AttributeFactory { key, config -> CompositeAttribute(key, config, PotionEffectAttribute.Factory) }
    )
)

abstract class MetaAttribute<V>(key: String, value: V) : AttributeBase<V>(key, value) {
    override fun apply(item: ItemStack): ItemStack = item.also { it.editMeta(::apply) }
    abstract fun apply(meta: ItemMeta)
}

@Suppress("DEPRECATION")
class DisplayNameAttribute(
    name: String
) : MetaAttribute<String>("name", name), PlayerAwareAttribute<String> {

    override fun apply(meta: ItemMeta) =
        meta.setDisplayNameComponent(I18n.global.getPlaceholderComponent(null, value).bungee())

    override fun apply(item: ItemStack, player: Player): ItemStack =
        item.also { it.editMeta { meta -> meta.setDisplayNameComponent(I18n.global.getPlaceholderComponent(player.locale(), player, value).bungee()) } }

}

@Suppress("DEPRECATION")
class LoreAttribute(
    lore: List<String>
) : MetaAttribute<List<String>>("lore", lore), PlayerAwareAttribute<List<String>> {

    override fun apply(meta: ItemMeta) =
        meta.setLoreComponents(value.map { I18n.global.getPlaceholderComponent(null, it).bungee() })

    override fun apply(item: ItemStack, player: Player): ItemStack =
        item.also { it.editMeta { meta -> meta.setLoreComponents(value.map { I18n.global.getPlaceholderComponent(null, it).bungee() } ) } }

}

class FlagsAttribute(
    flags: Array<ItemFlag>
) : MetaAttribute<Array<ItemFlag>>("flags", flags) {

    override fun apply(meta: ItemMeta) =
        meta.addItemFlags(*value)

}

class SkullAttribute(
    owner: OfflinePlayer?
) : MetaAttribute<OfflinePlayer?>("skull", owner) {

    override fun apply(meta: ItemMeta) {
        if (meta !is SkullMeta) return
        meta.owningPlayer = value
    }

}

class Skull64Attribute(
    skull64: String
) : MetaAttribute<String>("skull64", skull64) {

    override fun apply(meta: ItemMeta) {
        if (meta !is SkullMeta) return
        val profile = Bukkit.createProfile(UUID.randomUUID())
        profile.setProperty(ProfileProperty("textures", value))
        meta.playerProfile = profile
    }

}

object PlayerSkullAttribute : Attribute<Unit> by EmptyAttribute("skullPlayer"), PlayerAwareAttribute<Unit> {

    override fun apply(item: ItemStack, player: Player): ItemStack {
        if (item.itemMeta !is SkullMeta) return item
        item.editMeta { meta -> (meta as SkullMeta).owningPlayer = player }
        return item
    }

}

class UnbreakableAttribute(
    unbreakable: Boolean
) : MetaAttribute<Boolean>("unbreakable", unbreakable) {

    override fun apply(meta: ItemMeta) {
        meta.isUnbreakable = value
    }

}

class GlowAttribute(
    glow: Boolean
) : MetaAttribute<Boolean>("glow", glow) {

    override fun apply(meta: ItemMeta) {
        if (meta.hasEnchants()) return
        if (!value) return
        meta.addEnchant(Enchantment.LURE, 1, true)
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
    }

}

class DurabilityAttribute(
    durability: Int
) : MetaAttribute<Int>("durability", durability) {

    private var item: ItemStack? = null

    override fun apply(item: ItemStack): ItemStack {
        this.item = item
        return super.apply(item)
    }

    override fun apply(meta: ItemMeta) {
        if (meta !is Damageable) return
        val maxDurability = item?.type?.maxDurability?.toInt() ?: 0
        meta.damage = maxDurability - min(maxDurability, value)
    }

}

class DamageAttribute(
    damage: Int
) : MetaAttribute<Int>("damage", damage) {

    override fun apply(meta: ItemMeta) {
        if (meta !is Damageable) return
        meta.damage = value
    }

}

class CustomModelDataAttribute(
    model: Int?
) : MetaAttribute<Int?>("model", model) {

    override fun apply(meta: ItemMeta) =
        meta.setCustomModelData(value)

}

class RepairableAttribute(
    cost: Int
) : MetaAttribute<Int>("repairCost", cost) {

    override fun apply(meta: ItemMeta) {
        if (meta !is Repairable) return
        meta.repairCost = value
    }

}

class EnchantmentAttribute(
    key: String,
    enchantment: EnchantmentData
) : MetaAttribute<EnchantmentData>(key, enchantment) {

    override fun apply(meta: ItemMeta) {
        meta.addEnchant(value.enchantment, value.level, true)
    }

    data class EnchantmentData(val enchantment: Enchantment, val level: Int)

    object Factory : CompositeAttributeFactory {
        override fun fromSection(key: String, section: ConfigurationSection): EnchantmentAttribute {
            return EnchantmentAttribute(
                key,
                EnchantmentData(
                    Enchantment.getByKey(NamespacedKey.fromString(section.getString("enchantment", "lure")!!))!!,
                    section.getInt("level", 1)
                )
            )
        }

        override fun fromString(key: String, string: String): EnchantmentAttribute {
            val args = Args(string, COLON_DELIMITER)
            return EnchantmentAttribute(
                key,
                EnchantmentData(
                    Enchantment.getByKey(NamespacedKey.fromString(args.get(0, "lure")))!!,
                    args.get(1, "1").toInt()
                )
            )
        }
    }

}

class GenericAttribute(
    key: String,
    attribute: AttributeData
) : MetaAttribute<GenericAttribute.AttributeData>(key, attribute) {

    override fun apply(meta: ItemMeta) {
        meta.addAttributeModifier(value.attribute, value.modifier)
    }

    data class AttributeData(val attribute: org.bukkit.attribute.Attribute, val modifier: AttributeModifier)

    object Factory : CompositeAttributeFactory {
        override fun fromSection(key: String, section: ConfigurationSection): GenericAttribute {
            return GenericAttribute(
                key,
                AttributeData(
                    getByKey(NamespacedKey.fromString(section.getString("attribute", "generic.armor")!!)!!)
                        ?: org.bukkit.attribute.Attribute.GENERIC_ARMOR,
                    AttributeModifier(
                        UUID.fromString(section.getString("uuid", UUID.randomUUID().toString())),
                        section.getString("name", "CI Attribute")!!,
                        section.getDouble("amount", 1.0),
                        AttributeModifier.Operation.valueOf(section.getString("operation", "ADD_NUMBER")!!),
                        EquipmentSlot.valueOf(section.getString("slot", "HAND")!!),
                    )
                )
            )
        }

        override fun fromString(key: String, string: String): GenericAttribute {
            val args = Args(string, SPACE_DELIMITER)
            return GenericAttribute(
                key,
                AttributeData(
                    getByKey(NamespacedKey.fromString(args.get(0, "generic.armor"))!!)
                        ?: org.bukkit.attribute.Attribute.GENERIC_ARMOR,
                    AttributeModifier(
                        UUID.randomUUID(),
                        "CI Attribute",
                        args.get(1, "1.0").toDouble(),
                        AttributeModifier.Operation.valueOf(args.get(2, "ADD_NUMBER")),
                        EquipmentSlot.valueOf(args.get(3, "HAND"))
                    )
                )
            )
        }

        private fun getByKey(key: NamespacedKey): org.bukkit.attribute.Attribute? =
            org.bukkit.attribute.Attribute.values().firstOrNull { it.key == key }
    }

}

class PotionColorAttribute(
    color: Color
) : MetaAttribute<Color>("potionColor", color) {

    override fun apply(meta: ItemMeta) {
        if (meta !is PotionMeta) return
        meta.color = value
    }

}

class PotionTypeAttribute(
    type: PotionType
) : MetaAttribute<PotionType>("potionType", type) {

    override fun apply(meta: ItemMeta) {
        if (meta !is PotionMeta) return
        val baseData = meta.basePotionData ?: PotionData(value, false, false)
        meta.basePotionData = PotionData(value, baseData.isExtended, baseData.isUpgraded)
    }

}

class PotionModifierAttribute(
    modifier: PotionModifier
) : MetaAttribute<PotionModifierAttribute.PotionModifier>("potionModifier", modifier) {

    override fun apply(meta: ItemMeta) {
        if (meta !is PotionMeta) return
        val baseData = meta.basePotionData ?: PotionData(PotionType.UNCRAFTABLE, false, false)
        meta.basePotionData = when (value) {
            PotionModifier.NONE -> PotionData(baseData.type, false, false)

            PotionModifier.EXTENDED -> if (baseData.type.isExtendable)
                PotionData(baseData.type, true, false) else baseData

            PotionModifier.UPGRADED -> if (baseData.type.isUpgradeable)
                PotionData(baseData.type, false, true) else baseData
        }
    }

    enum class PotionModifier {
        NONE,
        EXTENDED,
        UPGRADED
    }

}

class PotionEffectAttribute(
    key: String,
    effect: PotionEffect
) : MetaAttribute<PotionEffect>(key, effect) {

    override fun apply(meta: ItemMeta) {
        if (meta !is PotionMeta) return
        meta.addCustomEffect(value, false)
    }

    object Factory : CompositeAttributeFactory {
        override fun fromSection(key: String, section: ConfigurationSection): PotionEffectAttribute {
            return PotionEffectAttribute(
                key,
                PotionEffect(
                    PotionEffectType.getByName(section.getString("type", "SPEED")!!)
                        ?: PotionEffectType.SPEED,
                    section.getInt("duration", 20),
                    section.getInt("amplifier", 1),
                    section.getBoolean("ambient", false),
                    section.getBoolean("particles", false),
                    section.getBoolean("icon", true)
                )
            )
        }

        override fun fromString(key: String, string: String): PotionEffectAttribute {
            val args = Args(string, SPACE_DELIMITER)
            return PotionEffectAttribute(
                key,
                PotionEffect(
                    PotionEffectType.getByName(args.get(0, "SPEED"))
                        ?: PotionEffectType.SPEED,
                    args.get(1, "20").toInt(),
                    args.get(2, "1").toInt(),
                    args.get(3, "false").toBoolean(),
                    args.get(4, "false").toBoolean(),
                    args.get(5, "true").toBoolean(),
                )
            )
        }
    }

}